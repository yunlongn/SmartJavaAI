package cn.smartjavaai.face.model;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.AbstractFaceModel;
import cn.smartjavaai.face.FaceModelConfig;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.translator.FaceFeatureTranslator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author dwj
 */
@Slf4j
public class FeatureExtractionModel extends AbstractFaceModel implements AutoCloseable{


    private ObjectPool<Predictor<Image, float[]>> predictorPool;


    private ZooModel<Image, float[]> model;

    public static final List<Float> mean =
            Arrays.asList(
                    127.5f / 255.0f,
                    127.5f / 255.0f,
                    127.5f / 255.0f,
                    128.0f / 255.0f,
                    128.0f / 255.0f,
                    128.0f / 255.0f);


    /**
     * 加载人脸特征提取模型
     * @param config
     */
    @Override
    public void loadModel(FaceModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        String normalize = mean.stream().map(Object::toString).collect(Collectors.joining(","));
        Criteria<Image, float[]> faceFeatureCriteria =
                Criteria.builder()
                        .setTypes(Image.class, float[].class)
                        .optModelName("face_feature") // specify model file prefix
                        .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null :
                                "https://resources.djl.ai/test-models/pytorch/face_feature.zip")
                        .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                        .optTranslator(new FaceFeatureTranslator())
                        .optArgument("normalize", normalize)
                        .optDevice(device)
                        .optEngine("PyTorch") // Use PyTorch engine
                        .optProgress(new ProgressBar())
                        .build();

        try {
            model = faceFeatureCriteria.loadModel();
            // 创建池子：每个线程独享 Predictor
            this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(model));
            log.info("当前设备: " + model.getNDManager().getDevice());
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new FaceException("模型加载失败", e);
        }
    }

    private float[] featureExtraction(Image image){
        image.getWrappedImage();
        Predictor<Image, float[]> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            return predictor.predict(image);
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    predictorPool.returnObject(predictor); //归还
                    log.info("释放资源");
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        predictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }


    /**
     * 特征提取
     * @param imagePath 图片路径
     * @return
     */
    @Override
    public float[] featureExtraction(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
        } catch (IOException e) {
            throw new FaceException("无效图片", e);
        }
        return featureExtraction(img);
    }

    /**
     * 特征提取
     * @param inputStream 输入流
     * @return
     */
    @Override
    public float[] featureExtraction(InputStream inputStream) {
        if(Objects.isNull(inputStream)){
            throw new FaceException("图像输入流无效");
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromInputStream(inputStream);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }
        return featureExtraction(img);
    }

    @Override
    public float[] featureExtraction(BufferedImage sourceImage) {
        if(!ImageUtils.isImageValid(sourceImage)){
            throw new FaceException("图像无效");
        }
        Image img = ImageFactory.getInstance().fromImage(sourceImage);
        return featureExtraction(img);
    }

    @Override
    public float[] featureExtraction(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return featureExtraction(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("无效图片字节流", e);
        }
    }

    /**
     * 计算相似度
     * @param feature1 图1特征
     * @param feature2 图2特征
     * @return
     */
    @Override
    public float calculSimilar(float[] feature1, float[] feature2) {
        float ret = 0.0f;
        float mod1 = 0.0f;
        float mod2 = 0.0f;
        int length = feature1.length;
        for (int i = 0; i < length; ++i) {
            ret += feature1[i] * feature2[i];
            mod1 += feature1[i] * feature1[i];
            mod2 += feature2[i] * feature2[i];
        }
        return (float) ((ret / Math.sqrt(mod1) / Math.sqrt(mod2) + 1) / 2.0f);
    }

    /**
     * 特征比较
     * @param imagePath1 图1路径
     * @param imagePath2 图2路径
     * @return
     */
    @Override
    public float featureComparison(String imagePath1, String imagePath2) {
        if(!FileUtils.isFileExists(imagePath1) || !FileUtils.isFileExists(imagePath2)){
            throw new FaceException("图像文件不存在");
        }
        float[] feature1 = featureExtraction(imagePath1);
        float[] feature2 = featureExtraction(imagePath2);
        return calculSimilar(feature1, feature2);
    }

    /**
     * 特征比较
     * @param inputStream1 图1输入流
     * @param inputStream2 图2输入流
     * @return
     */
    @Override
    public float featureComparison(InputStream inputStream1, InputStream inputStream2) {
        if(Objects.isNull(inputStream1) || Objects.isNull(inputStream2)){
            throw new FaceException("图像输入流无效");
        }
        float[] feature1 = featureExtraction(inputStream1);
        float[] feature2 = featureExtraction(inputStream2);
        return calculSimilar(feature1, feature2);
    }

    @Override
    public float featureComparison(BufferedImage sourceImage1, BufferedImage sourceImag2) {
        if(!ImageUtils.isImageValid(sourceImage1) || !ImageUtils.isImageValid(sourceImag2)){
            throw new FaceException("图像无效");
        }
        float[] feature1 = featureExtraction(sourceImage1);
        float[] feature2 = featureExtraction(sourceImag2);
        return calculSimilar(feature1, feature2);
    }

    @Override
    public float featureComparison(byte[] imageData1, byte[] imageData2) {
        if(Objects.isNull(imageData1) || Objects.isNull(imageData2)){
            throw new FaceException("图像无效");
        }
        float[] feature1 = featureExtraction(imageData1);
        float[] feature2 = featureExtraction(imageData2);
        return calculSimilar(feature1, feature2);
    }

    @Override
    public void close() {
        if (predictorPool != null) {
            predictorPool.close();
        }
    }

}

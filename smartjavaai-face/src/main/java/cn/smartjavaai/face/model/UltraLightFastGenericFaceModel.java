package cn.smartjavaai.face.model;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.*;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.translator.FaceDetectionTranslator;
import cn.smartjavaai.face.utils.FaceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;

/**
 * @author dwj
 */
@Slf4j
public class UltraLightFastGenericFaceModel extends AbstractFaceModel implements AutoCloseable{


    private ObjectPool<Predictor<Image, DetectedObjects>> predictorPool;

    /**
     * 特征图层的基础缩放比例
     */
    private static final int[][] scales = {{10, 16, 24}, {32, 48}, {64, 96}, {128, 192, 256}};
    /**
     * 特征图相对于原图的采样步长
     */
    private static final int[] steps = {8, 16, 32, 64};
    /**
     * 缩放系数
     */
    private static final double[] variance = {0.1f, 0.2f};


    private ZooModel<Image, DetectedObjects> model;




    /**
     * 加载模型
     * @param config
     */
    @Override
    public void loadModel(FaceModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        FaceDetectionTranslator translator =
                new FaceDetectionTranslator(config.getConfidenceThreshold(), config.getNmsThresh(), variance, FaceConfig.MAX_FACE_LIMIT, scales, steps);
        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null : "https://resources.djl.ai/test-models/pytorch/ultranet.zip")
                        .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                        .optTranslator(translator)
                        .optProgress(new ProgressBar())
                        .optDevice(device)
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();
        try {
            model = criteria.loadModel();
            // 创建池子：每个线程独享 Predictor
            this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(model));
            log.info("当前设备: " + model.getNDManager().getDevice());
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new FaceException("模型加载失败", e);
        }
    }

    /**
     * 检测人脸
     * @param imagePath 图片路径
     * @return
     * @throws Exception
     */
    @Override
    public DetectionResponse detect(String imagePath){
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
        } catch (IOException e) {
            throw new FaceException("无效的图片", e);
        }
        DetectedObjects detection = detect(img);
        return FaceUtils.convertToDetectionResponse(detection,img);
    }

    /**
     * 检测人脸
     * @param imageInputStream 图片流
     * @return
     * @throws Exception
     */
    @Override
    public DetectionResponse detect(InputStream imageInputStream){
        try {
            Image img = ImageFactory.getInstance().fromInputStream(imageInputStream);
            DetectedObjects detection = detect(img);
            return FaceUtils.convertToDetectionResponse(detection,img);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }

    }

    @Override
    public DetectionResponse detect(BufferedImage image) {
        Image img = ImageFactory.getInstance().fromImage(image);
        DetectedObjects detection = detect(img);
        return FaceUtils.convertToDetectionResponse(detection,img);
    }

    @Override
    public DetectionResponse detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public void detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        try {
            Image img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            DetectedObjects detectedObjects = detect(img);
            if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
                throw new FaceException("未识别到人脸");
            }
            img.drawBoundingBoxes(detectedObjects);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 调用 save 方法将 Image 写入字节流
            img.save(new FileOutputStream(Paths.get(outputPath).toAbsolutePath().toString()), "png");
        } catch (IOException e) {
            throw new FaceException(e);
        }
    }

    @Override
    public BufferedImage detectAndDraw(BufferedImage sourceImage) {
        if(!ImageUtils.isImageValid(sourceImage)){
            throw new FaceException("图像无效");
        }
        Image img = ImageFactory.getInstance().fromImage(sourceImage);
        DetectedObjects detectedObjects = detect(img);
        if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
            throw new FaceException("未识别到人脸");
        }
        img.drawBoundingBoxes(detectedObjects);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 调用 save 方法将 Image 写入字节流
            img.save(outputStream, "png");
            // 将字节流转换为 BufferedImage
            byte[] imageBytes = outputStream.toByteArray();
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            throw new FaceException("导出图片失败", e);
        }
    }

    /**
     * 人脸检测
     * @param image
     * @return
     */
    private DetectedObjects detect(Image image){
        Predictor<Image, DetectedObjects> predictor = null;
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


    @Override
    public void close() {
        if (predictorPool != null) {
            predictorPool.close();
        }
    }
}

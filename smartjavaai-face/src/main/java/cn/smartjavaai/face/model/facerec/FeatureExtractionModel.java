package cn.smartjavaai.face.model.facerec;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceExtractConfig;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceModelFactory;
import cn.smartjavaai.face.translator.FaceFeatureTranslator;
import cn.smartjavaai.face.utils.FaceAlignUtils;
import cn.smartjavaai.face.utils.FaceUtils;
import cn.smartjavaai.face.utils.OpenCVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;
import org.opencv.face.Face;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        float[] feature1 = extractTopFaceFeature(imagePath1);
        float[] feature2 = extractTopFaceFeature(imagePath2);
        return calculSimilar(feature1, feature2);
    }


    @Override
    public float featureComparison(BufferedImage sourceImage1, BufferedImage sourceImag2) {
        if(!ImageUtils.isImageValid(sourceImage1) || !ImageUtils.isImageValid(sourceImag2)){
            throw new FaceException("图像无效");
        }
        float[] feature1 = extractTopFaceFeature(sourceImage1);
        float[] feature2 = extractTopFaceFeature(sourceImag2);
        return calculSimilar(feature1, feature2);
    }

    @Override
    public float featureComparison(byte[] imageData1, byte[] imageData2) {
        if(Objects.isNull(imageData1) || Objects.isNull(imageData2)){
            throw new FaceException("图像无效");
        }
        float[] feature1 = extractTopFaceFeature(imageData1);
        float[] feature2 = extractTopFaceFeature(imageData2);
        return calculSimilar(feature1, feature2);
    }

    /**
     * 获取默认特征提取配置
     * @return
     */
    private FaceExtractConfig getDefaultConfig() {
        FaceExtractConfig config = new FaceExtractConfig();
        FaceModelConfig detectModelConfig = new FaceModelConfig();
        detectModelConfig.setModelEnum(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);
        config.setDetectModelConfig(detectModelConfig);
        return config;
    }

    @Override
    public List<float[]> extractFeatures(String imagePath) {
        return extractFeatures(imagePath, getDefaultConfig());
    }

    @Override
    public List<float[]> extractFeatures(byte[] imageData) {
        return extractFeatures(imageData, getDefaultConfig());
    }

    @Override
    public List<float[]> extractFeatures(BufferedImage image) {
        return extractFeatures(image, getDefaultConfig());
    }


    @Override
    public List<float[]> extractFeatures(BufferedImage image, FaceExtractConfig config) {
        if(Objects.isNull(config)){
            throw new FaceException("config为null");
        }
        List<float[]> featureList = new ArrayList<float[]>();
        if(Objects.isNull(config.getDetectModelConfig())){
            throw new FaceException("config.detectModelConfig为null");
        }
        FaceModel faceModel = FaceModelFactory.getInstance().getModel(config.getDetectModelConfig());
        DetectionResponse detectedResult = faceModel.detect(image);
        if(Objects.isNull(detectedResult) || Objects.isNull(detectedResult.getDetectionInfoList()) || detectedResult.getDetectionInfoList().isEmpty()){
            throw new FaceException("未检测到人脸");
        }
        Image djlImage = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        NDManager manager = NDManager.newBaseManager();
        for (DetectionInfo detectionInfo : detectedResult.getDetectionInfoList()){
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();

            float[] features = null;
            //裁剪人脸
            Image subImage = djlImage.getSubImage(rectangle.getX(), rectangle.getY() , rectangle.getWidth() , rectangle.getHeight());
            //人脸对齐
            if(config.isAlign()){
                //获取子图中人脸关键点坐标
                double[][] pointsArray = FaceUtils.facePoints(detectionInfo.getFaceInfo().getKeyPoints());
                NDArray srcPoints = manager.create(pointsArray);
                NDArray dstPoints = FaceUtils.faceTemplate512x512(manager);
                // 5点仿射变换
                Mat affine_matrix = OpenCVUtils.toOpenCVMat(manager, srcPoints, dstPoints);
                /*Mat sourceMat = OpenCVUtils.image2Mat(image);
                Mat mat = FaceAlignUtils.warpAffine(sourceMat, affine_matrix);
                //OpenCVUtils.mat2Image(mat);
                Image alignedImg = ImageFactory.getInstance().fromImage(mat);
                features = featureExtraction(alignedImg);*/
                Mat mat = FaceAlignUtils.warpAffine((Mat) djlImage.getWrappedImage(), affine_matrix);
                Image alignedImg = OpenCVImageFactory.getInstance().fromImage(mat);
                features = featureExtraction(alignedImg);
            }else{
                //不对齐人脸
                features = featureExtraction(subImage);
            }
            if(Objects.nonNull(features)){
                featureList.add(features);
            }
        }
        return featureList;
    }

    @Override
    public List<float[]> extractFeatures(String imagePath, FaceExtractConfig config) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return extractFeatures(image, config);
    }

    @Override
    public List<float[]> extractFeatures(byte[] imageData, FaceExtractConfig config) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return extractFeatures(ImageIO.read(new ByteArrayInputStream(imageData)), config);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public float[] extractTopFaceFeature(BufferedImage image) {
        return extractTopFaceFeature(image, getDefaultConfig());
    }

    @Override
    public float[] extractTopFaceFeature(String imagePath) {
        return extractTopFaceFeature(imagePath, getDefaultConfig());
    }

    @Override
    public float[] extractTopFaceFeature(byte[] imageData) {
        return extractTopFaceFeature(imageData, getDefaultConfig());
    }

    @Override
    public float[] extractTopFaceFeature(BufferedImage image, FaceExtractConfig config) {
        if(Objects.isNull(config)){
            throw new FaceException("config为null");
        }
        if(Objects.isNull(config.getDetectModelConfig())){
            throw new FaceException("config.detectModelConfig为null");
        }
        Image djlImage = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        float[] features = null;
        if(config.isCropFace()){
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config.getDetectModelConfig());
            DetectionResponse detectedResult = faceModel.detect(image);
            if(Objects.isNull(detectedResult) || Objects.isNull(detectedResult.getDetectionInfoList()) || detectedResult.getDetectionInfoList().isEmpty()){
                throw new FaceException("未检测到人脸");
            }
            //只取第一个人脸
            DetectionInfo detectionInfo = detectedResult.getDetectionInfoList().get(0);
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            //裁剪人脸
            Image subImage = djlImage.getSubImage(rectangle.getX(), rectangle.getY() , rectangle.getWidth() , rectangle.getHeight());
            //人脸对齐
            if(config.isAlign()){
                NDManager manager = NDManager.newBaseManager();
                //获取子图中人脸关键点坐标
                double[][] pointsArray = FaceUtils.facePoints(detectionInfo.getFaceInfo().getKeyPoints());
                NDArray srcPoints = manager.create(pointsArray);
                NDArray dstPoints = FaceUtils.faceTemplate512x512(manager);
                // 5点仿射变换
                Mat affine_matrix = OpenCVUtils.toOpenCVMat(manager, srcPoints, dstPoints);
                /*Mat sourceMat = OpenCVUtils.image2Mat(image);
                Mat mat = FaceAlignUtils.warpAffine(sourceMat, affine_matrix);
                OpenCVUtils.mat2Image(mat);
                Image alignedImg = ImageFactory.getInstance().fromImage(OpenCVUtils.mat2Image(mat));
                features = featureExtraction(alignedImg);*/
                Mat mat = FaceAlignUtils.warpAffine((Mat) djlImage.getWrappedImage(), affine_matrix);
                Image alignedImg = OpenCVImageFactory.getInstance().fromImage(mat);
                features = featureExtraction(alignedImg);
            }else{
                //不对齐人脸
                features = featureExtraction(subImage);
            }
        }else{
            //不裁剪人脸直接提取特征
            features = featureExtraction(djlImage);
        }
        return features;
    }

    @Override
    public float[] extractTopFaceFeature(String imagePath, FaceExtractConfig config) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return extractTopFaceFeature(image, config);
    }

    @Override
    public float[] extractTopFaceFeature(byte[] imageData, FaceExtractConfig config) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return extractTopFaceFeature(ImageIO.read(new ByteArrayInputStream(imageData)), config);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public void close() {
        if (predictorPool != null) {
            predictorPool.close();
        }
    }



}

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
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceExtractConfig;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.entity.FaceRegisterInfo;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.enums.SimilarityType;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceModelFactory;
import cn.smartjavaai.face.translator.FaceFeatureTranslator;
import cn.smartjavaai.face.utils.*;
import cn.smartjavaai.face.vector.config.MilvusConfig;
import cn.smartjavaai.face.vector.config.SQLiteConfig;
import cn.smartjavaai.face.vector.constant.VectorDBConstants;
import cn.smartjavaai.face.vector.core.VectorDBClient;
import cn.smartjavaai.face.vector.core.VectorDBFactory;
import cn.smartjavaai.face.vector.entity.FaceVector;
import cn.smartjavaai.face.vector.exception.VectorDBException;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * FaceNet 人脸特征提取模型
 * @author dwj
 */
@Slf4j
public class FaceNetModel implements FaceModel, AutoCloseable{

    /**
     * 特征维度
     */
    private static final int DIMENSION = 512;

    /**
     * 是否加载人脸库完毕
     */
    private static volatile boolean isLoadCompleted = false;


    private ObjectPool<Predictor<Image, float[]>> predictorPool;


    private ZooModel<Image, float[]> model;

    private FaceModelConfig config;

    /**
     * 是否归一化相似度
     */
    public static final boolean NORMALIZE_SIMILARITY = true;


    public static final List<Float> mean =
            Arrays.asList(
                    127.5f / 255.0f,
                    127.5f / 255.0f,
                    127.5f / 255.0f,
                    128.0f / 255.0f,
                    128.0f / 255.0f,
                    128.0f / 255.0f);

    private VectorDBClient vectorDBClient;


    /**
     * 加载人脸特征提取模型
     * @param config
     */
    @Override
    public void loadModel(FaceModelConfig config) {
        if(Objects.isNull(config)){
            throw new FaceException("config为null");
        }
        if(Objects.isNull(config.getExtractConfig())){
            config.setExtractConfig(getDefaultConfig());
        }else{
            if(Objects.isNull(config.getExtractConfig().getDetectModel())){
                throw new FaceException("请设置人脸检测模型");
            }
        }
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        this.config = config;
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

        //初始化人脸库
        if(config.getVectorDBConfig() != null && config.getVectorDBConfig().getType() != null){
            if(config.getVectorDBConfig() instanceof MilvusConfig){
                MilvusConfig milvusConfig = ((MilvusConfig) config.getVectorDBConfig());
                //设置向量维度
                milvusConfig.setDimension(DIMENSION);
                //相似度计算方式 为空，设置默认值
                if(Objects.isNull(milvusConfig.getMetricType())){
                    //FaceNet 默认使用内积
                    milvusConfig.setMetricType(MetricType.IP);
                }

            }else if (config.getVectorDBConfig() instanceof SQLiteConfig){
                SQLiteConfig  sqliteConfig = (SQLiteConfig) config.getVectorDBConfig();
                if(Objects.isNull(sqliteConfig.getSimilarityType())){
                    //seetaface6 默认使用内积
                    sqliteConfig.setSimilarityType(SimilarityType.IP);
                }
            }
            vectorDBClient = VectorDBFactory.createClient(config.getVectorDBConfig());

            // 加载人脸数据库
            if(config.isAutoLoadFace()){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.debug("start load face...");
                            vectorDBClient.initialize();
                            isLoadCompleted = true;
                            log.debug("Load face success!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
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
     * 计算相似度，返回归一化结果
     * @param feature1 图1特征
     * @param feature2 图2特征
     * @return
     */
    @Override
    public float calculSimilar(float[] feature1, float[] feature2) {
        //默认返回归一化结果
        return SimilarityUtil.calculate(feature1, feature2, SimilarityType.IP, NORMALIZE_SIMILARITY);
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
        R<float[]> feature1 = extractTopFaceFeature(imagePath1);
        if (!feature1.isSuccess()){
            throw new FaceException(feature1.getMessage());
        }
        R<float[]> feature2 = extractTopFaceFeature(imagePath2);
        if (!feature2.isSuccess()){
            throw new FaceException(feature2.getMessage());
        }
        float ret = calculSimilar(feature1.getData(), feature2.getData());
        return ret;
    }


    @Override
    public float featureComparison(BufferedImage sourceImage1, BufferedImage sourceImag2) {
        if(!ImageUtils.isImageValid(sourceImage1) || !ImageUtils.isImageValid(sourceImag2)){
            throw new FaceException("图像无效");
        }
        R<float[]> feature1 = extractTopFaceFeature(sourceImage1);
        if (!feature1.isSuccess()){
            throw new FaceException(feature1.getMessage());
        }
        R<float[]> feature2 = extractTopFaceFeature(sourceImag2);
        if (!feature2.isSuccess()){
            throw new FaceException(feature2.getMessage());
        }
        float ret = calculSimilar(feature1.getData(), feature2.getData());
        return ret;
    }

    @Override
    public float featureComparison(byte[] imageData1, byte[] imageData2) {
        if(Objects.isNull(imageData1) || Objects.isNull(imageData2)){
            throw new FaceException("图像无效");
        }
        R<float[]> feature1 = extractTopFaceFeature(imageData1);
        if (!feature1.isSuccess()){
            throw new FaceException(feature1.getMessage());
        }
        R<float[]> feature2 = extractTopFaceFeature(imageData2);
        if (!feature2.isSuccess()){
            throw new FaceException(feature2.getMessage());
        }
        float ret = calculSimilar(feature1.getData(), feature2.getData());
        return ret;
    }

    /**
     * 获取默认特征提取配置
     * @return
     */
    private FaceExtractConfig getDefaultConfig() {
        FaceExtractConfig config = new FaceExtractConfig();
        FaceModelConfig detectModelConfig = new FaceModelConfig();
        detectModelConfig.setModelEnum(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);
        detectModelConfig.setConfidenceThreshold(0.98);
        log.debug("创建默认检测模型：ULTRA_LIGHT_FAST_GENERIC_FACE");
        FaceModel detectModel = FaceModelFactory.getInstance().getModel(detectModelConfig);
        log.debug("创建检测模型完毕");
        config.setDetectModel(detectModel);
        return config;
    }

    @Override
    public R<DetectionResponse> extractFeatures(BufferedImage image) {
        DetectionResponse detectedResult = config.getExtractConfig().getDetectModel().detect(image);
        if(Objects.isNull(detectedResult) || Objects.isNull(detectedResult.getDetectionInfoList()) || detectedResult.getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        Image djlImage = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        NDManager manager = NDManager.newBaseManager();
        for (DetectionInfo detectionInfo : detectedResult.getDetectionInfoList()){
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            FaceInfo faceInfo = detectionInfo.getFaceInfo();
            float[] features = null;
            //裁剪人脸
            Image subImage = djlImage.getSubImage(rectangle.getX(), rectangle.getY() , rectangle.getWidth() , rectangle.getHeight());
            //人脸对齐
            if(config.getExtractConfig().isAlign()){
                //获取子图中人脸关键点坐标
                double[][] pointsArray = FaceUtils.facePoints(detectionInfo.getFaceInfo().getKeyPoints());
                NDArray srcPoints = manager.create(pointsArray);
                NDArray dstPoints = FaceUtils.faceTemplate512x512(manager);
                // 5点仿射变换
                Mat affine_matrix = OpenCVUtils.toOpenCVMat(manager, srcPoints, dstPoints);
                Mat mat = FaceAlignUtils.warpAffine((Mat) djlImage.getWrappedImage(), affine_matrix);
                Image alignedImg = OpenCVImageFactory.getInstance().fromImage(mat);
                features = featureExtraction(alignedImg);
            }else{
                //不对齐人脸
                features = featureExtraction(subImage);
            }
            faceInfo.setFeature(features);
        }
        return R.ok(detectedResult);
    }


    @Override
    public R<DetectionResponse> extractFeatures(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return extractFeatures(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<DetectionResponse> extractFeatures(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return extractFeatures(image);
    }

    @Override
    public R<float[]> extractTopFaceFeature(BufferedImage image) {
        Image djlImage = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        float[] features = null;
        if(config.getExtractConfig().isCropFace()){
            DetectionResponse detectedResult = config.getExtractConfig().getDetectModel().detect(image);
            if(Objects.isNull(detectedResult) || Objects.isNull(detectedResult.getDetectionInfoList()) || detectedResult.getDetectionInfoList().isEmpty()){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            //只取第一个人脸
            DetectionInfo detectionInfo = detectedResult.getDetectionInfoList().get(0);
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            //裁剪人脸
            Image subImage = djlImage.getSubImage(rectangle.getX(), rectangle.getY() , rectangle.getWidth() , rectangle.getHeight());
            //人脸对齐
            if(config.getExtractConfig().isAlign()){
                NDManager manager = NDManager.newBaseManager();
                //获取子图中人脸关键点坐标
                double[][] pointsArray = FaceUtils.facePoints(detectionInfo.getFaceInfo().getKeyPoints());
                NDArray srcPoints = manager.create(pointsArray);
                NDArray dstPoints = FaceUtils.faceTemplate512x512(manager);
                // 5点仿射变换
                Mat affine_matrix = OpenCVUtils.toOpenCVMat(manager, srcPoints, dstPoints);
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
        return Objects.isNull(features) ? R.fail(R.Status.Unknown) : R.ok(features);
    }

    @Override
    public R<float[]> extractTopFaceFeature(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return extractTopFaceFeature(image);
    }

    @Override
    public R<float[]> extractTopFaceFeature(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return extractTopFaceFeature(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }



    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return register(faceRegisterInfo, bufferedImage);
    }


    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, BufferedImage sourceImage) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        //提取最大人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(sourceImage);
        if(!featureResponse.isSuccess()){
            return R.fail(featureResponse.getCode(), featureResponse.getMessage());
        }
        return register(faceRegisterInfo, featureResponse.getData());
    }


    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, InputStream inputStream) {
        if(Objects.isNull(inputStream)){
            throw new FaceException("图像输入流无效");
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }
        return register(faceRegisterInfo, image);
    }

    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return register(faceRegisterInfo, ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, float[] feature) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        if(Objects.isNull(feature)){
            throw new FaceException("人脸特征为空");
        }
        FaceVector faceVector = new FaceVector();
        if(faceRegisterInfo != null){
            faceVector.setId(faceRegisterInfo.getId());
            faceVector.setMetadata(faceRegisterInfo.getMetadata());
        }
        faceVector.setVector(feature);
        return R.ok(vectorDBClient.insert(faceVector));
    }

    @Override
    public void removeRegister(String... keys) {
        vectorDBClient.deleteBatch(Arrays.asList(keys));
    }

    @Override
    public void clearFace() {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        vectorDBClient.dropCollection(VectorDBConstants.Defaults.DEFAULT_COLLECTION_NAME);
    }



    @Override
    public void upsertFace(FaceRegisterInfo faceRegisterInfo, String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        upsertFace(faceRegisterInfo, bufferedImage);
    }


    @Override
    public void upsertFace(FaceRegisterInfo faceRegisterInfo, BufferedImage sourceImage) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        if(Objects.isNull(faceRegisterInfo)){
            throw new FaceException("注册信息为空");
        }
        if(StringUtils.isBlank(faceRegisterInfo.getId())){
            throw new FaceException("注册信息中ID为空");
        }
        //提取最大人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(sourceImage);
        if(!featureResponse.isSuccess()){
            throw new FaceException(featureResponse.getMessage());
        }
        upsertFace(faceRegisterInfo, featureResponse.getData());
    }

    @Override
    public void upsertFace(FaceRegisterInfo faceRegisterInfo, float[] feature) {
        if(Objects.isNull(feature)){
            throw new FaceException("人脸特征为空");
        }
        if(Objects.isNull(vectorDBClient)){
            throw new FaceException("未初始化人脸库");
        }
        FaceVector faceVector = new FaceVector();
        if(faceRegisterInfo != null){
            faceVector.setId(faceRegisterInfo.getId());
            faceVector.setMetadata(faceRegisterInfo.getMetadata());
        }
        faceVector.setVector(feature);
        vectorDBClient.upsert(faceVector);
    }


    @Override
    public void upsertFace(FaceRegisterInfo faceRegisterInfo, byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            upsertFace(faceRegisterInfo, ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<List<FaceSearchResult>> searchByTopFace(String imagePath, FaceSearchParams params) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return searchByTopFace(bufferedImage, params);
    }

    @Override
    public R<List<FaceSearchResult>> searchByTopFace(byte[] imageData, FaceSearchParams params) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return searchByTopFace(ImageIO.read(new ByteArrayInputStream(imageData)), params);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<List<FaceSearchResult>> searchByTopFace(BufferedImage sourceImage, FaceSearchParams params) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        //提取最大人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(sourceImage);
        if(!featureResponse.isSuccess()){
            return R.fail(featureResponse.getCode(), featureResponse.getMessage());
        }
        return R.ok(search(featureResponse.getData(), params));
    }



    @Override
    public List<FaceSearchResult> search(float[] feature, FaceSearchParams params) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        if(Objects.isNull(feature)){
            throw new FaceException("人脸特征为空");
        }
        if(Objects.isNull(params)){
            throw new FaceException("人脸查询参数为空");
        }
        //设置默认值
        float threshold = Objects.isNull(params.getThreshold()) ? FaceDetectConstant.FACENET_DEFAULT_SIMILARITY_THRESHOLD : params.getThreshold();
        int topK = Objects.isNull(params.getTopK()) ? 1 : params.getTopK();
        boolean normalize = Objects.isNull(params.getNormalizeSimilarity()) ? NORMALIZE_SIMILARITY : params.getNormalizeSimilarity();
        FaceSearchParams searchParams = new FaceSearchParams(topK, threshold, normalize);
        List<FaceSearchResult> searchResults = vectorDBClient.search(feature, searchParams);
        return searchResults;
    }

    @Override
    public R<DetectionResponse> search(String imagePath, FaceSearchParams params) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return search(bufferedImage, params);
    }

    @Override
    public R<DetectionResponse> search(BufferedImage sourceImage, FaceSearchParams params) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        //提取所有人脸特征
        R<DetectionResponse> detectionResponse = extractFeatures(sourceImage);
        if(!detectionResponse.isSuccess()){
            return detectionResponse;
        }
        //设置默认值
        float threshold = Objects.isNull(params.getThreshold()) ? FaceDetectConstant.FACENET_DEFAULT_SIMILARITY_THRESHOLD : params.getThreshold();
        int topK = Objects.isNull(params.getTopK()) ? 1 : params.getTopK();
        boolean normalize = Objects.isNull(params.getNormalizeSimilarity()) ? NORMALIZE_SIMILARITY : params.getNormalizeSimilarity();
        FaceSearchParams searchParams = new FaceSearchParams(topK, threshold, normalize);
        for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
            if(Objects.nonNull(detectionInfo.getFaceInfo()) && Objects.nonNull(detectionInfo.getFaceInfo().getFeature())){
                List<FaceSearchResult> searchResults = vectorDBClient.search(detectionInfo.getFaceInfo().getFeature(), searchParams);
                detectionInfo.getFaceInfo().setFaceSearchResults(searchResults);
            }
        }
        return detectionResponse;
    }

    @Override
    public R<DetectionResponse> search(byte[] imageData, FaceSearchParams params) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return search(ImageIO.read(new ByteArrayInputStream(imageData)), params);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public void loadFaceFeatures() {
        if(Objects.isNull(vectorDBClient)){
            throw new FaceException("未初始化人脸库");
        }
        vectorDBClient.loadFaceFeatures();
    }

    @Override
    public void releaseFaceFeatures() {
        if(Objects.isNull(vectorDBClient)){
            throw new FaceException("未初始化人脸库");
        }
        vectorDBClient.releaseFaceFeatures();
    }


    @Override
    public void close() {
        if (predictorPool != null) {
            predictorPool.close();
        }
        if(Objects.nonNull(vectorDBClient)){
            vectorDBClient.close();
        }
    }

    @Override
    public boolean isLoadFaceCompleted() {
        return isLoadCompleted;
    }



}

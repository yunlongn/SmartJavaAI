package cn.smartjavaai.face.model.expression;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.face.ExpressionResult;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.face.FacialExpression;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.Base64ImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.face.config.FaceExpressionConfig;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.expression.criterial.EmotionCriteriaFactory;
import cn.smartjavaai.face.model.expression.translator.DenseNetEmotionTranslator;
import cn.smartjavaai.face.preprocess.DJLImagePreprocessor;
import cn.smartjavaai.face.utils.FaceUtils;
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
import java.util.List;
import java.util.Objects;

/**
 * 通用人脸表情识别模型
 * @author dwj
 */
@Slf4j
public class CommonEmotionModel implements ExpressionModel{


    private FaceExpressionConfig config;

    private ZooModel<Image, Classifications> model;

    private GenericObjectPool<Predictor<Image, Classifications>> predictorPool;

    @Override
    public void loadModel(FaceExpressionConfig config) {
        if(Objects.isNull(config)){
            throw new FaceException("config为null");
        }
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath为空");
        }

        this.config = config;

        Criteria<Image, Classifications> criteria = EmotionCriteriaFactory.createCriteria(config);
        try {
            model = criteria.loadModel();
            this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(model));
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            predictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + model.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new FaceException("DenseNetEmotionModel模型加载失败", e);
        }
    }

    public Classifications detectCore(Image image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        Predictor<Image, Classifications> predictor = null;
        try (NDManager manager = model.getNDManager().newSubManager()){
            predictor = predictorPool.borrowObject();
            DJLImagePreprocessor imagePreprocessor = new DJLImagePreprocessor(image, manager);
            Image faceImg = image;
            if(config.isAlign()){
                //仿射变换
                faceImg = imagePreprocessor.enableAffine(FaceUtils.facePoints(keyPoints), 512, 512)
                        .process();
                return predictor.predict(faceImg);
            }else{
                if(config.isCropFace()){
                    //裁剪
                    faceImg = imagePreprocessor.enableCrop(faceDetectionRectangle)
                            .process();
                }
            }
            return predictor.predict(faceImg);
        } catch (Exception e) {
            throw new FaceException("表情识别异常", e);
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

    @Override
    public R<DetectionResponse> detect(String imagePath) {
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
        return detect(image);
    }

    @Override
    public R<DetectionResponse> detect(BufferedImage image) {
        if(Objects.isNull(config.getDetectModel())){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "未指定检测模型");
        }
        R<DetectionResponse> faceDetectionResponse = config.getDetectModel().detect(image);
        if(Objects.isNull(faceDetectionResponse.getData()) || Objects.isNull(faceDetectionResponse.getData().getDetectionInfoList()) || faceDetectionResponse.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        Image djlImage = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        for(DetectionInfo detectionInfo : faceDetectionResponse.getData().getDetectionInfoList()){
            FaceInfo faceInfo = detectionInfo.getFaceInfo();
            if(Objects.isNull(faceInfo) || Objects.isNull(faceInfo.getKeyPoints())){
                return R.fail(R.Status.Unknown.getCode(), "未检测到人脸关键点");
            }
            Classifications classifications = detectCore(djlImage, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
            Classifications.Classification bestClass = classifications.best();
            FacialExpression expression = FacialExpression.fromLabel(bestClass.getClassName());
            ExpressionResult result = new ExpressionResult(expression, (float)bestClass.getProbability());
            result.setClassifications(classifications);
            faceInfo.setExpressionResult(result);
        }
        ((Mat)djlImage.getWrappedImage()).release();
        return faceDetectionResponse;
    }

    @Override
    public R<DetectionResponse> detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<DetectionResponse> detectBase64(String base64Image) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detect(imageData);
    }

    @Override
    public R<List<ExpressionResult>> detect(String imagePath, DetectionResponse faceDetectionResponse) {
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
        return detect(image, faceDetectionResponse);
    }

    @Override
    public R<List<ExpressionResult>> detect(byte[] imageData, DetectionResponse faceDetectionResponse) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)), faceDetectionResponse);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<List<ExpressionResult>> detect(BufferedImage image, DetectionResponse faceDetectionResponse) {
        if(!ImageUtils.isImageValid(image)){
            R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(faceDetectionResponse) || Objects.isNull(faceDetectionResponse.getDetectionInfoList()) || faceDetectionResponse.getDetectionInfoList().isEmpty()){
            R.fail(R.Status.NO_FACE_DETECTED);
        }
        Image djlImage = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        List<ExpressionResult> expressionResults = new ArrayList<>();
        for(DetectionInfo detectionInfo : faceDetectionResponse.getDetectionInfoList()){
            FaceInfo faceInfo = detectionInfo.getFaceInfo();
            if(Objects.isNull(faceInfo) || Objects.isNull(faceInfo.getKeyPoints())){
                return R.fail(R.Status.Unknown.getCode(), "未检测到人脸关键点");
            }
            Classifications classifications = detectCore(djlImage, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
            Classifications.Classification bestClass = classifications.best();
            FacialExpression expression = FacialExpression.fromLabel(bestClass.getClassName());
            ExpressionResult result = new ExpressionResult(expression, (float)bestClass.getProbability());
            result.setClassifications(classifications);
            expressionResults.add(result);
        }
        ((Mat)djlImage.getWrappedImage()).release();
        return R.ok(expressionResults);
    }

    @Override
    public R<List<ExpressionResult>> detectBase64(String base64Image, DetectionResponse faceDetectionResponse) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detect(imageData, faceDetectionResponse);
    }

    @Override
    public R<ExpressionResult> detect(String imagePath, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
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
        return detect(image, faceDetectionRectangle, keyPoints);
    }

    @Override
    public R<ExpressionResult> detect(byte[] imageData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)), faceDetectionRectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<ExpressionResult> detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image djlImage = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        Classifications classifications = detectCore(djlImage, faceDetectionRectangle, keyPoints);
        Classifications.Classification bestClass = classifications.best();
        FacialExpression expression = FacialExpression.fromLabel(bestClass.getClassName());
        ExpressionResult result = new ExpressionResult(expression, (float)bestClass.getProbability());
        result.setClassifications(classifications);
        ((Mat)djlImage.getWrappedImage()).release();
        return R.ok(result);

    }

    @Override
    public R<ExpressionResult> detectBase64(String base64Image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detect(imageData, faceDetectionRectangle, keyPoints);
    }

    @Override
    public R<ExpressionResult> detectTopFace(BufferedImage image) {
        if(Objects.isNull(config.getDetectModel())){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "未指定检测模型");
        }
        R<DetectionResponse> faceDetectionResponse = config.getDetectModel().detect(image);
        if(Objects.isNull(faceDetectionResponse.getData()) || Objects.isNull(faceDetectionResponse.getData().getDetectionInfoList()) || faceDetectionResponse.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        DetectionInfo detectionInfo = faceDetectionResponse.getData().getDetectionInfoList().get(0);
        FaceInfo faceInfo = detectionInfo.getFaceInfo();
        if(Objects.isNull(faceInfo) || Objects.isNull(faceInfo.getKeyPoints())){
            return R.fail(R.Status.Unknown.getCode(), "未检测到人脸关键点");
        }
        return detect(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getKeyPoints());
    }

    @Override
    public R<ExpressionResult> detectTopFace(String imagePath) {
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
        return detectTopFace(image);
    }

    @Override
    public R<ExpressionResult> detectTopFace(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return detectTopFace(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<ExpressionResult> detectTopFaceBase64(String base64Image) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detectTopFace(imageData);
    }

    @Override
    public GenericObjectPool<Predictor<Image, Classifications>> getPool() {
        return predictorPool;
    }

    @Override
    public void close() {
        try {
            if (predictorPool != null) {
                predictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (model != null) {
                model.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
    }
}

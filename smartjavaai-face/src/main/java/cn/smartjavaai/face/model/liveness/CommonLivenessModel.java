package cn.smartjavaai.face.model.liveness;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.preprocess.BufferedImagePreprocessor;
import cn.smartjavaai.common.utils.*;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.face.constant.MiniVisionConstant;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.liveness.criterial.LivenessCriteriaFactory;
import cn.smartjavaai.face.model.liveness.translator.MiniVisionTranslator;
import com.seeta.sdk.FaceAntiSpoofing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.util.*;

/**
 * 通用活体检测模型
 * @author dwj
 */
@Slf4j
public class CommonLivenessModel implements LivenessDetModel{

    protected GenericObjectPool<Predictor<Image, Float>> predictorPool;

    protected LivenessConfig config;

    protected ZooModel<Image, Float> model;
    @Override
    public void loadModel(LivenessConfig config) {
        if(Objects.isNull(config)){
            throw new FaceException("config为null");
        }
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath不能为空");
        }
        this.config = config;
        //设置真人阈值
        Float realityThreshold = Objects.isNull(config.getRealityThreshold()) ? MiniVisionConstant.REALITY_THRESHOLD : config.getRealityThreshold();
        this.config.setRealityThreshold(realityThreshold);
        Criteria<Image, Float> criteria = LivenessCriteriaFactory.createCriteria(config);
        try {
            model = criteria.loadModel();
            this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(model));
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new FaceException("阿里通义实验室活体检测模型加载失败", e);
        }

        int predictorPoolSize = config.getPredictorPoolSize();
        if(config.getPredictorPoolSize() <= 0){
            predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
        }
        predictorPool.setMaxTotal(predictorPoolSize);
        log.debug("当前设备: " + model.getNDManager().getDevice());
        log.debug("当前引擎: " + Engine.getInstance().getEngineName());
        log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
    }


    @Override
    public R<LivenessResult> detect(BufferedImage image, DetectionRectangle faceDetectionRectangle) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        Predictor<Image, Float> predictor = null;
        Image djlImage = null;
        try {
            predictor = predictorPool.borrowObject();
            //预处理图片
            BufferedImage processedImage = image;
            if(config.getModelEnum() == LivenessModelEnum.IIC_FL_MODEL){
                processedImage = new BufferedImagePreprocessor(image, faceDetectionRectangle)
                        .setExtendRatio(96f / 112f)
                        .enableSquarePadding(true)
                        .enableScaling(true)
                        .setTargetSize(128)
                        .enableCenterCrop(true)
                        .setCenterCropSize(112)
                        .process();
            }
            djlImage = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(processedImage));
            Float result = predictor.predict(djlImage);
            if(result >= config.getRealityThreshold()){
                return R.ok(new LivenessResult(LivenessStatus.LIVE, result));
            }else{
                float nonLiveScore = BigDecimal.ONE.subtract(new BigDecimal(result)).floatValue();
                return R.ok(new LivenessResult(LivenessStatus.NON_LIVE, nonLiveScore));
            }
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
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
            if (djlImage != null){
                ((Mat)djlImage.getWrappedImage()).release();
            }

        }
    }

    @Override
    public R<LivenessResult> detect(String imagePath, DetectionRectangle faceDetectionRectangle) {
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
        return detect(image, faceDetectionRectangle);
    }

    @Override
    public R<LivenessResult> detect(byte[] imageData, DetectionRectangle faceDetectionRectangle) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)), faceDetectionRectangle);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<LivenessResult> detectBase64(String base64Image, DetectionRectangle faceDetectionRectangle) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detect(imageData, faceDetectionRectangle);
    }

    @Override
    public R<List<LivenessResult>> detect(String imagePath, DetectionResponse faceDetectionResponse) {
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
    public R<List<LivenessResult>> detect(byte[] imageData, DetectionResponse faceDetectionResponse) {
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
    public R<List<LivenessResult>> detect(BufferedImage image, DetectionResponse faceDetectionResponse) {
        if(!ImageUtils.isImageValid(image)){
            R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(faceDetectionResponse) || Objects.isNull(faceDetectionResponse.getDetectionInfoList()) || faceDetectionResponse.getDetectionInfoList().isEmpty()){
            R.fail(R.Status.NO_FACE_DETECTED);
        }
        List<LivenessResult> livenessStatusList = new ArrayList<LivenessResult>();
        for(DetectionInfo detectionInfo : faceDetectionResponse.getDetectionInfoList()){
            R<LivenessResult> result = detect(image, detectionInfo.getDetectionRectangle());
            if(!result.isSuccess()){
                return R.fail(result.getCode(), result.getMessage());
            }
            livenessStatusList.add(result.getData());
        }
        return R.ok(livenessStatusList);
    }

    @Override
    public R<List<LivenessResult>> detectBase64(String base64Image, DetectionResponse faceDetectionResponse) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detect(imageData, faceDetectionResponse);
    }


    @Override
    public R<LivenessResult> detectTopFace(BufferedImage image) {
        if(Objects.isNull(config.getDetectModel())){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "未指定检测模型");
        }
        R<DetectionResponse> faceDetectionResponse = config.getDetectModel().detect(image);
        if(Objects.isNull(faceDetectionResponse.getData()) || Objects.isNull(faceDetectionResponse.getData().getDetectionInfoList()) || faceDetectionResponse.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        return detect(image, faceDetectionResponse.getData().getDetectionInfoList().get(0).getDetectionRectangle());
    }

    @Override
    public R<LivenessResult> detectTopFace(String imagePath) {
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
    public R<LivenessResult> detectTopFace(byte[] imageData) {
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
    public R<LivenessResult> detectTopFaceBase64(String base64Image) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detectTopFace(imageData);
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
        for(DetectionInfo detectionInfo : faceDetectionResponse.getData().getDetectionInfoList()){
            R<LivenessResult> result = detect(image, detectionInfo.getDetectionRectangle());
            if(!result.isSuccess()){
                return R.fail(result.getCode(), result.getMessage());
            }
            if(Objects.isNull(detectionInfo.getFaceInfo())){
                detectionInfo.setFaceInfo(new FaceInfo());
            }
            detectionInfo.getFaceInfo().setLivenessStatus(result.getData());
        }
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
    public R<LivenessResult> detectVideo(InputStream videoInputStream) {
        if(Objects.isNull(videoInputStream)){
            return R.fail(R.Status.INVALID_VIDEO);
        }
        return detectVideo(new FFmpegFrameGrabber(videoInputStream));
    }

    @Override
    public R<LivenessResult> detectVideo(String videoPath) {
        if(!FileUtils.isFileExists(videoPath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        return detectVideo(new FFmpegFrameGrabber(videoPath));
    }

    private R<LivenessResult> detectVideo(FFmpegFrameGrabber grabber) {
        try {
            //滑动窗口
            Deque<Float> scoreWindow = new ArrayDeque<>();
            grabber.start();
            // 获取视频总帧数
            int totalFrames = grabber.getLengthInFrames();
            log.debug("视频总帧数：{}，检测帧数：{}", totalFrames, config.getFrameCount());
            if(totalFrames < config.getFrameCount()){
                return R.fail(10001, "视频帧数低于检测帧数");
            }
            // 逐帧处理视频
            for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
                if(frameIndex >= config.getMaxVideoDetectFrames()){
                    return R.fail(10002, "超出最大检测帧数：" + config.getMaxVideoDetectFrames());
                }
                // 获取当前帧
                Frame frame = grabber.grabImage();
                if (frame != null) {
                    BufferedImage bufferedImage = Java2DFrameUtils.toBufferedImage(frame);
                    R<LivenessResult> livenessStatus = detectTopFace(bufferedImage);
                    if(!livenessStatus.isSuccess()){
                        log.debug("第" + frameIndex + "帧处理失败：" + livenessStatus.getMessage());
                        continue;
                    }else{
                        log.debug("第" + frameIndex + "帧活体检测结果：" + JsonUtils.toJson(livenessStatus));
                        float liveScore = 0;
                        if(livenessStatus.getData().getStatus() == LivenessStatus.LIVE){
                            liveScore = livenessStatus.getData().getScore();
                        }else{
                            liveScore = BigDecimal.ONE.subtract(BigDecimal.valueOf(livenessStatus.getData().getScore())).floatValue();
                        }
                        scoreWindow.add(liveScore);
                    }
                    // 如果累计检测帧数 >= 配置值，开始判断
                    if (scoreWindow.size() >= config.getFrameCount()) {
                        float avgScore = (float) scoreWindow.stream()
                                .mapToDouble(Float::doubleValue)
                                .average()
                                .orElse(0.0);
                        log.debug("滑动窗口平均得分: {}", avgScore);
                        if (avgScore >= config.getRealityThreshold()) {
                            grabber.stop();
                            return R.ok(new LivenessResult(LivenessStatus.LIVE, avgScore));
                        } else {
                            grabber.stop();
                            float nonLiveScore = BigDecimal.ONE.subtract(BigDecimal.valueOf(avgScore)).floatValue();
                            return R.ok(new LivenessResult(LivenessStatus.NON_LIVE, nonLiveScore));
                        }
                    }
                }
            }
            grabber.stop();
            if(scoreWindow.size() < config.getFrameCount()){
                return R.fail(1000, "有效帧数量不足，无法完成活体检测");
            }
        } catch (Exception e) {
            throw new FaceException(e);
        }
        return R.fail(R.Status.Unknown);
    }


    @Override
    public GenericObjectPool<Predictor<Image, Float>> getPool() {
        return predictorPool;
    }

    @Override
    public void close() throws Exception {
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

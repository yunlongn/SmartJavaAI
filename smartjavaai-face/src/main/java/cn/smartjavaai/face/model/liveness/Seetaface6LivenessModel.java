package cn.smartjavaai.face.model.liveness;

import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.face.constant.LivenessConstant;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.seetaface.NativeLoader;
import cn.smartjavaai.face.utils.FaceUtils;
import com.seeta.pool.*;
import com.seeta.sdk.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * seetaface6 活体检测模型
 * @author dwj
 * @date 2025/4/30
 */
@Slf4j
public class Seetaface6LivenessModel implements LivenessDetModel{


    private FaceDetectorPool faceDetectorPool;
    private FaceAntiSpoofingPool faceAntiSpoofingPool;
    private FaceLandmarkerPool faceLandmarkerPool;

    private LivenessConfig config;

    @Override
    public void loadModel(LivenessConfig config) {
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
        this.config = config;
        //加载依赖库
        NativeLoader.loadNativeLibraries(config.getDevice());
        log.debug("Loading seetaFace6 library successfully.");
        String[] faceDetectorModelPath = {config.getModelPath() + File.separator + "face_detector.csta"};
        String[] faceAntiSpoofingModelPath = {config.getModelPath() + File.separator + "fas_first.csta",config.getModelPath() + File.separator + "fas_second.csta"};
        String[] faceLandmarkerModelPath = {config.getModelPath() + File.separator + "face_landmarker_pts5.csta"};
        SeetaDevice device = SeetaDevice.SEETA_DEVICE_AUTO;
        int gpuId = 0;
        if(Objects.nonNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? SeetaDevice.SEETA_DEVICE_CPU : SeetaDevice.SEETA_DEVICE_GPU;
            Integer gpuIdValue = config.getCustomParam("gpuId", Integer.class);
            if(Objects.nonNull(gpuIdValue) && device == SeetaDevice.SEETA_DEVICE_GPU){
                gpuId = gpuIdValue;
            }
        }

        try {
            SeetaModelSetting faceDetectorPoolSetting = new SeetaModelSetting(gpuId, faceDetectorModelPath, device);
            SeetaConfSetting faceDetectorPoolConfSetting = new SeetaConfSetting(faceDetectorPoolSetting);

            SeetaModelSetting faceLandmarkerPoolSetting = new SeetaModelSetting(gpuId, faceLandmarkerModelPath, device);
            SeetaConfSetting faceLandmarkerPoolConfSetting = new SeetaConfSetting(faceLandmarkerPoolSetting);

            SeetaModelSetting faceAntiSpoofingSetting = new SeetaModelSetting(gpuId, faceAntiSpoofingModelPath, device);
            SeetaConfSetting faceAntiSpoofingPoolConfSetting = new SeetaConfSetting(faceAntiSpoofingSetting);

            this.faceDetectorPool = new FaceDetectorPool(faceDetectorPoolConfSetting);
            this.faceAntiSpoofingPool = new FaceAntiSpoofingPool(faceAntiSpoofingPoolConfSetting);
            this.faceLandmarkerPool = new FaceLandmarkerPool(faceLandmarkerPoolConfSetting);

            //初始化模型参数
            initConfig();
        } catch (FileNotFoundException e) {
            throw new FaceException(e);
        }
    }

    /**
     * 初始化模型参数
     */
    private void initConfig(){
        FaceAntiSpoofing faceAntiSpoofing = null;
        //设置参数
        try {
            //人脸清晰度阈值
            float faceClarityThreshold = LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD;
            //活体阈值
            float realityThreshold = LivenessConstant.DEFAULT_REALITY_THRESHOLD;
            Float faceClarityThresholdValue = config.getCustomParam("faceClarityThreshold", Float.class);
            if(Objects.nonNull(faceClarityThresholdValue)){
                faceClarityThreshold = faceClarityThresholdValue;
            }
            Float realityThresholdValue = config.getRealityThreshold();
            if(Objects.nonNull(realityThresholdValue)){
                realityThreshold = realityThresholdValue;
            }
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            faceAntiSpoofing.SetThreshold(faceClarityThreshold, realityThreshold);
            faceAntiSpoofing.SetVideoFrameCount(config.getFrameCount());
        } catch (Exception e) {
            throw new FaceException(e);
        } finally {
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }


    private R<LivenessResult> detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints, boolean isImage) {
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(faceDetectionRectangle)){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        if(keyPoints == null || keyPoints.isEmpty()){
            return R.fail(1002,"人脸关键点keyPoints为空");
        }
        FaceAntiSpoofing.Status status = null;
        FaceAntiSpoofing faceAntiSpoofing = null;
        try {
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = FaceUtils.convertToSeetaRect(faceDetectionRectangle);
            SeetaPointF[] landmarks = FaceUtils.convertToSeetaPointF(keyPoints);
            //检测图片
            if(isImage){
                status = faceAntiSpoofing.Predict(imageData, seetaRect, landmarks);
            }else{
                //检测视频
                status = faceAntiSpoofing.PredictVideo(imageData, seetaRect, landmarks);
            }
            return R.ok(new LivenessResult(FaceUtils.convertToLivenessStatus(status)));
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
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
    public R<DetectionResponse> detect(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        FaceAntiSpoofing faceAntiSpoofing = null;
        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        try {
            //默认使用Seetaface6检测模型
            if(Objects.isNull(config.getDetectModel())){
                faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
                detectPredictor = faceDetectorPool.borrowObject();
                faceLandmarker = faceLandmarkerPool.borrowObject();
                List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
                List<LivenessStatus> livenessStatusList = new ArrayList<LivenessStatus>();
                SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
                imageData.data = ImageUtils.getMatrixBGR(image);
                //检测人脸
                SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
                if(Objects.isNull(seetaResult)){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                for(SeetaRect seetaRect : seetaResult){
                    SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
                    faceLandmarker.mark(imageData, seetaRect, landmarks);
                    seetaPointFSList.add(landmarks);
                    //检测图片
                    FaceAntiSpoofing.Status status = faceAntiSpoofing.Predict(imageData, seetaRect, landmarks);
                    livenessStatusList.add(FaceUtils.convertToLivenessStatus(status));
                }
                return R.ok(FaceUtils.convertToDetectionResponse(seetaResult, seetaPointFSList, livenessStatusList));
            }else{
                R<DetectionResponse> faceDetectionResponse = config.getDetectModel().detect(image);
                if(Objects.isNull(faceDetectionResponse.getData()) || Objects.isNull(faceDetectionResponse.getData().getDetectionInfoList()) || faceDetectionResponse.getData().getDetectionInfoList().isEmpty()){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                for(DetectionInfo detectionInfo : faceDetectionResponse.getData().getDetectionInfoList()){
                    R<LivenessResult> result = detect(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getKeyPoints());
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
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            if (detectPredictor != null) {
                try {
                    faceDetectorPool.returnObject(detectPredictor);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
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
    public R<List<LivenessResult>> detect(BufferedImage image, DetectionResponse faceDetectionResponse) {
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(faceDetectionResponse) || Objects.isNull(faceDetectionResponse.getDetectionInfoList()) || faceDetectionResponse.getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        List<LivenessResult> livenessStatusList = new ArrayList<LivenessResult>();
        for(DetectionInfo detectionInfo : faceDetectionResponse.getDetectionInfoList()){
            FaceInfo faceInfo = detectionInfo.getFaceInfo();
            if(Objects.isNull(faceInfo) || Objects.isNull(faceInfo.getKeyPoints())){
                return R.fail(R.Status.Unknown.getCode(), "未检测到人脸关键点");
            }
            R<LivenessResult> result = detect(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getKeyPoints());
            if(!result.isSuccess()){
                return R.fail(result.getCode(), result.getMessage());
            }
            livenessStatusList.add(result.getData());
        }
        return R.ok(livenessStatusList);
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
    public R<LivenessResult> detect(String imagePath, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
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
    public R<LivenessResult> detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        return detect(image, faceDetectionRectangle, keyPoints, true);
    }

    @Override
    public R<LivenessResult> detect(byte[] imageData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
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


    private R<LivenessResult> detectTopFace(BufferedImage image, boolean isImage) {
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        FaceAntiSpoofing faceAntiSpoofing = null;
        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        try {
            //默认使用Seetaface6检测模型
            if(Objects.isNull(config.getDetectModel())){
                detectPredictor = faceDetectorPool.borrowObject();
                faceLandmarker = faceLandmarkerPool.borrowObject();
                faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
                SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
                imageData.data = ImageUtils.getMatrixBGR(image);
                //检测人脸
                SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
                if(Objects.isNull(seetaResult)){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaResult[0], landmarks);
                FaceAntiSpoofing.Status status = null;
                //检测图片
                if(isImage){
                    status = faceAntiSpoofing.Predict(imageData, seetaResult[0], landmarks);
                }else{
                    status = faceAntiSpoofing.PredictVideo(imageData, seetaResult[0], landmarks);
                }
                return R.ok(new LivenessResult(FaceUtils.convertToLivenessStatus(status)));
            }else{
                R<DetectionResponse> faceDetectionResponse = config.getDetectModel().detect(image);
                if(Objects.isNull(faceDetectionResponse.getData()) || Objects.isNull(faceDetectionResponse.getData().getDetectionInfoList()) || faceDetectionResponse.getData().getDetectionInfoList().isEmpty()){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                DetectionInfo detectionInfo = faceDetectionResponse.getData().getDetectionInfoList().get(0);
                return detect(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getKeyPoints(), isImage);
            }
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            if (detectPredictor != null) {
                try {
                    faceDetectorPool.returnObject(detectPredictor);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }


    @Override
    public R<LivenessResult> detectTopFace(BufferedImage image) {
        return detectTopFace(image, true);
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


    public R<LivenessResult> detectVideoByFrame(BufferedImage frameImage, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(!ImageUtils.isImageValid(frameImage)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        return detect(frameImage,faceDetectionRectangle, keyPoints,false);
    }

    public R<LivenessResult> detectVideoByFrame(byte[] frameData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(Objects.isNull(frameData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(frameData)), faceDetectionRectangle, keyPoints, false);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    public R<LivenessResult> detectVideoByFrame(byte[] frameImageData) {
        if(Objects.isNull(frameImageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return detectVideoByFrame(ImageIO.read(new ByteArrayInputStream(frameImageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    public R<LivenessResult> detectVideoByFrame(BufferedImage frameImageData) {
        return detectTopFace(frameImageData, false);
    }

    @Override
    public R<LivenessResult> detectVideo(InputStream videoInputStream) {
        if(Objects.isNull(videoInputStream)){
            throw new FaceException("视频无效");
        }
        return detectVideo(new FFmpegFrameGrabber(videoInputStream));
    }

    @Override
    public R<LivenessResult> detectVideo(String videoPath) {
        if(!FileUtils.isFileExists(videoPath)){
            throw new FaceException("视频文件不存在");
        }
        return detectVideo(new FFmpegFrameGrabber(videoPath));
    }

    private R<LivenessResult> detectVideo(FFmpegFrameGrabber grabber) {
        FaceAntiSpoofing faceAntiSpoofing = null;
        try {
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            //重置视频
            faceAntiSpoofing.ResetVideo();
            grabber.start();
            // 获取视频总帧数
            int totalFrames = grabber.getLengthInFrames();
            int videoFrameCountConfig = faceAntiSpoofing.GetVideoFrameCount();
            log.debug("视频总帧数：{}，检测帧数：{}", totalFrames, videoFrameCountConfig);
            if(totalFrames < videoFrameCountConfig){
                return R.fail(1001, "视频帧数低于检测帧数");
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
                    R<LivenessResult> livenessStatus = detectVideoByFrame(bufferedImage);
                    if(!livenessStatus.isSuccess()){
                        log.debug("第" + frameIndex + "帧处理失败：" + livenessStatus.getMessage());
                        continue;
                    }
                    //满足检测帧数之后停止检测
                    if(livenessStatus.getData().getStatus() != LivenessStatus.DETECTING){
                        return livenessStatus;
                    }
                }
            }
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new FaceException(e);
        } catch (Exception e) {
            throw new FaceException(e);
        } finally {
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
        return R.fail(1000, "有效帧数量不足，无法完成活体检测");
    }

    @Override
    public void close() throws Exception {
        try {
            if (faceDetectorPool != null) {
                faceDetectorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (faceAntiSpoofingPool != null) {
                faceAntiSpoofingPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (faceLandmarkerPool != null) {
                faceLandmarkerPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
    }
}

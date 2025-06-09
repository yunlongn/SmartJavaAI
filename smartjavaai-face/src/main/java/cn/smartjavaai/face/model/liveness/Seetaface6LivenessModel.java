package cn.smartjavaai.face.model.liveness;

import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.common.enums.LivenessStatus;
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


    @Override
    public void loadModel(LivenessConfig config) {
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
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
            if(config.getGpuId() >= 0 && device == SeetaDevice.SEETA_DEVICE_GPU){
                gpuId = config.getGpuId();
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
            FaceAntiSpoofing faceAntiSpoofing = null;
            //设置参数
            try {
                faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
                if(config.getFaceClarityThreshold() > 0 && config.getRealityThreshold() > 0){
                    faceAntiSpoofing.SetThreshold(config.getFaceClarityThreshold(), config.getRealityThreshold());
                }
                if(config.getFrameCount() > 0){
                    faceAntiSpoofing.SetVideoFrameCount(config.getFrameCount());
                }
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
        } catch (FileNotFoundException e) {
            throw new FaceException(e);
        }
    }

    @Override
    public DetectionResponse detect(String imagePath) {
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
        return detect(image);
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
    public DetectionResponse detect(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        FaceAntiSpoofing.Status status = null;
        FaceAntiSpoofing faceAntiSpoofing = null;
        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
        List<LivenessStatus> livenessStatusList = new ArrayList<LivenessStatus>();
        try {
            detectPredictor = faceDetectorPool.borrowObject();
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            //检测人脸
            SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
            if(Objects.isNull(seetaResult)){
                throw new FaceException("无人脸数据");
            }
            for(SeetaRect seetaRect : seetaResult){
                SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaRect, landmarks);
                seetaPointFSList.add(landmarks);
                //检测图片
                status = faceAntiSpoofing.Predict(imageData, seetaRect, landmarks);
                livenessStatusList.add(FaceUtils.convertToLivenessStatus(status));
            }
            return FaceUtils.convertToDetectionResponse(seetaResult, seetaPointFSList, livenessStatusList);
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
    public List<LivenessStatus> detect(String imagePath, DetectionResponse faceDetectionResponse) {
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
        return detect(image, faceDetectionResponse);
    }

    @Override
    public LivenessStatus detect(String imagePath, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
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
        return detect(image, faceDetectionRectangle, keyPoints);
    }




    private List<LivenessStatus> detect(BufferedImage image, DetectionResponse faceDetectionResponse,boolean isImage) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        if(Objects.isNull(faceDetectionResponse) || Objects.isNull(faceDetectionResponse.getDetectionInfoList()) || faceDetectionResponse.getDetectionInfoList().isEmpty()){
            throw new FaceException("无人脸数据");
        }
        FaceAntiSpoofing.Status status = null;
        FaceAntiSpoofing faceAntiSpoofing = null;
        FaceLandmarker faceLandmarker = null;
        List<LivenessStatus> livenessStatusList = new ArrayList<LivenessStatus>();
        try {
            for(DetectionInfo detectionInfo : faceDetectionResponse.getDetectionInfoList()){
                faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
                faceLandmarker = faceLandmarkerPool.borrowObject();
                SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
                imageData.data = ImageUtils.getMatrixBGR(image);
                SeetaRect seetaRect = FaceUtils.convertToSeetaRect(detectionInfo.getDetectionRectangle());
                SeetaPointF[] landmarks = null;
                FaceInfo faceInfo = detectionInfo.getFaceInfo();
                //如果没有人脸标识，则提取人脸标识
                if(faceInfo == null || faceInfo.getKeyPoints() == null || faceInfo.getKeyPoints().isEmpty()){
                    //提取人脸的5点人脸标识
                    landmarks = new SeetaPointF[faceLandmarker.number()];
                    faceLandmarker.mark(imageData, seetaRect, landmarks);
                }else{
                    landmarks = FaceUtils.convertToSeetaPointF(faceInfo.getKeyPoints());
                }
                //检测图片
                if(isImage){
                    status = faceAntiSpoofing.Predict(imageData, seetaRect, landmarks);
                }else{
                    //检测视频
                    status = faceAntiSpoofing.PredictVideo(imageData, seetaRect, landmarks);
                }
                livenessStatusList.add(FaceUtils.convertToLivenessStatus(status));
            }
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
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
        return livenessStatusList;
    }

    @Override
    public List<LivenessStatus> detect(BufferedImage image, DetectionResponse faceDetectionResponse) {
         return detect(image, faceDetectionResponse, true);
    }

    private LivenessStatus detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints, boolean isImage) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        if(Objects.isNull(faceDetectionRectangle)){
            throw new FaceException("无人脸数据");
        }
        FaceAntiSpoofing.Status status = null;
        FaceAntiSpoofing faceAntiSpoofing = null;
        FaceLandmarker faceLandmarker = null;
        try {
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = FaceUtils.convertToSeetaRect(faceDetectionRectangle);
            SeetaPointF[] landmarks = null;
            if(keyPoints == null || keyPoints.isEmpty()){
                throw new FaceException("人脸关键点keyPoints为空");
            }
            landmarks = FaceUtils.convertToSeetaPointF(keyPoints);
            //检测图片
            if(isImage){
                status = faceAntiSpoofing.Predict(imageData, seetaRect, landmarks);
            }else{
                //检测视频
                status = faceAntiSpoofing.PredictVideo(imageData, seetaRect, landmarks);
            }
            return FaceUtils.convertToLivenessStatus(status);
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
    public LivenessStatus detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        return detect(image, faceDetectionRectangle, keyPoints, true);
    }



    @Override
    public List<LivenessStatus> detect(byte[] imageData, DetectionResponse faceDetectionResponse) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)), faceDetectionResponse);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public LivenessStatus detect(byte[] imageData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)), faceDetectionRectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }


    @Override
    public LivenessStatus detectTopFace(String imagePath) {
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
        return detectTopFace(image);
    }


    private LivenessStatus detectTopFace(BufferedImage image, boolean isImage) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        FaceAntiSpoofing.Status status = null;
        FaceAntiSpoofing faceAntiSpoofing = null;
        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        try {
            detectPredictor = faceDetectorPool.borrowObject();
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            //检测人脸
            SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
            if(Objects.isNull(seetaResult)){
                throw new FaceException("无人脸数据");
            }
            SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
            faceLandmarker.mark(imageData, seetaResult[0], landmarks);
            //检测图片
            if(isImage){
                status = faceAntiSpoofing.Predict(imageData, seetaResult[0], landmarks);
            }else{
                status = faceAntiSpoofing.PredictVideo(imageData, seetaResult[0], landmarks);
            }
            return FaceUtils.convertToLivenessStatus(status);
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
    public LivenessStatus detectTopFace(BufferedImage image) {
        return detectTopFace(image, true);
    }

    @Override
    public LivenessStatus detectTopFace(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detectTopFace(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }


    @Override
    public LivenessStatus detectVideoByFrame(BufferedImage frameImage, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(!ImageUtils.isImageValid(frameImage)){
            throw new FaceException("图像无效");
        }
        return detect(frameImage,faceDetectionRectangle, keyPoints,false);
    }

    @Override
    public LivenessStatus detectVideoByFrame(byte[] frameData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(Objects.isNull(frameData)){
            throw new FaceException("图像无效");
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(frameData)), faceDetectionRectangle, keyPoints, false);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public LivenessStatus detectVideoByFrame(byte[] frameImageData) {
        if(Objects.isNull(frameImageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detectVideoByFrame(ImageIO.read(new ByteArrayInputStream(frameImageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public LivenessStatus detectVideoByFrame(BufferedImage frameImageData) {
        return detectTopFace(frameImageData, false);
    }

    @Override
    public LivenessStatus detectVideo(InputStream videoInputStream) {
        if(Objects.isNull(videoInputStream)){
            throw new FaceException("视频无效");
        }
        return detectVideo(new FFmpegFrameGrabber(videoInputStream));
    }

    @Override
    public LivenessStatus detectVideo(String videoPath) {
        if(!FileUtils.isFileExists(videoPath)){
            throw new FaceException("视频文件不存在");
        }
        return detectVideo(new FFmpegFrameGrabber(videoPath));
    }

    private LivenessStatus detectVideo(FFmpegFrameGrabber grabber) {
        FaceAntiSpoofing faceAntiSpoofing = null;
        try {
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            grabber.start();
            // 获取视频总帧数
            int totalFrames = grabber.getLengthInFrames();
            int videoFrameCountConfig = faceAntiSpoofing.GetVideoFrameCount();
            log.debug("视频总帧数：{}，检测帧数：{}", totalFrames, videoFrameCountConfig);
            if(totalFrames < videoFrameCountConfig){
                throw new FaceException("视频帧数低于检测帧数");
            }
            // 逐帧处理视频
            for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
                // 获取当前帧
                Frame frame = grabber.grabImage();
                if (frame != null) {
                    BufferedImage bufferedImage = Java2DFrameUtils.toBufferedImage(frame);
                    LivenessStatus livenessStatus = detectVideoByFrame(bufferedImage);
                    //满足检测帧数之后停止检测
                    if(livenessStatus != LivenessStatus.DETECTING){
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
        return LivenessStatus.UNKNOWN;
    }
}

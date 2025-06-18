package smartai.examples.face.liveness;

import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.FaceInfo;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.LivenessStatus;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.face.constant.LivenessConstant;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceModelFactory;
import cn.smartjavaai.face.factory.LivenessModelFactory;
import cn.smartjavaai.face.model.facerec.FaceModel;
import cn.smartjavaai.face.model.liveness.LivenessDetModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * 静态活体检测demo
 * 模型下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
 * @author dwj
 * @date 2025/5/1
 */
@Slf4j
public class LivenessDetDemo {



    /**
     * 图片活体检测（多人脸）
     */
    @Test
    public void testLivenessDetect(){
        LivenessConfig config = new LivenessConfig();
        config.setModelEnum(LivenessModelEnum.SEETA_FACE6_MODEL);
        config.setDevice(DeviceEnum.GPU);
        //需替换为实际模型存储路径
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
        //人脸清晰度阈值,可选,默认0.3，活体识别时，如果清晰度低的话，就会直接返回FUZZY，清晰度满足阈值，则判断真实度
        config.setFaceClarityThreshold(LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD);
        //人脸活体阈值,可选,默认0.8，超过阈值则认为是真人，低于阈值是非活体
        config.setRealityThreshold(LivenessConstant.DEFAULT_REALITY_THRESHOLD);
        LivenessDetModel livenessDetModel = LivenessModelFactory.getInstance().getModel(config);
        DetectionResponse livenessStatusList = livenessDetModel.detect("src/main/resources/double_person.png");
        log.info("活体检测结果：{}", JSONObject.toJSONString(livenessStatusList));
    }

    /**
     * 图片活体检测（分数最高人脸）
     */
    @Test
    public void testLivenessDetect2(){
        LivenessConfig config = new LivenessConfig();
        config.setModelEnum(LivenessModelEnum.SEETA_FACE6_MODEL);
        //需替换为实际模型存储路径
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
        //人脸清晰度阈值,可选,默认0.3，活体识别时，如果清晰度低的话，就会直接返回FUZZY，清晰度满足阈值，则判断真实度
        config.setFaceClarityThreshold(LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD);
        //人脸活体阈值,可选,默认0.8，超过阈值则认为是真人，低于阈值是非活体
        config.setRealityThreshold(LivenessConstant.DEFAULT_REALITY_THRESHOLD);
        LivenessDetModel livenessDetModel = LivenessModelFactory.getInstance().getModel(config);
        LivenessStatus livenessStatus = livenessDetModel.detectTopFace("src/main/resources/double_person.png");
        log.info("活体检测结果：{}", JSONObject.toJSONString(livenessStatus));
    }

    /**
     * 图片多人脸活体检测（基于已检测出的人脸区域和关键点）
     */
    @Test
    public void testLivenessDetect3(){
        //人脸检测
        //需替换为实际模型存储路径
        String modelPath = "C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models";
        FaceModelConfig faceDetectModelConfig = new FaceModelConfig();
        faceDetectModelConfig.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);
        faceDetectModelConfig.setModelPath(modelPath);
        FaceModel faceDetectModel = FaceModelFactory.getInstance().getModel(faceDetectModelConfig);
        DetectionResponse detectionResponse = faceDetectModel.detect("src/main/resources/double_person.png");
        log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse));
        //检测到人脸
        if(detectionResponse != null && detectionResponse.getDetectionInfoList() != null && detectionResponse.getDetectionInfoList().size() > 0){
            //活体检测
            LivenessConfig config = new LivenessConfig();
            config.setModelEnum(LivenessModelEnum.SEETA_FACE6_MODEL);
            config.setModelPath(modelPath);
            //人脸清晰度阈值,可选,默认0.3，活体识别时，如果清晰度低的话，就会直接返回FUZZY，清晰度满足阈值，则判断真实度
            config.setFaceClarityThreshold(LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD);
            //人脸活体阈值,可选,默认0.8，超过阈值则认为是真人，低于阈值是非活体
            config.setRealityThreshold(LivenessConstant.DEFAULT_REALITY_THRESHOLD);
            LivenessDetModel livenessDetModel = LivenessModelFactory.getInstance().getModel(config);
            List<LivenessStatus> livenessStatusList = livenessDetModel.detect("src/main/resources/double_person.png",detectionResponse);
            log.info("活体检测结果：{}", JSONObject.toJSONString(livenessStatusList));
        }
    }

    /**
     * 图片单人脸活体检测（基于已检测出的人脸区域和关键点）
     */
    @Test
    public void testLivenessDetect4(){
        try {
            //人脸检测
            //需替换为实际模型存储路径
            String modelPath = "C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models";
            String imagePath = "src/main/resources/double_person.png";
            FaceModelConfig faceDetectModelConfig = new FaceModelConfig();
            faceDetectModelConfig.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);
            faceDetectModelConfig.setModelPath(modelPath);
            FaceModel faceDetectModel = FaceModelFactory.getInstance().getModel(faceDetectModelConfig);
            DetectionResponse detectionResponse = faceDetectModel.detect(imagePath);
            log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse));
            //检测到人脸
            if(detectionResponse != null && detectionResponse.getDetectionInfoList() != null && detectionResponse.getDetectionInfoList().size() > 0){
                //活体检测
                LivenessConfig config = new LivenessConfig();
                config.setModelEnum(LivenessModelEnum.SEETA_FACE6_MODEL);
                config.setModelPath(modelPath);
                //人脸清晰度阈值,可选,默认0.3，活体识别时，如果清晰度低的话，就会直接返回FUZZY，清晰度满足阈值，则判断真实度
                config.setFaceClarityThreshold(LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD);
                //人脸活体阈值,可选,默认0.8，超过阈值则认为是真人，低于阈值是非活体
                config.setRealityThreshold(LivenessConstant.DEFAULT_REALITY_THRESHOLD);
                LivenessDetModel livenessDetModel = LivenessModelFactory.getInstance().getModel(config);
                BufferedImage image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
                for (DetectionInfo detectionInfo : detectionResponse.getDetectionInfoList()){
                    FaceInfo faceInfo = detectionInfo.getFaceInfo();
                    LivenessStatus livenessStatus = livenessDetModel.detect(image, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
                    log.info("活体检测结果：{}", JSONObject.toJSONString(livenessStatus));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频活体检测
     */
    @Test
    public void testLivenessDetectVideo(){
        LivenessConfig config = new LivenessConfig();
        config.setModelEnum(LivenessModelEnum.SEETA_FACE6_MODEL);
        //需替换为实际模型存储路径
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
        //人脸清晰度阈值,可选,默认0.3，活体识别时，如果清晰度低的话，就会直接返回FUZZY，清晰度满足阈值，则判断真实度
        config.setFaceClarityThreshold(LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD);
        //人脸活体阈值,可选,默认0.8，超过阈值则认为是真人，低于阈值是非活体
        config.setRealityThreshold(LivenessConstant.DEFAULT_REALITY_THRESHOLD);
        /*视频检测帧数，可选，默认10，输出帧数超过这个number之后，就可以输出识别结果。
        这个数量相当于多帧识别结果融合的融合的帧数。当输入的帧数超过设定帧数的时候，会采用滑动窗口的方式，返回融合的最近输入的帧融合的识别结果。
        一般来说，在10以内，帧数越多，结果越稳定，相对性能越好，但是得到结果的延时越高。*/
        config.setFrameCount(LivenessConstant.DEFAULT_FRAME_COUNT);
        LivenessDetModel livenessDetModel = LivenessModelFactory.getInstance().getModel(config);
        LivenessStatus livenessStatus = livenessDetModel.detectVideo("src/main/resources/girl.mp4");
        log.info("视频活体检测结果：{}", JSONObject.toJSONString(livenessStatus));
    }




    /**
     * 视频活体检测（逐帧检测，基于已检测出的人脸区域和关键点）
     */
    @Test
    public void testLivenessDetectVideo2(){
        LivenessConfig config = new LivenessConfig();
        config.setModelEnum(LivenessModelEnum.SEETA_FACE6_MODEL);
        //需替换为实际模型存储路径
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
        //人脸清晰度阈值,可选,默认0.3，活体识别时，如果清晰度低的话，就会直接返回FUZZY，清晰度满足阈值，则判断真实度
        config.setFaceClarityThreshold(LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD);
        //人脸活体阈值,可选,默认0.8，超过阈值则认为是真人，低于阈值是非活体
        config.setRealityThreshold(LivenessConstant.DEFAULT_REALITY_THRESHOLD);
        /* 视频检测帧数，可选，默认10，输出帧数超过这个number之后，就可以输出识别结果。
        这个数量相当于多帧识别结果融合的融合的帧数。当输入的帧数超过设定帧数的时候，会采用滑动窗口的方式，返回融合的最近输入的帧融合的识别结果。
        一般来说，在10以内，帧数越多，结果越稳定，相对性能越好，但是得到结果的延时越高。*/
        config.setFrameCount(LivenessConstant.DEFAULT_FRAME_COUNT);
        LivenessDetModel livenessDetModel = LivenessModelFactory.getInstance().getModel(config);
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("src/main/resources/girl.mp4");
            grabber.start();
            // 获取视频总帧数
            int totalFrames = grabber.getLengthInFrames();
            log.info("视频总帧数：{}，检测帧数：{}", totalFrames, config.getFrameCount());
            //活体检测结果
            LivenessStatus livenessStatus = LivenessStatus.UNKNOWN;
            // 逐帧处理视频
            for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
                // 获取当前帧
                Frame frame = grabber.grabImage();
                if (frame != null) {
                    BufferedImage bufferedImage = Java2DFrameUtils.toBufferedImage(frame);
                    LivenessStatus livenessStatusFrame = livenessDetModel.detectVideoByFrame(bufferedImage);
                    //满足检测帧数之后停止检测
                    if(livenessStatusFrame != LivenessStatus.DETECTING){
                        livenessStatus = livenessStatusFrame;
                    }
                }
            }
            log.info("视频活体检测结果：{}", JSONObject.toJSONString(livenessStatus));
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new FaceException(e);
        }
    }

    /**
     * 视频活体检测（逐帧检测）
     */
    @Test
    public void testLivenessDetectVideo3(){
        //获取活体检测模型
        //需替换为实际模型存储路径
        String modelPath = "C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models";
        LivenessConfig config = new LivenessConfig();
        config.setModelEnum(LivenessModelEnum.SEETA_FACE6_MODEL);
        config.setModelPath(modelPath);
        //人脸清晰度阈值,可选,默认0.3，活体识别时，如果清晰度低的话，就会直接返回FUZZY，清晰度满足阈值，则判断真实度
        config.setFaceClarityThreshold(LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD);
        //人脸活体阈值,可选,默认0.8，超过阈值则认为是真人，低于阈值是非活体
        config.setRealityThreshold(LivenessConstant.DEFAULT_REALITY_THRESHOLD);
        /* 视频检测帧数，可选，默认10，输出帧数超过这个number之后，就可以输出识别结果。
        这个数量相当于多帧识别结果融合的融合的帧数。当输入的帧数超过设定帧数的时候，会采用滑动窗口的方式，返回融合的最近输入的帧融合的识别结果。
        一般来说，在10以内，帧数越多，结果越稳定，相对性能越好，但是得到结果的延时越高。*/
        config.setFrameCount(LivenessConstant.DEFAULT_FRAME_COUNT);
        LivenessDetModel livenessDetModel = LivenessModelFactory.getInstance().getModel(config);
        //获取人脸检测模型
        FaceModelConfig faceDetectModelConfig = new FaceModelConfig();
        faceDetectModelConfig.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);
        faceDetectModelConfig.setModelPath(modelPath);
        FaceModel faceDetectModel = FaceModelFactory.getInstance().getModel(faceDetectModelConfig);
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("src/main/resources/girl.mp4");
            grabber.start();
            // 获取视频总帧数
            int totalFrames = grabber.getLengthInFrames();
            log.info("视频总帧数：{}，检测帧数：{}", totalFrames, config.getFrameCount());
            //活体检测结果
            LivenessStatus livenessStatus = LivenessStatus.UNKNOWN;
            // 逐帧处理视频
            for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
                // 获取当前帧
                Frame frame = grabber.grabImage();
                if (frame != null) {
                    BufferedImage bufferedImage = Java2DFrameUtils.toBufferedImage(frame);
                    //检测视频帧人脸
                    DetectionResponse detectionResponse = faceDetectModel.detect(bufferedImage);
                    //检测到人脸
                    if(detectionResponse != null && detectionResponse.getDetectionInfoList() != null && detectionResponse.getDetectionInfoList().size() > 0){
                        DetectionRectangle detectionRectangle = detectionResponse.getDetectionInfoList().get(0).getDetectionRectangle();
                        FaceInfo faceInfo = detectionResponse.getDetectionInfoList().get(0).getFaceInfo();
                        //使用人脸检测结果 活体检测
                        LivenessStatus livenessStatusFrame = livenessDetModel.detectVideoByFrame(bufferedImage, detectionRectangle, faceInfo.getKeyPoints());
                        //满足检测帧数之后停止检测
                        if(livenessStatusFrame != LivenessStatus.DETECTING){
                            livenessStatus = livenessStatusFrame;
                        }
                    }else{
                        log.info("未检测到人脸");
                    }
                }
            }
            log.info("视频活体检测结果：{}", JSONObject.toJSONString(livenessStatus));
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new FaceException(e);
        }
    }


}

package smartai.examples.face.facedet;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.liveness.LivenessDetModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import smartai.examples.face.ViewerFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;

/**
 * 人脸检测模型demo
 * 支持系统：windows 64位，linux 64位, macos M系列
 * 支持功能：人脸检测
 * 模型下载地址：https://pan.baidu.com/s/1d2YlJ2YOdGn3Y-AegyAhmQ?pwd=1234 提取码: 1234
 * @author dwj
 */
@Slf4j
public class FaceDetDemo {


    public static String imgPath = "src/main/resources/iu_1.jpg";


    /**
     * 获取人脸检测模型
     * 注意事项：高精度模型，速度较慢
     * @return
     */
    public FaceDetModel getFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        config.setModelEnum(FaceDetModelEnum.RETINA_FACE);//人脸检测模型
        config.setConfidenceThreshold(FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD);//只返回相似度大于该值的人脸
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);//用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        return FaceDetModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取Seetaface6 人脸检测模型
     * 注意：不支持macos
     * @return
     */
    public FaceDetModel getSeetaface6DetModel(){
        FaceDetConfig config = new FaceDetConfig();
        //指定模型
        config.setModelEnum(FaceDetModelEnum.SEETA_FACE6_MODEL);
        //指定模型路径：请根据实际情况替换为本地模型文件的绝对路径（模型下载地址请查看文档）
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
        return FaceDetModelFactory.getInstance().getModel(config);
    }

    /**
     * 人脸检测(默认配置)
     * 使用默认模型参数检测，默认模型：retinaface，需联网，会自动下载模型
     * 图片参数：图片路径
     */
    @Test
    public void testFaceDetect(){
        try (FaceDetModel faceModel = FaceDetModelFactory.getInstance().getModel()) {
            R<DetectionResponse> detectedResult = faceModel.detect(imgPath);
            if(detectedResult.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult.getData()));
            }else{
                log.info("人脸检测失败：{}", detectedResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸检测(自定义模型参数)
     * 图片参数：图片路径
     */
    @Test
    public void testFaceDetectCustomConfig(){
        try (FaceDetModel faceModel = getFaceDetModel()){
            R<DetectionResponse> detectedResult = faceModel.detect(imgPath);
            if(detectedResult.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult.getData()));
            }else{
                log.info("人脸检测失败：{}", detectedResult.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 人脸检测并绘制人脸框
     */
    @Test
    public void testFaceDetectAndDraw(){
        try (FaceDetModel faceModel = getFaceDetModel()){
            faceModel.detectAndDraw("src/main/resources/largest_selfie.jpg","output/largest_selfie_detected.png");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 人脸检测并绘制人脸框,返回BufferedImage
     *
     */
    @Test
    public void testFaceDetectAndDraw2(){
        try (FaceDetModel faceModel = getFaceDetModel()){
            BufferedImage image = null;
            String imagePath = "src/main/resources/largest_selfie.jpg";
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
            //可以根据后续业务场景使用detectedImage
            R<BufferedImage> detectedImage = faceModel.detectAndDraw(image);
            if(detectedImage.isSuccess()){
                log.info("人脸检测成功");
            }else{
                log.info("人脸检测失败：{}", detectedImage.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * 人脸检测（离线模型）
     */
    @Test
    public void testDetectFaceOffine(){
        FaceDetConfig config = new FaceDetConfig();
        config.setModelEnum(FaceDetModelEnum.RETINA_FACE);//人脸模型
        //模型路径,不同模型下载路径请参看文档
        config.setModelPath("/Users/xxx/Documents/develop/face_model/retinaface.pt");
        try (FaceDetModel faceModel = FaceDetModelFactory.getInstance().getModel(config)) {
            R<DetectionResponse> detectedResult = faceModel.detect(imgPath);
            if(detectedResult.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult.getData()));
            }else{
                log.info("人脸检测失败：{}", detectedResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸检测（GPU模式）
     */
    @Test
    public void testDetectFaceGPU(){
        FaceDetConfig config = new FaceDetConfig();
        config.setModelEnum(FaceDetModelEnum.RETINA_FACE);//人脸模型
        config.setDevice(DeviceEnum.GPU);
        try (FaceDetModel faceModel = FaceDetModelFactory.getInstance().getModel(config)) {
            R<DetectionResponse> detectedResult = faceModel.detect(imgPath);
            if(detectedResult.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult.getData()));
            }else{
                log.info("人脸检测失败：{}", detectedResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸检测(Seetaface6)
     * 图片参数：图片路径
     */
    @Test
    public void testFaceDetectSeetaface6(){
        try (FaceDetModel faceModel = getSeetaface6DetModel()){
            R<DetectionResponse> detectedResult = faceModel.detect(imgPath);
            if(detectedResult.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult.getData()));
            }else{
                log.info("人脸检测失败：{}", detectedResult.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 摄像头人脸检测
     * 注意事项：如果视频比较卡，可以使用轻量的人脸检测模型
     */
    @Test
    public void testDetectCamera(){
        try (FaceDetModel faceModel = getFaceDetModel()){
            OpenCV.loadShared();
            VideoCapture capture = new VideoCapture(0);
            if (!capture.isOpened()) {
                System.out.println("No camera detected");
                return;
            }

            double ratio =
                    capture.get(Videoio.CAP_PROP_FRAME_WIDTH)
                            / capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int height = (int) (screenSize.height * 0.65f);
            int width = (int) (height * ratio);
            if (width > screenSize.width) {
                width = screenSize.width;
            }

            Mat image = new Mat();
            boolean captured = false;
            for (int i = 0; i < 10; ++i) {
                captured = capture.read(image);
                if (captured) {
                    break;
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignore) {
                    // ignore
                }
            }
            if (!captured) {
                JOptionPane.showConfirmDialog(null, "Failed to capture image from WebCam.");
            }
            ViewerFrame frame = new ViewerFrame(width, height);
            ImageFactory factory = ImageFactory.getInstance();
            Size size = new Size(width, height);

            while (capture.isOpened()) {
                if (!capture.read(image)) {
                    break;
                }
                Mat resizeImage = new Mat();
                Imgproc.resize(image, resizeImage, size);
                Image img = factory.fromImage(resizeImage);
                BufferedImage bufferedImage = OpenCVUtils.mat2Image(resizeImage);
                R<DetectionResponse> detectedResult = faceModel.detect(bufferedImage);
                if(!detectedResult.isSuccess()){
                    log.debug("识别失败：{}", detectedResult.getMessage());
                    continue;
                }
                for(DetectionInfo detectionInfo : detectedResult.getData().getDetectionInfoList()){
                    DetectionRectangle detectionRectangle = detectionInfo.getDetectionRectangle();
                    String text = null;
                    if(detectionInfo.getScore() > 0){
                        text = detectionInfo.getScore() + "";
                    }
                    ImageUtils.drawImageRectWithText(bufferedImage, detectionRectangle, text, Color.red);
                }
                frame.showImage(bufferedImage);
            }

            capture.release();
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}

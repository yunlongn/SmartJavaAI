package smartai.examples.face;

import cn.smartjavaai.common.entity.Rectangle;
import cn.smartjavaai.face.FaceAlgorithm;
import cn.smartjavaai.face.FaceAlgorithmFactory;
import cn.smartjavaai.face.FaceDetectedResult;
import cn.smartjavaai.face.ModelConfig;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartai.examples.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author dwj
 */
public class FaceDemo {

    // 创建 Logger 实例
    private static final Logger logger = LoggerFactory.getLogger(FaceDemo.class);


    public static void main(String[] args) {
        try {
            //detectFace();
            //verifyIDCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸检测（服务端模型）
     * 人脸模型：retinaface
     * 特点：识别精度高，高速
     * 应用场景：如监控摄像头、智能安防系统等需要高精度检测的场合
     */
    public static void detectFace() throws Exception {
        //创建人脸算法
        FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm();
        //使用图片路径检测
        FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
        logger.info("人脸检测结果：{}", JSONObject.toJSONString(result));
        //使用图片流检测
        //File imageFile = new File("/Users/wenjie/Downloads/djl-master/examples/src/test/resources/largest_selfie.jpg");
        //FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(imageFile));
        File input = new File("src/main/resources/largest_selfie.jpg");
        BufferedImage image = ImageIO.read(input);
        //创建保存路径
        Path imagePath = Paths.get("output").resolve("retinaface_detected.jpg");
        //绘制人脸框
        ImageUtils.drawBoundingBoxes(image, result, imagePath.toAbsolutePath().toString());
    }


    /**
     * 人脸检测（轻量模型）
     * 人脸模型：Ultra-Light-Fast-Generic-Face-Detector-1MB
     * 特点：高速，准确率略低
     * 应用场景：如监控摄像头、智能安防系统等需要高精度检测的场合
     */
    public static void detectFace2() throws Exception {
        //创建轻量人脸算法
        FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createLightFaceAlgorithm();
        //使用图片路径检测
        FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
        logger.info("轻量人脸检测结果：{}", JSONObject.toJSONString(result));
        //使用图片流检测
        //File imageFile = new File("/Users/wenjie/Downloads/djl-master/examples/src/test/resources/largest_selfie.jpg");
        //FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(imageFile));
        File input = new File("src/main/resources/largest_selfie.jpg");
        BufferedImage image = ImageIO.read(input);
        //创建保存路径
        Path imagePath = Paths.get("output").resolve("retinaface_detected.jpg");
        //绘制人脸框
        ImageUtils.drawBoundingBoxes(image, result, imagePath.toAbsolutePath().toString());
    }

    /**
     * 人证核验
     * @throws Exception
     */
    public static void verifyIDCard() throws Exception {
        //创建脸算法
        FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm();
        //提取身份证人脸特征（图片仅供测试）
        float[] featureIdCard = currentAlgorithm.featureExtraction("src/main/resources/kana1.jpg");
        //提取身份证人脸特征（从图片流获取）
        //File imageFile = new File("/Users/wenjie/Downloads/djl-master/examples/src/test/resources/largest_selfie.jpg");
        //float[] featureIdCard = currentAlgorithm.featureExtraction(new FileInputStream(imageFile));
        logger.info("身份证人脸特征：{}", JSONObject.toJSONString(featureIdCard));
        //提取实时人脸特征（图片仅供测试）
        float[] realTimeFeature = currentAlgorithm.featureExtraction("src/main/resources/kana2.jpg");
        logger.info("实时人脸特征：{}", JSONObject.toJSONString(realTimeFeature));
        if(realTimeFeature != null){
            if(currentAlgorithm.calculSimilar(featureIdCard, realTimeFeature) > 0.8){
                logger.info("人脸核验通过");
            }else{
                logger.info("人脸核验不通过");
            }
        }
    }

}

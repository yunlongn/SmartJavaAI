package smartai.examples.face;

import cn.smartjavaai.common.entity.Rectangle;
import cn.smartjavaai.face.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.StopWatch;
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

    private static final Logger logger = LoggerFactory.getLogger(FaceDemo.class);


    public static void main(String[] args) {
        try {
            verifyIDCard();
            //detectFace2();
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
        // 创建并启动计时器
        StopWatch sw = StopWatch.createStarted();
        //创建人脸算法
        FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm();
        sw.stop();
        logger.info("创建人脸算法耗时：" + sw.getTime() + "ms");
        sw.reset();
        sw.start();
        //使用图片路径检测
        FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
        sw.stop();
        logger.info("人脸检测耗时：" + sw.getTime() + "ms");
        logger.info("人脸检测结果：{}", JSONObject.toJSONString(result));
        //使用图片流检测
        File input = new File("src/main/resources/largest_selfie.jpg");
        //FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(input));
        //logger.info("人脸检测结果：{}", JSONObject.toJSONString(result));
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
        // 创建并启动计时器
        StopWatch sw = StopWatch.createStarted();
        //创建轻量人脸算法
        FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createLightFaceAlgorithm();
        sw.stop();
        logger.info("创建人脸算法耗时：" + sw.getTime() + "ms");
        sw.reset();
        sw.start();
        //使用图片路径检测
        FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
        sw.stop();
        logger.info("人脸检测耗时：" + sw.getTime() + "ms");
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
        // 创建并启动计时器
        StopWatch sw = StopWatch.createStarted();
        //创建脸算法
        FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceFeatureAlgorithm();
        sw.stop();
        logger.info("创建人脸算法耗时：" + sw.getTime() + "ms");
        sw.reset();
        sw.start();
        //提取身份证人脸特征（图片仅供测试）
        float[] featureIdCard = currentAlgorithm.featureExtraction("src/main/resources/MJ_20250213_155245.png");
        sw.stop();
        logger.info("人脸特征提取耗时：" + sw.getTime() + "ms");
        //提取身份证人脸特征（从图片流获取）
        //File input = new File("src/main/resources/kana1.jpg");
        //float[] featureIdCard = currentAlgorithm.featureExtraction(new FileInputStream(input));
        logger.info("身份证人脸特征：{}", JSONObject.toJSONString(featureIdCard));
        //提取实时人脸特征（图片仅供测试）
        float[] realTimeFeature = currentAlgorithm.featureExtraction("src/main/resources/MJ_20250213_155228.png");
        logger.info("实时人脸特征：{}", JSONObject.toJSONString(realTimeFeature));
        if(realTimeFeature != null){
            System.out.println("相似度：" + currentAlgorithm.calculSimilar(featureIdCard, realTimeFeature));
            if(currentAlgorithm.calculSimilar(featureIdCard, realTimeFeature) > 0.8){
                logger.info("人脸核验通过");
            }else{
                logger.info("人脸核验不通过");
            }
        }
    }

    /**
     * 人脸检测（离线模型）
     * 人脸模型：retinaface
     * 特点：识别精度高，高速
     * 应用场景：如监控摄像头、智能安防系统等需要高精度检测的场合
     */
    public static void detectFaceOffine() throws Exception {
        // 初始化配置
        ModelConfig config = new ModelConfig();
        config.setAlgorithmName("retinaface");//人脸算法模型，目前支持：retinaface及ultralightfastgenericface
        //config.setAlgorithmName("ultralightfastgenericface");//轻量模型
        config.setConfidenceThreshold(FaceConfig.DEFAULT_CONFIDENCE_THRESHOLD);//置信度阈值
        config.setMaxFaceCount(FaceConfig.MAX_FACE_LIMIT);//每张特征图保留的最大候选框数量
        //nms阈值:控制重叠框的合并程度,取值越低，合并越多重叠框（减少误检但可能漏检）；取值越高，保留更多框（增加检出但可能引入冗余）
        config.setNmsThresh(FaceConfig.NMS_THRESHOLD);
        //模型下载地址：
        //retinaface: https://resources.djl.ai/test-models/pytorch/retinaface.zip
        //ultralightfastgenericface: https://resources.djl.ai/test-models/pytorch/ultranet.zip
        //改为模型存放路径
        config.setModelPath("/Users/wenjie/Documents/develop/face_model/retinaface.pt");
        //创建人脸算法
        FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm(config);
        //使用图片路径检测
        FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
        logger.info("人脸检测结果：{}", JSONObject.toJSONString(result));
        //使用图片流检测
        File input = new File("src/main/resources/largest_selfie.jpg");
        //FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(input));
        //logger.info("人脸检测结果：{}", JSONObject.toJSONString(result));
        BufferedImage image = ImageIO.read(input);
        //创建保存路径
        Path imagePath = Paths.get("output").resolve("retinaface_detected.jpg");
        //绘制人脸框
        ImageUtils.drawBoundingBoxes(image, result, imagePath.toAbsolutePath().toString());
    }

    /**
     * 人证核验(离线模型)
     * @throws Exception
     */
    public static void verifyIDCardOffine() throws Exception {
        // 初始化配置
        ModelConfig config = new ModelConfig();
        config.setAlgorithmName("featureExtraction");
        //模型下载地址：https://resources.djl.ai/test-models/pytorch/face_feature.zip
        //改为模型存放路径
        config.setModelPath("/Users/xxx/Documents/develop/face_model/face_feature.pt");
        //创建脸算法
        FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceFeatureAlgorithm(config);
        //提取身份证人脸特征（图片仅供测试）
        float[] featureIdCard = currentAlgorithm.featureExtraction("src/main/resources/kana1.jpg");
        //提取身份证人脸特征（从图片流获取）
        //File input = new File("src/main/resources/kana1.jpg");
        //float[] featureIdCard = currentAlgorithm.featureExtraction(new FileInputStream(input));
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

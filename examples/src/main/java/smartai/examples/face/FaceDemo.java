package smartai.examples.face;

import cn.smartjavaai.common.entity.Rectangle;
import cn.smartjavaai.face.*;
import cn.smartjavaai.face.entity.FaceResult;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartai.examples.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author dwj
 */
@Slf4j
public class FaceDemo {


    public static void main(String[] args) {
        try {
            featureExtractionAndCompare2();
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
    public static void detectFace(){
        try {
            //创建人脸算法
            FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm();
            //使用图片路径检测
            FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
            log.info("人脸检测结果：{}", JSONObject.toJSONString(result));
            //使用图片流检测
            File input = new File("src/main/resources/largest_selfie.jpg");
            //FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(input));
            //log.info("人脸检测结果：{}", JSONObject.toJSONString(result));
            BufferedImage image = ImageIO.read(input);
            //创建保存路径
            Path imagePath = Paths.get("output").resolve("retinaface_detected.jpg");
            //绘制人脸框
            ImageUtils.drawBoundingBoxes(image, result, imagePath.toAbsolutePath().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 人脸检测（轻量模型）
     * 人脸模型：Ultra-Light-Fast-Generic-Face-Detector-1MB
     * 特点：高速，准确率略低
     * 应用场景：如监控摄像头、智能安防系统等需要高精度检测的场合
     */
    public static void detectFace2(){
        try {
            //创建轻量人脸算法
            FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createLightFaceAlgorithm();
            //使用图片路径检测
            FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
            log.info("轻量人脸检测结果：{}", JSONObject.toJSONString(result));
            //使用图片流检测
            //File imageFile = new File("/Users/wenjie/Downloads/djl-master/examples/src/test/resources/largest_selfie.jpg");
            //FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(imageFile));
            File input = new File("src/main/resources/largest_selfie.jpg");
            BufferedImage image = ImageIO.read(input);
            //创建保存路径
            Path imagePath = Paths.get("output").resolve("retinaface_detected.jpg");
            //绘制人脸框
            ImageUtils.drawBoundingBoxes(image, result, imagePath.toAbsolutePath().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 人脸检测（离线模型）
     * 人脸模型：retinaface
     * 特点：识别精度高，高速
     * 应用场景：如监控摄像头、智能安防系统等需要高精度检测的场合
     */
    public static void detectFaceOffine(){
        try {
            // 初始化配置
            ModelConfig config = new ModelConfig();
            config.setAlgorithmName("retinaface");//人脸算法模型，目前支持：retinaface/ultralightfastgenericface/seetaface6
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
            log.info("人脸检测结果：{}", JSONObject.toJSONString(result));
            //使用图片流检测
            File input = new File("src/main/resources/largest_selfie.jpg");
            //FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(input));
            //logger.info("人脸检测结果：{}", JSONObject.toJSONString(result));
            BufferedImage image = ImageIO.read(input);
            //创建保存路径
            Path imagePath = Paths.get("output").resolve("retinaface_detected.jpg");
            //绘制人脸框
            ImageUtils.drawBoundingBoxes(image, result, imagePath.toAbsolutePath().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸比对（1：1）
     * @throws Exception
     */
    public static void featureComparison(){
        try {
            // 初始化配置
            ModelConfig config = new ModelConfig();
            config.setAlgorithmName("seetaface6");//目前支持人脸比对的算法只有：seetaface6
            //人脸库路径 如果不指定人脸库，无法使用 1:N人脸搜索
            config.setFaceDbPath("C:/Users/Administrator/Downloads/faces-data.db");
            //改为模型存放路径
            config.setModelPath("/opt/sf3.0_models");
            //创建人脸算法
            FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm(config);
            //自动裁剪人脸并比对人脸特征
            float similar = currentAlgorithm.featureComparison("src/main/resources/kana1.jpg","src/main/resources/kana2.jpg");
            log.info("相似度：{}", similar);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * seetaface6人脸特征提取及比对（可人证核验）
     * 目前仅支持windows 64位系统，如需支持其他操作系统可参考方法：featureExtractionAndCompare2
     */
    public static void featureExtractionAndCompare(){
        try {
            // 初始化配置
            ModelConfig config = new ModelConfig();
            config.setAlgorithmName("seetaface6");
            //人脸库路径 如果不指定人脸库，无法使用 1:N人脸搜索
            config.setFaceDbPath("C:/Users/Administrator/Downloads/faces-data.db");
            //改为模型存放路径
            config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
            //创建人脸算法
            FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm(config);
            //提取图像中最大人脸的特征
            float[] feature1 = currentAlgorithm.featureExtraction("src/main/resources/kana1.jpg");
            float[] feature2 = currentAlgorithm.featureExtraction("src/main/resources/kana2.jpg");
            if(feature1 != null && feature2 != null){
                float similar = currentAlgorithm.calculSimilar(feature1, feature2);
                log.info("相似度：{}", similar);
            }else{
                log.warn("人脸特征提取失败");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * facenet-pytorch 人脸特征提取及比对（可人证核验）
     * 支持windows，linux，macos
     */
    public static void featureExtractionAndCompare2(){
        try {
            //创建脸算法
            FaceAlgorithm featureAlgorithm = FaceAlgorithmFactory.createFaceFeatureAlgorithm();
            //提取身份证人脸特征
            float[] feature1 = featureAlgorithm.featureExtraction("src/main/resources/kana1.jpg");
            float[] feature2 = featureAlgorithm.featureExtraction("src/main/resources/kana2.jpg");
            if (feature1 != null && feature2 != null) {
                //相似度在0.8至0.85及以上时，可判定为同一人，但具体阈值可能因图片而异，存在一定误差。
                float similar = featureAlgorithm.calculSimilar(feature1, feature2);
                log.info("相似度：{}", similar);
            } else {
                log.warn("人脸特征提取失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        /**
         * 注册人脸及搜索人脸（1：N）
         */
    public static void registerAndSearchFace(){
        try {
            // 初始化配置
            ModelConfig config = new ModelConfig();
            config.setAlgorithmName("seetaface6");
            //人脸库路径 如果不指定人脸库，无法使用 1:N人脸搜索
            config.setFaceDbPath("C:/Users/Administrator/Downloads/faces-data.db");
            //改为模型存放路径
            config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
            //创建人脸算法 自动将人脸库加载到内存中
            FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm(config);
            //等待人脸库加载完毕
            Thread.sleep(1000);
            //注册kana1人脸，参数key建议设置为人名
            boolean isSuccss = currentAlgorithm.register("kana1","src/main/resources/kana1.jpg");
            //注册jsy人脸，参数key建议设置为人名
            isSuccss = currentAlgorithm.register("jsy","src/main/resources/jsy.jpg");
            FaceResult faceResult = currentAlgorithm.search("src/main/resources/kana2.jpg");
            if(faceResult != null){
                log.info("查询到人脸：{}", faceResult.toString());
            }else{
                log.info("未查询到人脸");
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 删除已注册人脸
     */
    public static void removeRegisterFace(){
        try {
            // 初始化配置
            ModelConfig config = new ModelConfig();
            config.setAlgorithmName("seetaface6");
            //人脸库路径 如果不指定人脸库，无法使用 1:N人脸搜索
            config.setFaceDbPath("C:/Users/Administrator/Downloads/faces-data.db");
            //改为模型存放路径
            config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
            //创建人脸算法 自动将人脸库加载到内存中
            FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm(config);
            //等待人脸库加载完毕
            Thread.sleep(1000);
            //使用注册人脸时的key值删除，可一次性删除单个
            long num = currentAlgorithm.removeRegister("kana1");
            //删除全部人脸
            //long num = currentAlgorithm.clearFace();
            log.info("删除成功数量：" + num);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}

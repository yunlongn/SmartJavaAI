package smartai.examples.face.facerec;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.factory.FaceModelFactory;
import cn.smartjavaai.face.model.facerec.FaceModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * UltraLightFastGenericFaceModel 轻量人脸算法模型demo
 * 支持功能：人脸检测（不支持人脸特征提取）
 * @author dwj
 * @date 2025/4/11
 */
@Slf4j
public class LightFaceDemo {


    /**
     * 人脸检测-自定义参数
     * 图片参数：图片路径
     */
    @Test
    public void testFaceDetectCustomConfig(){
        FaceModelConfig config = new FaceModelConfig();
        config.setModelEnum(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);//人脸模型
        //config.setConfidenceThreshold(FaceConfig.DEFAULT_CONFIDENCE_THRESHOLD);//只返回相似度大于该值的人脸
        //config.setNmsThresh(FaceConfig.NMS_THRESHOLD);//用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
        DetectionResponse detectedResult = faceModel.detect("src/main/resources/largest_selfie.jpg");
        log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult));
    }


    /**
     * 人脸检测并绘制人脸框
     */
    @Test
    public void testFaceDetectAndDraw(){
        FaceModelConfig config = new FaceModelConfig();
        config.setModelEnum(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);//人脸模型
        FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
        faceModel.detectAndDraw("src/main/resources/largest_selfie.jpg","output/largest_selfie_detected.png");
    }

    /**
     * 人脸检测并绘制人脸框,返回BufferedImage
     *
     */
    @Test
    public void testFaceDetectAndDraw2(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);//人脸模型
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            BufferedImage image = null;
            String imagePath = "src/main/resources/largest_selfie.jpg";
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
            //可以根据后续业务场景使用detectedImage
            BufferedImage detectedImage = faceModel.detectAndDraw(image);
            Assert.assertNotNull("detectedImage null", detectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 人脸检测（离线模型）
     */
    @Test
    public void testDetectFaceOffine(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);//人脸模型
            //模型路径,不同模型下载路径请参看文档
            config.setModelPath("/Users/xxx/Documents/develop/face_model/ultranet.pt");
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            DetectionResponse detectedResult = faceModel.detect("src/main/resources/largest_selfie.jpg");
            log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

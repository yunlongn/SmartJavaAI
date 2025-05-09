package smartai.examples.face.facerec;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.entity.FaceResult;
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
import java.util.List;

/**
 * SeetaFace6人脸算法模型demo
 * 支持系统：windows 64位
 * 支持功能：人脸检测、人脸特征提取、人脸比对（1：1）、人脸比对（1：N）、人脸注册
 * @author dwj
 * @date 2025/4/11
 */
@Slf4j
public class SeetaFace6Demo {


    /**
     * 人脸检测(自定义模型参数)
     * 图片参数：图片路径
     */
    @Test
    public void testFaceDetectCustomConfig(){
        FaceModelConfig config = new FaceModelConfig();
        config.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);//人脸模型
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
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
        config.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);//人脸模型
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
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
            config.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);//人脸模型
            config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
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
     * 提取人脸特征(支持多人脸)
     * 自动裁剪人脸 + 人脸对齐
     */
    @Test
    public void testExtractFeatures(){
        try {
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(new FaceModelConfig(FaceModelEnum.SEETA_FACE6_MODEL,
                    "C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models"));
            List<float[]> faceResult = faceModel.extractFeatures("src/main/resources/kana1.jpg");
            log.info("人脸特征提取结果：{}", JSONObject.toJSONString(faceResult));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 提取人脸特征(分数最高人脸)
     * 自动裁剪人脸 + 人脸对齐
     */
    @Test
    public void testExtractTopFaceFeature(){
        try {
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(new FaceModelConfig(FaceModelEnum.SEETA_FACE6_MODEL,
                    "C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models"));
            float[] faceResult = faceModel.extractTopFaceFeature("src/main/resources/kana1.jpg");
            log.info("人脸特征提取结果：{}", JSONObject.toJSONString(faceResult));
        }catch (Exception e){
            e.printStackTrace();
        }
    }




    /**
     * 人脸比对（1：1）
     * 图片参数：图片路径
     * @throws Exception
     */
    @Test
    public void featureComparison(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);//人脸模型
            config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //自动裁剪人脸并比对人脸特征
            float similar = faceModel.featureComparison("src/main/resources/kana1.jpg","src/main/resources/kana2.jpg");
            log.info("相似度：{}", similar);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 人脸比对（1：1）
     * 先特征提取，后比对人脸特征
     * 提取人脸特征图片参数：图片路径
     */
    @Test
    public void featureExtractionAndCompare(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);//人脸模型
            config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //提取图像中最大人脸的特征
            float[] feature1 = faceModel.extractTopFaceFeature("src/main/resources/kana1.jpg");
            float[] feature2 = faceModel.extractTopFaceFeature("src/main/resources/kana2.jpg");
            if(feature1 != null && feature2 != null){
                float similar = faceModel.calculSimilar(feature1, feature2);
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
     * 注册人脸
     * 图片参数：图片路径
     */
    @Test
    public void registerFace(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);//人脸模型
            //人脸库路径，从项目中 db/faces-data.db下载到本地
            config.setFaceDbPath("C:/Users/Administrator/Downloads/faces-data.db");
            config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //等待人脸库加载完毕
            Thread.sleep(1000);
            //注册kana1人脸，参数key建议设置为人名
            boolean isSuccss = faceModel.register("kana1","src/main/resources/kana1.jpg");
            log.info("注册结果：{}", isSuccss);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 搜索人脸（1：N）
     * 图片参数：图片路径
     * 注意事项：请先注册人脸
     */
    @Test
    public void searchFace(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);//人脸模型
            //人脸库路径，从项目中 db/faces-data.db下载到本地
            config.setFaceDbPath("C:/Users/Administrator/Downloads/faces-data.db");
            config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //等待人脸库加载完毕
            Thread.sleep(1000);
            FaceResult faceResult = faceModel.search("src/main/resources/kana1.jpg");
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
     * 注意事项：请先注册人脸
     */
    @Test
    public void removeRegisterFace(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);//人脸模型
            //人脸库路径，从项目中 db/faces-data.db下载到本地
            config.setFaceDbPath("C:/Users/Administrator/Downloads/faces-data.db");
            config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //等待人脸库加载完毕
            Thread.sleep(1000);
            //使用注册人脸时的key值删除，可一次性删除单个
            long num = faceModel.removeRegister("kana1");
            //删除全部人脸
            //long num = currentAlgorithm.clearFace();
            log.info("删除成功数量：" + num);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}

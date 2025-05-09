package smartai.examples.face.facerec;

import cn.smartjavaai.face.config.FaceExtractConfig;
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
 * FaceNet人脸算法模型demo
 * 支持功能：人脸特征提取、人脸比对（1：1）
 * @author dwj
 * @date 2025/4/11
 */
@Slf4j
public class FaceNetDemo {

    /**
     * 提取人脸特征(支持多人脸)
     * 默认使用检测模型：FACENET_FEATURE_EXTRACTION
     * 自动裁剪人脸 + 人脸对齐
     */
    @Test
    public void testExtractFeatures(){
        try {
            //人脸特征提取模型
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.FACENET_FEATURE_EXTRACTION);
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            List<float[]> faceResult = faceModel.extractFeatures("src/main/resources/kana1.jpg");
            log.info("人脸特征提取结果：{}", JSONObject.toJSONString(faceResult));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 提取人脸特征(支持多人脸，自定义配置)
     * 自动裁剪人脸 + 人脸对齐
     */
    @Test
    public void testExtractFeaturesWithCustomConfig(){
        try {
            //人脸特征提取模型
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(
                    new FaceModelConfig(FaceModelEnum.FACENET_FEATURE_EXTRACTION));
            //人脸特征提取参数
            FaceExtractConfig extractConfig = new FaceExtractConfig();
            //人脸检测模型配置
            extractConfig.setDetectModelConfig(new FaceModelConfig(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE));
            List<float[]> faceResult = faceModel.extractFeatures("src/main/resources/kana1.jpg",extractConfig);
            log.info("人脸特征提取结果：{}", JSONObject.toJSONString(faceResult));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 提取人脸特征(分数最高人脸)
     * 默认使用检测模型：FACENET_FEATURE_EXTRACTION
     * 自动裁剪人脸 + 人脸对齐
     */
    @Test
    public void testExtractTopFaceFeature(){
        try {
            //人脸特征提取模型
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.FACENET_FEATURE_EXTRACTION);
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            float[] faceResult = faceModel.extractTopFaceFeature("src/main/resources/kana1.jpg");
            log.info("人脸特征提取结果：{}", JSONObject.toJSONString(faceResult));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 提取人脸特征(分数最高人脸，自定义配置)
     * 自动裁剪人脸 + 人脸对齐
     */
    @Test
    public void testExtractTopFaceFeatureWithCustomConfig(){
        try {
            //人脸特征提取模型
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(
                    new FaceModelConfig(FaceModelEnum.FACENET_FEATURE_EXTRACTION));
            //人脸特征提取参数
            FaceExtractConfig extractConfig = new FaceExtractConfig();
            //人脸检测模型配置
            extractConfig.setDetectModelConfig(new FaceModelConfig(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE));
            float[] faceResult = faceModel.extractTopFaceFeature("src/main/resources/kana1.jpg");
            log.info("人脸特征提取结果：{}", JSONObject.toJSONString(faceResult));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 人脸比对（1：1）-在线模型
     * 图片参数：图片路径
     * @throws Exception
     */
    @Test
    public void featureComparison(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.FACENET_FEATURE_EXTRACTION);//人脸模型
            //config.setModelPath("/Users/xxx/Documents/develop/face_model/model_ir_se50.pth");
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
     * 人脸比对（1：1）- 使用离线模型
     * 图片参数：图片路径
     * @throws Exception
     */
    @Test
    public void featureComparisonOffline(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.FACENET_FEATURE_EXTRACTION);//人脸模型
            config.setModelPath("/Users/xxx/Documents/develop/face_model/face_feature.pt");
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //自动裁剪人脸并比对人脸特征
            float similar = faceModel.featureComparison("src/main/resources/kana1.jpg","src/main/resources/kana2.jpg");
            log.info("相似度：{}", similar);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}

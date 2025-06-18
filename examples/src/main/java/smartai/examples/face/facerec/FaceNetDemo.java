package smartai.examples.face.facerec;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.FaceSearchResult;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.face.config.FaceExtractConfig;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.entity.FaceRegisterInfo;
import cn.smartjavaai.face.entity.FaceResult;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.enums.IdStrategy;
import cn.smartjavaai.face.enums.SimilarityType;
import cn.smartjavaai.face.factory.FaceModelFactory;
import cn.smartjavaai.face.model.facerec.FaceModel;
import cn.smartjavaai.face.vector.config.MilvusConfig;
import cn.smartjavaai.face.vector.config.SQLiteConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.milvus.param.MetricType;
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
 * 支持系统：windows 64位，linux 64位，macOS M系列芯片
 * 支持功能：人脸特征提取、人脸比对（1：1）、人脸比对（1：N）、人脸注册
 * 模型下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
 * @author dwj
 * @date 2025/4/11
 */
@Slf4j
public class FaceNetDemo {

    /**
     * 提取人脸特征(多人脸场景)
     * 默认使用检测模型：ULTRA_LIGHT_FAST_GENERIC_FACE
     * 自动裁剪人脸（处理耗时略有增加）
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     */
    @Test
    public void testExtractFeatures(){
        try {
            //人脸特征提取模型
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.FACENET_MODEL);
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            log.info("人脸特征提取模型加载成功");
            //提取图片中所有人脸特征
            R<DetectionResponse> faceResult  = faceModel.extractFeatures("src/main/resources/face/iu_1.jpg");
            if(faceResult.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(faceResult.getData()));
            }else{
                log.info("人脸特征提取失败：{}", faceResult.getMessage());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 提取人脸特征(自定义配置)
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     */
    @Test
    public void testExtractFeaturesWithCustomConfig(){
        try {
            //人脸模型参数
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.FACENET_MODEL);
            //人脸特征提取参数
            FaceExtractConfig extractConfig = new FaceExtractConfig();
            //当关闭人脸裁剪时，程序将跳过人脸检测与裁剪流程，直接进行特征提取，适用于输入已为标准人脸区域的图像，有助于提升处理效率。
            extractConfig.setCropFace(true);
            //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
            extractConfig.setAlign(true);
            //人脸检测模型配置，指定人脸检测模型：ULTRA_LIGHT_FAST_GENERIC_FACE
            FaceModelConfig detectModelConfig = new FaceModelConfig(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);
            //设置人脸检测置信度阈值
            detectModelConfig.setConfidenceThreshold(0.98);
            extractConfig.setDetectModel(FaceModelFactory.getInstance().getModel(detectModelConfig));
            config.setExtractConfig(extractConfig);
            //获取人脸模型
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult = faceModel.extractTopFaceFeature("src/main/resources/face/iu_1.jpg");
            if(featureResult.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult.getMessage());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * 人脸比对1：1（基于图像直接比对）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 比对两张图片中提取的人脸特征。（接口内自动完成）
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void featureComparison(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            //人脸模型
            config.setModelEnum(FaceModelEnum.FACENET_MODEL);
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //基于图像直接比对人脸特征
            float similar = faceModel.featureComparison("src/main/resources/face/iu_1.jpg","src/main/resources/face/iu_2.jpg");
            log.info("相似度：{}", similar);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 人脸比对1：1（基于特征值比对）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 比对两张图片中提取的人脸特征。
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void featureComparison2(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            //人脸模型
            config.setModelEnum(FaceModelEnum.FACENET_MODEL);
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult1 = faceModel.extractTopFaceFeature("src/main/resources/face/iu_1.jpg");
            if(featureResult1.isSuccess()){
                log.info("图片1人脸特征提取成功：{}", JSONObject.toJSONString(featureResult1.getData()));
            }else{
                log.info("图片1人脸特征提取失败：{}", featureResult1.getMessage());
                return;
            }
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult2 = faceModel.extractTopFaceFeature("src/main/resources/face/iu_2.jpg");
            if(featureResult2.isSuccess()){
                log.info("图片2人脸特征提取成功：{}", JSONObject.toJSONString(featureResult2.getData()));
            }else{
                log.info("图片2人脸特征提取失败：{}", featureResult2.getMessage());
                return;
            }
            //计算相似度
            float similar = faceModel.calculSimilar(featureResult1.getData(), featureResult2.getData());
            log.info("相似度：{}", similar);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 人脸注册 + 人脸更新 + 人脸查询 + 人脸删除（使用向量数据库Milvus）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 注册人脸
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void searchFace(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            //人脸模型
            config.setModelEnum(FaceModelEnum.FACENET_MODEL);
            //初始化向量数据库：Milvus数据库配置
            MilvusConfig vectorDBConfig = new MilvusConfig();
            vectorDBConfig.setHost("127.0.0.1");
            vectorDBConfig.setPort(19530);
            //vectorDBConfig.setCollectionName("face5");
            //ID策略：自动生成
            vectorDBConfig.setIdStrategy(IdStrategy.AUTO);
            //索引类型:内积 (Inner Product) 不建议修改
            //vectorDBConfig.setMetricType(MetricType.IP);
            config.setVectorDBConfig(vectorDBConfig);
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //等待加载人脸库结束
            while (!faceModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            log.info("====================人脸注册==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult = faceModel.extractTopFaceFeature("src/main/resources/face/iu_1.jpg");
            if(featureResult.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult.getMessage());
                return;
            }
            //人脸注册信息
            FaceRegisterInfo faceRegisterInfo = new FaceRegisterInfo();
            //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
            JSONObject metadataJson = new JSONObject();
            metadataJson.put("name", "iu");
            metadataJson.put("age", "25");
            faceRegisterInfo.setMetadata(metadataJson.toJSONString());
            //人脸注册，返回人脸库ID
            R<String> registerResult = faceModel.register(faceRegisterInfo, featureResult.getData());
            if(registerResult.isSuccess()){
                log.info("注册成功：ID-{}", registerResult.getData());
            }else{
                log.info("注册失败：{}", registerResult.getMessage());
            }
            /*log.info("====================人脸更新==========================");
            //更新人脸 只支持自定义ID：vectorDBConfig.setIdStrategy(IdStrategy.CUSTOM);
            FaceRegisterInfo updateInfo = new FaceRegisterInfo();
            //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
            JSONObject metadataJsonUpdate = new JSONObject();
            metadataJsonUpdate.put("name", "iu_update");
            metadataJsonUpdate.put("age", "25");
            updateInfo.setMetadata(metadataJsonUpdate.toJSONString());
            //更新必须设置ID,只有
            updateInfo.setId(registerResult.getData());
            faceModel.upsertFace(updateInfo, "src/main/resources/face/iu_2.jpg");
            log.info("更新人脸成功");*/
            log.info("====================人脸查询==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult2 = faceModel.extractTopFaceFeature("src/main/resources/face/iu_3.jpg");
            if(featureResult2.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult2.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult2.getMessage());
                return;
            }
            FaceSearchParams faceSearchParams = new FaceSearchParams();
            faceSearchParams.setTopK(1);
            faceSearchParams.setThreshold(0.8f);

            List<FaceSearchResult> faceSearchResults = faceModel.search(featureResult2.getData(), faceSearchParams);
//                R<DetectionResponse> faceSearchResults = faceModel.search("src/main/resources/face/iu_3.jpg", faceSearchParams);
            log.info("人脸查询结果：{}", JSONArray.toJSONString(faceSearchResults));
            log.info("====================人脸删除==========================");
            faceModel.removeRegister(registerResult.getData());
            log.info("人脸删除成功");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 人脸注册 + 人脸更新 + 人脸查询 + 人脸删除（使用轻量数据库SQLite）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 注册人脸
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void searchFace2(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            //人脸模型
            config.setModelEnum(FaceModelEnum.FACENET_MODEL);
            //初始化向量数据库：Milvus数据库配置
            SQLiteConfig vectorDBConfig = new SQLiteConfig();
            vectorDBConfig.setDbPath("/Users/wenjie/Downloads/face.db");
            vectorDBConfig.setSimilarityType(SimilarityType.IP);
            config.setVectorDBConfig(vectorDBConfig);
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
            //等待加载人脸库结束
            while (!faceModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            log.info("====================人脸注册==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult = faceModel.extractTopFaceFeature("src/main/resources/face/iu_1.jpg");
            if(featureResult.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult.getMessage());
                return;
            }
            //人脸注册信息
            FaceRegisterInfo faceRegisterInfo = new FaceRegisterInfo();
            //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
            JSONObject metadataJson = new JSONObject();
            metadataJson.put("name", "iu");
            metadataJson.put("age", "25");
            faceRegisterInfo.setMetadata(metadataJson.toJSONString());
            //可自定义 ID，若未设置则自动生成。
            //faceRegisterInfo.setId("00001");
            //人脸注册，返回人脸库ID
            R<String> registerResult = faceModel.register(faceRegisterInfo, featureResult.getData());
            if(registerResult.isSuccess()){
                log.info("注册成功：ID-{}", registerResult.getData());
            }else{
                log.info("注册失败：{}", registerResult.getMessage());
            }
            log.info("====================人脸更新==========================");
            FaceRegisterInfo updateInfo = new FaceRegisterInfo();
            //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
            JSONObject metadataJsonUpdate = new JSONObject();
            metadataJsonUpdate.put("name", "iu_update");
            metadataJsonUpdate.put("age", "25");
            updateInfo.setMetadata(metadataJsonUpdate.toJSONString());
            //更新必须设置ID,只有
            updateInfo.setId(registerResult.getData());
            faceModel.upsertFace(updateInfo, "src/main/resources/face/iu_2.jpg");
            log.info("更新人脸成功");
            log.info("====================人脸查询==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult2 = faceModel.extractTopFaceFeature("src/main/resources/face/iu_3.jpg");
            if(featureResult2.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult2.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult2.getMessage());
                return;
            }
            FaceSearchParams faceSearchParams = new FaceSearchParams();
            faceSearchParams.setTopK(1);
            faceSearchParams.setThreshold(0.8f);
            List<FaceSearchResult> faceSearchResults = faceModel.search(featureResult2.getData(), faceSearchParams);
            log.info("人脸查询结果：{}", JSONArray.toJSONString(faceSearchResults));
            log.info("====================人脸删除==========================");
            faceModel.removeRegister(registerResult.getData());
            log.info("人脸删除成功");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 使用离线模型
     * @throws Exception
     */
    @Test
    public void featureComparisonOffline(){
        try {
            FaceModelConfig config = new FaceModelConfig();
            config.setModelEnum(FaceModelEnum.FACENET_MODEL);//人脸模型
            //设置人脸识别模型文件路径，请根据实际情况替换为本地模型文件的绝对路径
            config.setModelPath("/Users/xxx/Documents/develop/face_model/face_feature.pt");
            //人脸特征提取参数
            FaceExtractConfig extractConfig = new FaceExtractConfig();
            FaceModelConfig detectModelConfig = new FaceModelConfig(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);
            //设置人脸检测模型文件路径，请根据实际情况替换为本地模型文件的绝对路径
            detectModelConfig.setModelPath("/Users/xxx/Documents/develop/face_model/ultranet.pt");
            //人脸检测模型配置
            extractConfig.setDetectModel(FaceModelFactory.getInstance().getModel(detectModelConfig));
            config.setExtractConfig(extractConfig);
            FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}

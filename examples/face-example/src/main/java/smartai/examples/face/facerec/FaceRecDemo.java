package smartai.examples.face.facerec;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.entity.FaceRegisterInfo;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.enums.IdStrategy;
import cn.smartjavaai.face.enums.SimilarityType;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.FaceRecModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import cn.smartjavaai.face.vector.config.MilvusConfig;
import cn.smartjavaai.face.vector.config.SQLiteConfig;
import cn.smartjavaai.face.vector.entity.FaceVector;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

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
public class FaceRecDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;


    /**
     * 获取人脸检测模型
     * @return
     */
    public FaceDetModel getFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        config.setModelEnum(FaceDetModelEnum.RETINA_FACE);//人脸检测模型
        config.setConfidenceThreshold(FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD);//只返回相似度大于该值的人脸
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);//用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        config.setDevice(device);
        return FaceDetModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸识别模型
     * @return
     */
    public FaceRecModel getFaceRecModel(){
        FaceRecConfig config = new FaceRecConfig();
        config.setModelEnum(FaceRecModelEnum.FACENET_MODEL);
//        config.setModelPath("/Users/xxx/Documents/develop/model/elasticface.pt");
//        config.setModelPath("/Users/xxx/Documents/develop/model/InsightFace/model_mobilefacenet.pt");
        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
        config.setCropFace(true);
        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
        config.setAlign(true);
        config.setDevice(device);
        //指定人脸检测模型
        config.setDetectModel(getFaceDetModel());
        return FaceRecModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸识别模型(带向量数据库配置)
     * @return
     */
    public FaceRecModel getFaceRecModelWithDbConfig(){
        FaceRecConfig config = new FaceRecConfig();
        config.setModelEnum(FaceRecModelEnum.ELASTIC_FACE_MODEL);//人脸检测模型
        config.setModelPath("/Users/xxx/Documents/develop/model/elasticface.pt");
        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
        config.setCropFace(true);
        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
        config.setAlign(true);
        //指定人脸检测模型
        config.setDetectModel(getFaceDetModel());
        config.setDevice(device);

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
        return FaceRecModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸识别模型(带SQLite数据库配置)
     * @return
     */
    public FaceRecModel getFaceRecModelWithSQLiteConfig(){
        FaceRecConfig config = new FaceRecConfig();
        config.setModelEnum(FaceRecModelEnum.FACENET_MODEL);//人脸检测模型
        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
        config.setCropFace(true);
        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
        config.setAlign(true);
        //指定人脸检测模型
        config.setDetectModel(getFaceDetModel());
        config.setDevice(device);

        //初始化SQLite数据库
        SQLiteConfig vectorDBConfig = new SQLiteConfig();
        vectorDBConfig.setSimilarityType(SimilarityType.IP);
        config.setVectorDBConfig(vectorDBConfig);
        return FaceRecModelFactory.getInstance().getModel(config);
    }
    /**
     * 获取人脸识别模型(带SQLite数据库配置)
     * @return
     */
    public FaceRecModel getFaceRecModelWithSQLiteConfig3(){
        FaceRecConfig config = new FaceRecConfig();
        config.setModelPath("C:\\Users\\yunlong.li\\Downloads\\sf3.0_models\\sf3.0_models");
        config.setModelEnum(FaceRecModelEnum.SEETA_FACE6_MODEL);//人脸检测模型
        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
        config.setCropFace(true);
        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
        config.setAlign(true);
        //指定人脸检测模型
        config.setDetectModel(getFaceDetModel());
        config.setDevice(device);

        //初始化SQLite数据库
        SQLiteConfig vectorDBConfig = new SQLiteConfig();
        vectorDBConfig.setSimilarityType(SimilarityType.COSINE);
        vectorDBConfig.setDbPath("/Users//face3.db");
        config.setVectorDBConfig(vectorDBConfig);
        return FaceRecModelFactory.getInstance().getModel(config);
    }

    /**
     * 提取人脸特征(多人脸场景)
     * 自动裁剪人脸（处理耗时略有增加）
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     */
    @Test
    public void testExtractFeatures(){
        try (FaceRecModel faceRecModel = getFaceRecModel()){
            //提取图片中所有人脸特征
            R<DetectionResponse> faceResult  = faceRecModel.extractFeatures("src/main/resources/iu_1.jpg");
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
     * 人脸比对1：1（基于图像直接比对）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 比对两张图片中提取的人脸特征。（接口内自动完成）
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void featureComparison(){
        try (FaceRecModel faceRecModel = getFaceRecModel()){
            //基于图像直接比对人脸特征
            R<Float> similarResult = faceRecModel.featureComparison("src/main/resources/iu_1.jpg","src/main/resources/iu_2.jpg");
            if(similarResult.isSuccess()){
                //相似度阈值不同模型不同，具体参看文档
                log.info("人脸比对相似度：{}", JSONObject.toJSONString(similarResult.getData()));
            }else{
                log.info("人脸比对失败：{}", similarResult.getMessage());
            }
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
        try (FaceRecModel faceRecModel = getFaceRecModel()){
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult1 = faceRecModel.extractTopFaceFeature("src/main/resources/iu_1.jpg");
            if(featureResult1.isSuccess()){
                log.info("图片1人脸特征提取成功：{}", JSONObject.toJSONString(featureResult1.getData()));
            }else{
                log.info("图片1人脸特征提取失败：{}", featureResult1.getMessage());
                return;
            }
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult2 = faceRecModel.extractTopFaceFeature("src/main/resources/iu_2.jpg");
            if(featureResult2.isSuccess()){
                log.info("图片2人脸特征提取成功：{}", JSONObject.toJSONString(featureResult2.getData()));
            }else{
                log.info("图片2人脸特征提取失败：{}", featureResult2.getMessage());
                return;
            }
            //计算相似度
            float similar = faceRecModel.calculSimilar(featureResult1.getData(), featureResult2.getData());
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
        try (FaceRecModel faceRecModel = getFaceRecModelWithDbConfig()){
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            log.info("====================人脸注册==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult = faceRecModel.extractTopFaceFeature("src/main/resources/iu_1.jpg");
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
            //faceRegisterInfo.setId("001");
            faceRegisterInfo.setMetadata(metadataJson.toJSONString());
            //人脸注册，返回人脸库ID
            R<String> registerResult = faceRecModel.register(faceRegisterInfo, featureResult.getData());
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
            faceRecModel.upsertFace(updateInfo, "src/main/resources/iu_2.jpg");
            log.info("更新人脸成功");*/
            log.info("====================人脸查询==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult2 = faceRecModel.extractTopFaceFeature("src/main/resources/iu_3.jpg");
            if(featureResult2.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult2.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult2.getMessage());
                return;
            }
            FaceSearchParams faceSearchParams = new FaceSearchParams();
            faceSearchParams.setTopK(1);
            faceSearchParams.setThreshold(0.8f);

            List<FaceSearchResult> faceSearchResults = faceRecModel.search(featureResult2.getData(), faceSearchParams);
//                R<DetectionResponse> faceSearchResults = faceModel.search("src/main/resources/face/iu_3.jpg", faceSearchParams);
            log.info("人脸查询结果：{}", JSONArray.toJSONString(faceSearchResults));
            log.info("====================人脸删除==========================");
            faceRecModel.removeRegister(registerResult.getData());
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
        try (FaceRecModel faceRecModel = getFaceRecModelWithSQLiteConfig()){
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            log.info("====================人脸注册==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult = faceRecModel.extractTopFaceFeature("src/main/resources/iu_1.jpg");
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
            R<String> registerResult = faceRecModel.register(faceRegisterInfo, featureResult.getData());
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
            faceRecModel.upsertFace(updateInfo, "src/main/resources/iu_2.jpg");
            log.info("更新人脸成功");
            log.info("====================人脸查询==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult2 = faceRecModel.extractTopFaceFeature("src/main/resources/iu_3.jpg");
            if(featureResult2.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult2.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult2.getMessage());
                return;
            }
            FaceSearchParams faceSearchParams = new FaceSearchParams();
            faceSearchParams.setTopK(1);
            faceSearchParams.setThreshold(0.8f);
            List<FaceSearchResult> faceSearchResults = faceRecModel.search(featureResult2.getData(), faceSearchParams);
            log.info("人脸查询结果：{}", JSONArray.toJSONString(faceSearchResults));
            log.info("====================人脸删除==========================");
            faceRecModel.removeRegister(registerResult.getData());
            log.info("人脸删除成功");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @Test
    public void searchFace3(){
        try (FaceRecModel faceRecModel = getFaceRecModelWithSQLiteConfig3()){
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            log.info("====================人脸注册==========================");
            insertFace(faceRecModel, "src/main/resources/iu_1.jpg", "刘亦菲");
            
            FaceSearchParams faceSearchParams = new FaceSearchParams();
            faceSearchParams.setTopK(5);
            faceSearchParams.setThreshold(0.8f);
            List<FaceSearchResult> faceSearchResults = faceRecModel.searchByTopFace("src/main/resources/iu_3.jpg", faceSearchParams).getData();
            log.info("人脸查询结果：{}", JSONArray.toJSONString(faceSearchResults));
            
//            log.info("====================人脸删除==========================");
//            faceRecModel.removeRegister(registerResult.getData());
//            log.info("人脸删除成功");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void insertFace(FaceRecModel faceRecModel, String featureResult, String name) {
        //人脸注册信息
        FaceRegisterInfo faceRegisterInfo = new FaceRegisterInfo();
        //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("name", name);
        metadataJson.put("age", "25");
        faceRegisterInfo.setMetadata(metadataJson.toJSONString());
        //可自定义 ID，若未设置则自动生成。
        //faceRegisterInfo.setId("00001");
        //人脸注册，返回人脸库ID
        R<String> registerResult = faceRecModel.register(faceRegisterInfo, featureResult);
        if(registerResult.isSuccess()){
            log.info("注册成功：ID-{}", registerResult.getData());
        }else{
            log.info("注册失败：{}", registerResult.getMessage());
        }
    }


    /**
     * 获取人脸信息
     */
    @Test
    public void getFaceInfo(){
        //使用ID获取人脸信息
        try (FaceRecModel faceRecModel = getFaceRecModelWithSQLiteConfig()){
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            //ID需改为你需要查询的ID
            R<FaceVector> faceInfoResult = faceRecModel.getFaceInfoById("9c4c316d53a74b1184195c1714c250c4");
            if(faceInfoResult.isSuccess()){
                log.info("人脸信息：{}", JSONObject.toJSONString(faceInfoResult.getData()));
            }else{
                log.info("获取人脸信息失败：{}", faceInfoResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取人脸信息
     */
    @Test
    public void listFaces(){
        //使用ID获取人脸信息
        try (FaceRecModel faceRecModel = getFaceRecModelWithDbConfig()){
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            //ID需改为你需要查询的ID
            R<List<FaceVector>> faceInfoResult = faceRecModel.listFaces(1, 10);
            if(faceInfoResult.isSuccess()){
                log.info("人脸信息：{}", JSONObject.toJSONString(faceInfoResult.getData()));
            }else{
                log.info("获取人脸信息失败：{}", faceInfoResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}

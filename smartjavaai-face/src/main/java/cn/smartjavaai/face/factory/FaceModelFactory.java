package cn.smartjavaai.face.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.facerec.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人脸检测识别模型工厂
 * @author dwj
 */
@Slf4j
public class FaceModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile FaceModelFactory instance;

    private static final ConcurrentHashMap<String, FaceModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 算法注册表
     */
    private static final Map<String, Class<? extends FaceModel>> registry =
            new ConcurrentHashMap<>();


    public static FaceModelFactory getInstance() {
        if (instance == null) {
            synchronized (FaceModelFactory.class) {
                if (instance == null) {
                    instance = new FaceModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册算法
     * @param name
     * @param clazz
     */
    private static void registerAlgorithm(String name, Class<? extends FaceModel> clazz) {
        registry.put(name.toLowerCase(), clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public FaceModel getModel(FaceModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new FaceException("未配置人脸模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum().name(), k -> {
            return createFaceModel(config);
        });
    }

    /**
     * 获取默认模型
     * @return
     */
    public FaceModel getModel() {
        // 初始化默认配置
        FaceModelConfig config = new FaceModelConfig();
        config.setModelEnum(FaceModelEnum.RETINA_FACE);
        config.setConfidenceThreshold(FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD);
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return getModel(config);
    }

    /**
     * 使用ModelConfig创建算法
     * @param config
     * @return
     */
    private FaceModel createFaceModel(FaceModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum().getModelClassName().toLowerCase());
        if(clazz == null){
            throw new FaceException("Unsupported algorithm");
        }
        FaceModel algorithm = null;
        try {
            algorithm = (FaceModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FaceException(e);
        }
        algorithm.loadModel(config);
        return algorithm;
    }


    /**
     * 获取轻量级人脸模型
     * @return
     */
    public FaceModel getLightFaceModel() {
        // 初始化默认配置
        FaceModelConfig config = new FaceModelConfig();
        config.setModelEnum(FaceModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);
        config.setConfidenceThreshold(FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD);
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return getModel(config);
    }


    // 初始化默认算法
    static {
        registerAlgorithm("retinafacemodel", RetinaFaceModel.class);
        registerAlgorithm("ultralightfastgenericfacemodel", UltraLightFastGenericFaceModel.class);
        //人脸特征提取
        registerAlgorithm("featureextractionmodel", FeatureExtractionModel.class);
        registerAlgorithm("seetaface6model", SeetaFace6Model.class);
        log.info("缓存目录：{}", Config.getCachePath());
    }

}

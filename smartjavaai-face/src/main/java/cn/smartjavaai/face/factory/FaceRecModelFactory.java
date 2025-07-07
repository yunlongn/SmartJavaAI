package cn.smartjavaai.face.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
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
public class FaceRecModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile FaceRecModelFactory instance;

    private static final ConcurrentHashMap<FaceRecModelEnum, FaceRecModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<FaceRecModelEnum, Class<? extends FaceRecModel>> registry =
            new ConcurrentHashMap<>();


    public static FaceRecModelFactory getInstance() {
        if (instance == null) {
            synchronized (FaceRecModelFactory.class) {
                if (instance == null) {
                    instance = new FaceRecModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册模型
     * @param recModelEnum
     * @param clazz
     */
    private static void registerAlgorithm(FaceRecModelEnum recModelEnum, Class<? extends FaceRecModel> clazz) {
        registry.put(recModelEnum, clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public FaceRecModel getModel(FaceRecConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new FaceException("未配置人脸模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createFaceModel(config);
        });
    }


    /**
     * 使用ModelConfig创建模型
     * @param config
     * @return
     */
    private FaceRecModel createFaceModel(FaceRecConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new FaceException("Unsupported model");
        }
        FaceRecModel algorithm = null;
        try {
            algorithm = (FaceRecModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FaceException(e);
        }
        algorithm.loadModel(config);
        return algorithm;
    }


    // 初始化默认算法
    static {
        registerAlgorithm(FaceRecModelEnum.FACENET_MODEL, CommonFaceRecModel.class);
        registerAlgorithm(FaceRecModelEnum.INSIGHT_FACE_IRSE50_MODEL, CommonFaceRecModel.class);
        registerAlgorithm(FaceRecModelEnum.INSIGHT_FACE_MOBILE_FACENET_MODEL, CommonFaceRecModel.class);
        registerAlgorithm(FaceRecModelEnum.ELASTIC_FACE_MODEL, CommonFaceRecModel.class);
        registerAlgorithm(FaceRecModelEnum.SEETA_FACE6_MODEL, SeetaFace6FaceRecModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }

}

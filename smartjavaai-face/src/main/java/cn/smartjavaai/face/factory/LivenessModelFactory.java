package cn.smartjavaai.face.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.facerec.*;
import cn.smartjavaai.face.model.liveness.LivenessDetModel;
import cn.smartjavaai.face.model.liveness.Seetaface6LivenessModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 活体检测模型工厂
 * @author dwj
 */
@Slf4j
public class LivenessModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile LivenessModelFactory instance;

    private static final ConcurrentHashMap<String, LivenessDetModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 算法注册表
     */
    private static final Map<String, Class<? extends LivenessDetModel>> registry =
            new ConcurrentHashMap<>();


    public static LivenessModelFactory getInstance() {
        if (instance == null) {
            synchronized (LivenessModelFactory.class) {
                if (instance == null) {
                    instance = new LivenessModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册模型
     * @param name
     * @param clazz
     */
    private static void registerModel(String name, Class<? extends LivenessDetModel> clazz) {
        registry.put(name.toLowerCase(), clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public LivenessDetModel getModel(LivenessConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new FaceException("未配置活体检测模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum().name(), k -> {
            return createFaceModel(config);
        });
    }

    /**
     * 使用ModelConfig创建算法
     * @param config
     * @return
     */
    private LivenessDetModel createFaceModel(LivenessConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum().getModelClassName().toLowerCase());
        if(clazz == null){
            throw new FaceException("Unsupported algorithm");
        }
        LivenessDetModel model = null;
        try {
            model = (LivenessDetModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FaceException(e);
        }
        model.loadModel(config);
        return model;
    }


    // 初始化默认算法
    static {
        registerModel("seetaface6model", Seetaface6LivenessModel.class);
        log.info("缓存目录：{}", Config.getCachePath());
    }

}

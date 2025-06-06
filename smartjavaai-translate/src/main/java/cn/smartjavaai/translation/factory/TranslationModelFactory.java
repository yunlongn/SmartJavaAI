package cn.smartjavaai.translation.factory;

import cn.smartjavaai.common.config.Config;


import cn.smartjavaai.translation.config.MachineTranslationModelConfig;
import cn.smartjavaai.translation.exception.TranslationException;
import cn.smartjavaai.translation.model.common.TracedTranslationModel;
import cn.smartjavaai.translation.model.common.TranslationCommonModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 机器翻译模型工厂
 * @author dwj
 */
@Slf4j
public class TranslationModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile TranslationModelFactory instance;

    private static final ConcurrentHashMap<String, TranslationCommonModel> commonDetModelMap = new ConcurrentHashMap<>();



    /**
     * 检测模型注册表
     */
    private static final Map<String, Class<? extends TranslationCommonModel>> commonDetRegistry =
            new ConcurrentHashMap<>();


    public static TranslationModelFactory getInstance() {
        if (instance == null) {
            synchronized (TranslationModelFactory.class) {
                if (instance == null) {
                    instance = new TranslationModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册通用检测模型
     * @param name
     * @param clazz
     */
    private static void registerCommonDetModel(String name, Class<? extends TranslationCommonModel> clazz) {
        commonDetRegistry.put(name.toLowerCase(), clazz);
    }

    /**
     * 获取检测模型（通过配置）
     * @param config
     * @return
     */
    public TranslationCommonModel getDetModel(MachineTranslationModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new TranslationException("未配置OCR模型");
        }
        return commonDetModelMap.computeIfAbsent(config.getModelEnum().name(), k -> {
            return createCommonDetModel(config);
        });
    }


    /**
     * 创建OCR通用检测模型
     * @param config
     * @return
     */
    private TranslationCommonModel createCommonDetModel(MachineTranslationModelConfig config) {
        Class<?> clazz = commonDetRegistry.get(config.getModelEnum().name().toLowerCase());
        if(clazz == null){
            throw new TranslationException("Unsupported model");
        }
        TranslationCommonModel model = null;
        try {
            model = (TranslationCommonModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TranslationException(e);
        }
        model.loadModel(config);
        return model;
    }


    // 初始化默认算法
    static {
        registerCommonDetModel("TRACED_TRANSLATION_CPU", TracedTranslationModel.class);

        log.info("缓存目录：{}", Config.getCachePath());
    }

}

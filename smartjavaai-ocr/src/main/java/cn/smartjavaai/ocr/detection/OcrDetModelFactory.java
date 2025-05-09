package cn.smartjavaai.ocr.detection;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.ppv4.model.PaddleOCRV4DetModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人脸算法工厂
 * @author dwj
 */
@Slf4j
public class OcrDetModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile OcrDetModelFactory instance;

    private static final ConcurrentHashMap<String, OcrDetModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 算法注册表
     */
    private static final Map<String, Class<? extends OcrDetModel>> registry =
            new ConcurrentHashMap<>();


    public static OcrDetModelFactory getInstance() {
        if (instance == null) {
            synchronized (OcrDetModelFactory.class) {
                if (instance == null) {
                    instance = new OcrDetModelFactory();
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
    private static void registerModel(String name, Class<? extends OcrDetModel> clazz) {
        registry.put(name.toLowerCase(), clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public OcrDetModel getModel(OcrDetModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new OcrException("未配置OCR模型");
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
    private OcrDetModel createFaceModel(OcrDetModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum().name().toLowerCase());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        OcrDetModel algorithm = null;
        try {
            algorithm = (OcrDetModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        algorithm.loadModel(config);
        return algorithm;
    }


    // 初始化默认算法
    static {
        registerModel("PADDLEOCR_V4_DET_MODEL", PaddleOCRV4DetModel.class);
        log.info("缓存目录：{}", Config.getCachePath());
    }

}

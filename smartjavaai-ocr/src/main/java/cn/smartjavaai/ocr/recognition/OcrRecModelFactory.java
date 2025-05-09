package cn.smartjavaai.ocr.recognition;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.ppv4.model.PaddleOCRV4DetModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OCR模型工厂
 * @author dwj
 */
@Slf4j
public class OcrRecModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile OcrRecModelFactory instance;

    private static final ConcurrentHashMap<String, OcrRecModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 算法注册表
     */
    private static final Map<String, Class<? extends OcrRecModel>> registry =
            new ConcurrentHashMap<>();


    public static OcrRecModelFactory getInstance() {
        if (instance == null) {
            synchronized (OcrRecModelFactory.class) {
                if (instance == null) {
                    instance = new OcrRecModelFactory();
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
    private static void registerModel(String name, Class<? extends OcrRecModel> clazz) {
        registry.put(name.toLowerCase(), clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public OcrRecModel getModel(OcrRecModelConfig config) {
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
    private OcrRecModel createFaceModel(OcrRecModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum().name().toLowerCase());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        OcrRecModel algorithm = null;
        try {
            algorithm = (OcrRecModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        algorithm.loadModel(config);
        return algorithm;
    }


    // 初始化默认算法
    static {
        //registerModel("PADDLEOCR_V4_REC_MODEL", PaddleOCRV4DetModel.class);
        log.info("缓存目录：{}", Config.getCachePath());
    }

}

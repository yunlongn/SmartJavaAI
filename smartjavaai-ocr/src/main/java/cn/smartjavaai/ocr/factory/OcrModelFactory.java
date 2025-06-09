package cn.smartjavaai.ocr.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.detect.PpOCRV5DetModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.direction.PPOCRMobileV2Model;
import cn.smartjavaai.ocr.model.common.recognize.PpOCRV5RecModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OCR模型工厂
 * @author dwj
 */
@Slf4j
public class OcrModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile OcrModelFactory instance;

    private static final ConcurrentHashMap<String, OcrCommonDetModel> commonDetModelMap = new ConcurrentHashMap<>();


    private static final ConcurrentHashMap<String, OcrCommonRecModel> commonRecModelMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, OcrDirectionModel> directionModelMap = new ConcurrentHashMap<>();

    /**
     * 检测模型注册表
     */
    private static final Map<String, Class<? extends OcrCommonDetModel>> commonDetRegistry =
            new ConcurrentHashMap<>();

    /**
     * 识别模型注册表
     */
    private static final Map<String, Class<? extends OcrCommonRecModel>> commonRecRegistry =
            new ConcurrentHashMap<>();

    /**
     * 方向分类模型注册表
     */
    private static final Map<String, Class<? extends OcrDirectionModel>> directionRegistry =
            new ConcurrentHashMap<>();


    public static OcrModelFactory getInstance() {
        if (instance == null) {
            synchronized (OcrModelFactory.class) {
                if (instance == null) {
                    instance = new OcrModelFactory();
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
    private static void registerCommonDetModel(String name, Class<? extends OcrCommonDetModel> clazz) {
        commonDetRegistry.put(name.toLowerCase(), clazz);
    }

    /**
     * 注册通用识别模型
     * @param name
     * @param clazz
     */
    private static void registerCommonRecModel(String name, Class<? extends OcrCommonRecModel> clazz) {
        commonRecRegistry.put(name.toLowerCase(), clazz);
    }

    /**
     * 注册通用方向分类模型
     * @param name
     * @param clazz
     */
    private static void registerDirectionModel(String name, Class<? extends OcrDirectionModel> clazz) {
        directionRegistry.put(name.toLowerCase(), clazz);
    }


    /**
     * 获取检测模型（通过配置）
     * @param config
     * @return
     */
    public OcrCommonDetModel getDetModel(OcrDetModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new OcrException("未配置OCR模型");
        }
        return commonDetModelMap.computeIfAbsent(config.getModelEnum().name(), k -> {
            return createCommonDetModel(config);
        });
    }

    /**
     * 获取识别模型（通过配置）
     * @param config
     * @return
     */
    public OcrCommonRecModel getRecModel(OcrRecModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getRecModelEnum())){
            throw new OcrException("未配置OCR模型");
        }
        return commonRecModelMap.computeIfAbsent(config.getRecModelEnum().name(), k -> {
            return createCommonRecModel(config);
        });
    }

    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public OcrDirectionModel getDirectionModel(DirectionModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new OcrException("未配置OCR模型");
        }
        return directionModelMap.computeIfAbsent(config.getModelEnum().name(), k -> {
            return createDirectionModel(config);
        });
    }



    /**
     * 创建OCR通用检测模型
     * @param config
     * @return
     */
    private OcrCommonDetModel createCommonDetModel(OcrDetModelConfig config) {
        Class<?> clazz = commonDetRegistry.get(config.getModelEnum().name().toLowerCase());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        OcrCommonDetModel model = null;
        try {
            model = (OcrCommonDetModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        model.loadModel(config);
        return model;
    }


    /**
     * 创建OCR通用识别模型
     * @param config
     * @return
     */
    private OcrCommonRecModel createCommonRecModel(OcrRecModelConfig config) {
        Class<?> clazz = commonRecRegistry.get(config.getRecModelEnum().name().toLowerCase());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        OcrCommonRecModel model = null;
        try {
            model = (OcrCommonRecModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        model.loadModel(config);
        return model;
    }

    /**
     * 创建OCR方向分类模型
     * @param config
     * @return
     */
    private OcrDirectionModel createDirectionModel(DirectionModelConfig config) {
        Class<?> clazz = directionRegistry.get(config.getModelEnum().name().toLowerCase());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        OcrDirectionModel model = null;
        try {
            model = (OcrDirectionModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        model.loadModel(config);
        return model;
    }


    // 初始化默认算法
    static {
        registerCommonDetModel("PADDLEOCR_V5_DET_MODEL", PpOCRV5DetModel.class);
        registerCommonRecModel("PADDLEOCR_V5_REC_MODEL", PpOCRV5RecModel.class);
        registerDirectionModel("CH_PPOCR_MOBILE_V2_CLS", PPOCRMobileV2Model.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }

}

package cn.smartjavaai.ocr.enums;

/**
 * OCR检测模型枚举
 * @author dwj
 * @date 2025/4/4
 */
public enum CommonDetModelEnum {

    PADDLEOCR_V5_DET_MODEL;


    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static CommonDetModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (CommonDetModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}

package cn.smartjavaai.ocr.recognition;

/**
 * OCR识别模型枚举
 * @author dwj
 * @date 2025/4/4
 */
public enum OcrRecModelEnum {

    PADDLEOCR_V4_REC_MODEL;


    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static OcrRecModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (OcrRecModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}

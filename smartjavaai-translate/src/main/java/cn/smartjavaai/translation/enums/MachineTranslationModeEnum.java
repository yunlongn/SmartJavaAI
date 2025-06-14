package cn.smartjavaai.translation.enums;
/**
 * 机器翻译模型枚举
 * @author lwx
 * @date 2025/6/05
 */
public enum MachineTranslationModeEnum {

    TRACED_TRANSLATION_CPU;

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static MachineTranslationModeEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (MachineTranslationModeEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }
}

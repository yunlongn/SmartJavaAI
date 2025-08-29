package cn.smartjavaai.action.enums;

/**
 * 人类动作识别模型枚举
 * @author dwj
 */
public enum ActionRecModelEnum {

    VIT_BASE_PATCH16_224("djl://ai.djl.pytorch/Human-Action-Recognition-VIT-Base-patch16-224"),

    INCEPTIONV3_KINETICS400(""),

    INCEPTIONV1_KINETICS400(""),

    RESNET18_V1B_KINETICS400(""),

    RESNET34_V1B_KINETICS400(""),

    RESNET50_V1B_KINETICS400(""),

    RESNET101_V1B_KINETICS400(""),

    RESNET152_V1B_KINETICS400("");

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static ActionRecModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (ActionRecModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

    private final String modelUri;

    ActionRecModelEnum(String modelUri) {
        this.modelUri = modelUri;
    }

    public String getModelUri() {
        return modelUri;
    }

}

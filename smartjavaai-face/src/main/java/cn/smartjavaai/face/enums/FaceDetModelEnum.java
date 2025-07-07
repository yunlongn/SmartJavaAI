package cn.smartjavaai.face.enums;

/**
 * 人脸检测模型枚举
 * @author dwj
 */
public enum FaceDetModelEnum {

    RETINA_FACE("RetinaFaceModel"),
    ULTRA_LIGHT_FAST_GENERIC_FACE("UltraLightFastGenericFaceModel"),
    SEETA_FACE6_MODEL("SeetaFace6Model");

    private final String modelClassName;

    FaceDetModelEnum(String modelClassName) {
        this.modelClassName = modelClassName;
    }

    public String getModelClassName() {
        return modelClassName;
    }

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static FaceDetModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (FaceDetModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}

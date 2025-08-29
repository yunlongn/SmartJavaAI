package cn.smartjavaai.face.enums;

/**
 * 人脸识别模型枚举
 * @author dwj
 */
public enum FaceRecModelEnum {

    FACENET_MODEL("FaceNetModel"),
    SEETA_FACE6_MODEL("SeetaFace6Model"),
    SEETA_FACE6_LIGHT_MODEL("SeetaFace6Model"),
    INSIGHT_FACE_IRSE50_MODEL("InsightFaceIRSE50Model"),
    INSIGHT_FACE_MOBILE_FACENET_MODEL("InsightFaceMobilefacenetModel"),
    ELASTIC_FACE_MODEL("ElasticFaceModel");

    private final String modelClassName;

    FaceRecModelEnum(String modelClassName) {
        this.modelClassName = modelClassName;
    }

    public String getModelClassName() {
        return modelClassName;
    }

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static FaceRecModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (FaceRecModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}

package cn.smartjavaai.instanceseg.enums;

/**
 * 实例分割模型枚举
 * @author dwj
 */
public enum InstanceSegModelEnum {

    SEG_YOLO11N_PYTORCH("djl://ai.djl.pytorch/yolo11n-seg"),

    SEG_YOLOV8N_PYTORCH("djl://ai.djl.pytorch/yolo11n-seg"),

    SEG_YOLO11N_ONNX("djl://ai.djl.onnxruntime/yolo11n-seg"),

    SEG_YOLOV8N_ONNX("djl://ai.djl.onnxruntime/yolov8n-seg"),

    SEG_MASK_RCNN("djl://ai.djl.mxnet/mask_rcnn");


    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static InstanceSegModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (InstanceSegModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

    private final String modelUri;

    InstanceSegModelEnum(String modelUri) {
        this.modelUri = modelUri;
    }

    public String getModelUri() {
        return modelUri;
    }

}

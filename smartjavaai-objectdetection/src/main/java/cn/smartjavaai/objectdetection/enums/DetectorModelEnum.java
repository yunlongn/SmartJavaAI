package cn.smartjavaai.objectdetection.enums;

/**
 * 目标检测模型枚举
 * @author dwj
 * @date 2025/4/4
 */
public enum DetectorModelEnum {

    // resnet50 系列
    SSD_300_RESNET50("ai.djl.pytorch/ssd/0.0.1/ssd_300_resnet50"),
    SSD_512_RESNET50_V1_VOC("ai.djl.mxnet/ssd/0.0.1/ssd_512_resnet50_v1_voc"),

    // vgg16 系列
    SSD_512_VGG16_ATROUS_COCO("ai.djl.mxnet/ssd/0.0.1/ssd_512_vgg16_atrous_coco"),
    SSD_300_VGG16_ATROUS_VOC("ai.djl.mxnet/ssd/0.0.1/ssd_300_vgg16_atrous_voc"),

    // mobilenet 系列
    SSD_512_MOBILENET1_VOC("ai.djl.mxnet/ssd/0.0.1/ssd_512_mobilenet1.0_voc"),

    // YOLO 系列
    YOLOV8N("ai.djl.pytorch/yolov8n/0.0.1/yolov8n"),
    YOLO11N("ai.djl.pytorch/yolo11n/0.0.1/yolo11n"),
    YOLOV5S("ai.djl.pytorch/yolo5s/0.0.1/yolov5s"),
    YOLOV5S_ONNXRUNTIME("ai.djl.onnxruntime/yolo5s/0.0.1/yolo5s"),
    YOLO("ai.djl.mxnet/yolo/0.0.1/yolo"),

    // YOLOv3 变体
    YOLO3_DARKNET_VOC_416("ai.djl.mxnet/yolo/0.0.1/yolo3_darknet_voc_416"),
    YOLO3_MOBILENET_VOC_320("ai.djl.mxnet/yolo/0.0.1/yolo3_mobilenet_voc_320"),
    YOLO3_MOBILENET_VOC_416("ai.djl.mxnet/yolo/0.0.1/yolo3_mobilenet_voc_41"),
    YOLO3_DARKNET_COCO_320("ai.djl.mxnet/yolo/0.0.1/yolo3_darknet_coco_320"),
    YOLO3_DARKNET_COCO_416("ai.djl.mxnet/yolo/0.0.1/yolo3_darknet_coco_416"),
    YOLO3_DARKNET_COCO_608("ai.djl.mxnet/yolo/0.0.1/yolo3_darknet_coco_608"),
    YOLO3_MOBILENET_COCO_320("ai.djl.mxnet/yolo/0.0.1/yolo3_mobilenet_coco_320"),
    YOLO3_MOBILENET_COCO_416("ai.djl.mxnet/yolo/0.0.1/yolo3_mobilenet_coco_416"),
    YOLO3_MOBILENET_COCO_608("ai.djl.mxnet/yolo/0.0.1/yolo3_mobilenet_coco_608"),

    YOLOV12_OFFICIAL(""),
    YOLOV8_OFFICIAL(""),

    YOLOV8_CUSTOM(""),

    YOLOV12_CUSTOM("");

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static DetectorModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (DetectorModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

    private final String modelUri;

    DetectorModelEnum(String modelUri) {
        this.modelUri = modelUri;
    }

    public String getModelUri() {
        return modelUri;
    }

}

package cn.smartjavaai.face.enums;

import lombok.Data;

/**
 * 人脸检测模型枚举
 * @author dwj
 */
public enum FaceDetModelEnum {

    RETINA_FACE("PyTorch",0,0, "https://resources.djl.ai/test-models/pytorch/retinaface.zip"),
    RETINA_FACE_ONNX("OnnxRuntime",0,0, null),
    RETINA_FACE_640_ONNX("OnnxRuntime",640,640, null),
    RETINA_FACE_320_ONNX("OnnxRuntime",320,320, null),
    RETINA_FACE_720_1280_ONNX("OnnxRuntime",720,1280, null),
    RETINA_FACE_MOBILE_ONNX("OnnxRuntime",0,0, null),
    RETINA_FACE_MOBILE_320_ONNX("OnnxRuntime",320,320, null),
    RETINA_FACE_MOBILE_640_ONNX("OnnxRuntime",640,640, null),
    RETINA_FACE_MOBILE_720_1280_ONNX("OnnxRuntime",720,1080, null),
    ULTRA_LIGHT_FAST_GENERIC_FACE("PyTorch",0,0, "https://resources.djl.ai/test-models/pytorch/ultranet.zip"),
    SEETA_FACE6_MODEL(null,0,0, null),
    YOLOV8_FACE("OnnxRuntime",640,640, null),
    YOLOV5_FACE_640("OnnxRuntime", 640,640, null),
    YOLOV5_FACE_320("OnnxRuntime",320,320, null),
    SCRFD_160("OnnxRuntime",160,160, null),
    SCRFD_320("OnnxRuntime",320,320, null),
    SCRFD_640("OnnxRuntime",640,640, null),
    SCRFD_1280("OnnxRuntime",1280,1280, null),

    MTCNN("OnnxRuntime",1280,1280, null);




    /**
     * 模型输入尺寸：宽
     */
    private final int inputWidth;

    /**
     * 模型输入尺寸：高
     */
    private final int inputHeight;

    /**
     * 模型地址
     */
    private final String modelUrl;

    /**
     * 模型引擎
     */
    private final String engine;

    FaceDetModelEnum(String engine, int inputWidth, int inputHeight, String modelUrl) {
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
        this.modelUrl = modelUrl;
        this.engine = engine;
    }

    public int getInputWidth() {
        return inputWidth;
    }

    public int getInputHeight() {
        return inputHeight;
    }

    public String getModelUrl() {
        return modelUrl;
    }

    public String getEngine() {
        return engine;
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

package cn.smartjavaai.face.config;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.constant.LivenessConstant;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 活体检测模型配置
 * @author dwj
 */
@Data
public class LivenessConfig {

    /**
     * 活体检测模型枚举
     */
    private LivenessModelEnum modelEnum = LivenessModelEnum.SEETA_FACE6_MODEL;

    /**
     * 模型路径
     */
    private String modelPath;

    /**
     * 设备类型
     */
    private DeviceEnum device;

    /**
     * 人脸检测模型
     */
    private FaceDetModel detectModel;

    /**
     * 个性化配置（按模型类型动态解析）
     */
    private Map<String, Object> customParams = new HashMap<>();


    /**
     * 视频检测帧数
     */
    private int frameCount = LivenessConstant.DEFAULT_FRAME_COUNT;

    /**
     * 真人阈值
     */
    private Float realityThreshold;

    public LivenessConfig() {
    }

    public LivenessConfig(LivenessModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }

    public LivenessConfig(LivenessModelEnum modelEnum, String modelPath) {
        this.modelEnum = modelEnum;
        this.modelPath = modelPath;
    }

    public LivenessConfig(String modelPath) {
        this.modelPath = modelPath;
    }

    // 可选封装方法，便于类型转换和调用
    public <T> T getCustomParam(String key, Class<T> clazz) {
        Object value = customParams.get(key);
        if (value == null) return null;
        return clazz.cast(value);
    }

    /**
     * 添加个性化配置项
     */
    public void putCustomParam(String key, Object value) {
        if (customParams == null) {
            customParams = new HashMap<>();
        }
        customParams.put(key, value);
    }
}

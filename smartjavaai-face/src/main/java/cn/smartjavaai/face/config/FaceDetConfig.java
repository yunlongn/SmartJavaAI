package cn.smartjavaai.face.config;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 人脸检测模型配置
 * @author dwj
 */
@Data
public class FaceDetConfig {

    /**
     * 人脸检测模型枚举
     */
    private FaceDetModelEnum modelEnum;

    /**
     * 置信度阈值
     */
    private double confidenceThreshold = FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD;


    /**
     * 非极大抑制阈值 作用：消除重叠检测框，保留最优结果
     */
    private double nmsThresh = FaceDetectConstant.NMS_THRESHOLD;

    /**
     * 模型路径
     */
    private String modelPath;

    /**
     * 设备类型
     */
    private DeviceEnum device;

    /**
     * 个性化配置（按模型类型动态解析）
     */
    private Map<String, Object> customParams = new HashMap<>();


    public FaceDetConfig() {
    }

    public FaceDetConfig(FaceDetModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }

    public FaceDetConfig(FaceDetModelEnum modelEnum, String modelPath) {
        this.modelEnum = modelEnum;
        this.modelPath = modelPath;
    }

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

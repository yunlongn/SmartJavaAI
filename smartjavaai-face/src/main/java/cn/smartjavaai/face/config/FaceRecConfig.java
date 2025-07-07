package cn.smartjavaai.face.config;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import cn.smartjavaai.face.vector.config.VectorDBConfig;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 人脸检测识别模型配置
 * @author dwj
 */
@Data
public class FaceRecConfig {

    /**
     * 人脸模型枚举
     */
    private FaceRecModelEnum modelEnum;

    /**
     * 置信度阈值
     */
    private double confidenceThreshold = FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD;

    /**
     * 相似度阈值 作用：判断是否为同一人脸
     */
    //private double similarityThreshold = 0D;

    /**
     * 非极大抑制阈值 作用：消除重叠检测框，保留最优结果
     */
    private double nmsThresh = FaceDetectConstant.NMS_THRESHOLD;

    /**
     * 模型路径
     */
    private String modelPath;

    /**
     * 人脸库路径
     */
    private String faceDbPath;

    /**
     * 设备类型
     */
    private DeviceEnum device;


    /**
     * 向量数据库配置
     */
    private VectorDBConfig vectorDBConfig;

    /**
     * 是否自动加载人脸到内存
     */
    private boolean isAutoLoadFace = true;


    /**
     * 是否裁剪人脸
     */
    private boolean cropFace = true;

    /**
     * 是否对齐人脸
     */
    private boolean align = false;

    /**
     * 人脸检测模型
     */
    private FaceDetModel detectModel;

    /**
     * 个性化配置（按模型类型动态解析）
     */
    private Map<String, Object> customParams = new HashMap<>();

    public FaceRecConfig() {
    }

    public FaceRecConfig(FaceRecModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }

    public FaceRecConfig(FaceRecModelEnum modelEnum, String modelPath) {
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

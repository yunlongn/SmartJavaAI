package cn.smartjavaai.face;

import cn.smartjavaai.common.enums.DeviceEnum;
import lombok.Data;

/**
 * 模型配置
 * @author dwj
 */
@Data
public class FaceModelConfig {

    /**
     * 人脸算法名称
     */
    private FaceModelEnum modelEnum;

    /**
     * 置信度阈值
     */
    private double confidenceThreshold = FaceConfig.DEFAULT_CONFIDENCE_THRESHOLD;

    /**
     * 相似度阈值 作用：判断是否为同一人脸
     */
    private double similarityThreshold = 0D;

    /**
     * 非极大抑制阈值 作用：消除重叠检测框，保留最优结果
     */
    private double nmsThresh = FaceConfig.NMS_THRESHOLD;

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
     * gpu设备ID 当device为GPU时生效
     */
    private int gpuId = 0;

    public FaceModelConfig() {
    }

    public FaceModelConfig(FaceModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }

    public FaceModelConfig(FaceModelEnum modelEnum, String modelPath) {
        this.modelEnum = modelEnum;
        this.modelPath = modelPath;
    }
}

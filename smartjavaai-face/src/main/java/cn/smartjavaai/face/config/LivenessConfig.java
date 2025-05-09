package cn.smartjavaai.face.config;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.constant.LivenessConstant;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import lombok.Data;

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
     * gpu设备ID 当device为GPU时生效
     */
    private int gpuId = 0;

    /**
     * 人脸清晰度阈值
     */
    private float faceClarityThreshold = LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD;

    /**
     * 活体阈值
     */
    private float realityThreshold = LivenessConstant.DEFAULT_REALITY_THRESHOLD;

    /**
     * 视频检测帧数
     */
    private int frameCount = LivenessConstant.DEFAULT_FRAME_COUNT;

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
}

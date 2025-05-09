package cn.smartjavaai.face.config;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.enums.FaceAttributeModelEnum;
import lombok.Data;

/**
 * 人脸属性识别模型配置
 * @author dwj
 */
@Data
public class FaceAttributeConfig {

    /**
     * 人脸属性识别模型枚举
     */
    private FaceAttributeModelEnum modelEnum = FaceAttributeModelEnum.SEETA_FACE6_MODEL;

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
     * 是否启用年龄检测
     */
    private boolean enableAge = true;

    /**
     * 是否启用性别检测
     */
    private boolean enableGender = true;

    /**
     * 是否启用人脸姿态检测
     */
    private boolean enableHeadPose = true;

    /**
     * 是否启用眼睛状态检测
     */
    private boolean enableEyeStatus = true;

    /**
     * 是否启用口罩检测
     */
    private boolean enableMask = true;


    public FaceAttributeConfig() {
    }

    public FaceAttributeConfig(FaceAttributeModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }

    public FaceAttributeConfig(FaceAttributeModelEnum modelEnum, String modelPath) {
        this.modelEnum = modelEnum;
        this.modelPath = modelPath;
    }

    public FaceAttributeConfig(String modelPath) {
        this.modelPath = modelPath;
    }


}

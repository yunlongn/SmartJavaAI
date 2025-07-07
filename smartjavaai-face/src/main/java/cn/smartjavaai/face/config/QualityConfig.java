package cn.smartjavaai.face.config;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.constant.LivenessConstant;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import cn.smartjavaai.face.enums.QualityModelEnum;
import lombok.Data;

/**
 * 质量评估配置
 * @author dwj
 */
@Data
public class QualityConfig {

    /**
     * 活体检测模型枚举
     */
    private QualityModelEnum modelEnum = QualityModelEnum.SEETA_FACE6_MODEL;

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


    public QualityConfig() {
    }

    public QualityConfig(QualityModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }

    public QualityConfig(QualityModelEnum modelEnum, String modelPath) {
        this.modelEnum = modelEnum;
        this.modelPath = modelPath;
    }

    public QualityConfig(String modelPath) {
        this.modelPath = modelPath;
    }
}

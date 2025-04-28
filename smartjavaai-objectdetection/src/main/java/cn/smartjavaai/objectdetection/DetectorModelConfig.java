package cn.smartjavaai.objectdetection;

import cn.smartjavaai.common.enums.DeviceEnum;
import lombok.Data;

/**
 * 目标检测模型参数配置
 *
 * @author dwj
 * @date 2025/4/4
 */
@Data
public class DetectorModelConfig {

    /**
     * 模型名称
     */
    private DetectorModelEnum modelEnum;

    /**
     * 置信度阈值
     */
    private float threshold = DetectorConfig.DEFAULT_THRESHOLD;

    /**
     * 设备类型
     */
    private DeviceEnum device;

    public DetectorModelConfig() {
    }

    public DetectorModelConfig(DetectorModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        this.device = device;
    }

    public DetectorModelConfig(DetectorModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }
}

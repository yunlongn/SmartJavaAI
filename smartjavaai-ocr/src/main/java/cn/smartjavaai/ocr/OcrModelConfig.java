package cn.smartjavaai.ocr;

import cn.smartjavaai.common.enums.DeviceEnum;
import lombok.Data;

/**
 * @author dwj
 * @date 2025/4/22
 */
@Data
public class OcrModelConfig {

    /**
     * 模型名称
     */
    private OcrModelEnum modelEnum;

    /**
     * 设备类型
     */
    private DeviceEnum device;

    /**
     * 模型路径
     */
    private String modelPath;

}

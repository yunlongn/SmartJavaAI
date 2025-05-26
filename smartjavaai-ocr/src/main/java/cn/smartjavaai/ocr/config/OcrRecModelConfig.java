package cn.smartjavaai.ocr.config;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import lombok.Data;

/**
 * OCR识别模型配置
 * @author dwj
 * @date 2025/4/22
 */
@Data
public class OcrRecModelConfig {

    /**
     * 检测模型
     */
    private CommonDetModelEnum detModelEnum;

    /**
     * 识别模型
     */
    private CommonRecModelEnum recModelEnum;

    /**
     * 设备类型
     */
    private DeviceEnum device;

    /**
     * 检测模型路径
     */
    private String detModelPath;

    /**
     * 识别模型路径
     */
    private String recModelPath;

    /**
     * 方向检测模型
     */
    private DirectionModelEnum directionModelEnum;

    /**
     * 方向检测模型路径
     */
    private String directionModelPath;

}

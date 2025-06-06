package cn.smartjavaai.translation.config;

import cn.smartjavaai.common.enums.DeviceEnum;

import cn.smartjavaai.translation.enums.MachineTranslationModeEnum;
import lombok.Data;

/**
 * 机器翻译模型配置
 * @author lwx
 * @date 2025/6/05
 */
@Data
public class MachineTranslationModelConfig {
    /**
     * 翻译模型
     */
    private MachineTranslationModeEnum modelEnum;

    /**
     * 设备类型
     */
    private DeviceEnum device;

    /**
     * 翻译模型路径
     */
    private String modelPath;
    /**
     * 翻译模型路径
     */
    private String modelName;
    /**
     * 翻译模型配置
     */
    private SearchConfig  searchConfig;



}

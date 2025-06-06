package cn.smartjavaai.translation.model.common;

import cn.smartjavaai.translation.config.MachineTranslationModelConfig;

/**
 * 机器翻译通用检测模型
 * @author lwx
 * @date 2025/6/05
 */
public interface TranslationCommonModel {

    /**
     * 加载模型
     * @param config
     */
    void loadModel(MachineTranslationModelConfig config); // 加载模型

    /**
     * 机器翻译
     * @param input 翻译内容
     * @return
     */
    default  String translate(String input) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }






}

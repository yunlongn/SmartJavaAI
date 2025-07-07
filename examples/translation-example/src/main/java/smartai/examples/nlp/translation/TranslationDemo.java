package smartai.examples.nlp.translation;

import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.translation.config.TranslationModelConfig;
import cn.smartjavaai.translation.entity.TranslateParam;
import cn.smartjavaai.translation.enums.LanguageCode;
import cn.smartjavaai.translation.enums.TranslationModeEnum;
import cn.smartjavaai.translation.factory.TranslationModelFactory;
import cn.smartjavaai.translation.model.TranslationModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * 翻译Demo
 * 支持 Meta AI 开源的 NLLB-200 模型，实现 200 多种语言之间的高质量互译。
 * NLLB-200官网地址：https://github.com/facebookresearch/fairseq/tree/nllb
 * 模型下载地址：https://pan.baidu.com/s/1wf7btnb4cyBFv7DB7baHnw?pwd=1234 提取码: 1234
 * @author dwj
 */
@Slf4j
public class TranslationDemo {


    /**
     * 翻译
     */
    @Test
    public void translate() {
        try {
            TranslationModelConfig config = new TranslationModelConfig();
            //指定翻译模型：NLLB
            config.setModelEnum(TranslationModeEnum.NLLB_MODEL);
            //指定模型路径，需将模型路径修改为本地的模型路径
            config.setModelPath("/Users/xxx/Documents/develop/model/trans/traced_translation_cpu.pt");
            TranslationModel translationModel = TranslationModelFactory.getInstance().getModel(config);
            //翻译参数
            TranslateParam translateParam = new TranslateParam();
            //输入文字
            translateParam.setInput("你好，欢迎使用SmartJavaAI！");
            //源语言：中文
            translateParam.setSourceLanguage(LanguageCode.ZHO_HANS);
            //目标语言：英文
            translateParam.setTargetLanguage(LanguageCode.ENG_LATN);
            R<String> result = translationModel.translate(translateParam);
            if(result.isSuccess()){
                log.info("翻译结果：{}", result.getData());
            }else{
                log.error("翻译失败：{}", result.getMessage());
            }
            //目标语言：韩语
            translateParam.setTargetLanguage(LanguageCode.KOR_HANG);
            R<String> result2 = translationModel.translate(translateParam);
            if(result2.isSuccess()){
                log.info("翻译结果：{}", result2.getData());
            }else{
                log.error("翻译失败：{}", result2.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GPU 翻译
     */
    @Test
    public void translateGpu() {
        try {
            TranslationModelConfig config = new TranslationModelConfig();
            //指定翻译模型：NLLB
            config.setModelEnum(TranslationModeEnum.NLLB_MODEL);
            //指定设备：GPU
            config.setDevice(DeviceEnum.GPU);
            //指定模型路径，需将模型路径修改为本地的 GPU 模型路径
            config.setModelPath("/Users/xxx/Documents/develop/model/trans/traced_translation_gpu.pt");
            //获取翻译模型
            TranslationModel translationModel = TranslationModelFactory.getInstance().getModel(config);
            //翻译参数
            TranslateParam translateParam = new TranslateParam();
            //输入文字
            translateParam.setInput("你好，欢迎使用SmartJavaAI！");
            //源语言：中文
            translateParam.setSourceLanguage(LanguageCode.ZHO_HANS);
            //目标语言：韩语
            translateParam.setTargetLanguage(LanguageCode.ENG_LATN);
            R<String> result = translationModel.translate(translateParam);
            if(result.isSuccess()){
                log.info("翻译结果：{}", result.getData());
            }else{
                log.error("翻译失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

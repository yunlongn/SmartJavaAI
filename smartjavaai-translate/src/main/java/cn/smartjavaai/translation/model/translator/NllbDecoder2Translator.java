package cn.smartjavaai.translation.model.translator;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.NoBatchifyTranslator;
import ai.djl.translate.TranslatorContext;
import cn.smartjavaai.translation.entity.CausalLMOutput;


/**
 * 解碼器，參數支持 pastKeyValues
 *
 * @author Calvin
 * @mail 179209347@qq.com
 * @website www.aias.top
 */
public class NllbDecoder2Translator implements NoBatchifyTranslator<NDList, CausalLMOutput> {
    private String tupleName;

    public NllbDecoder2Translator() {
        tupleName = "past_key_values(" + 12 + ',' + 4 + ')';
    }

    @Override
    public NDList processInput(TranslatorContext ctx, NDList input) {

        NDArray placeholder = ctx.getNDManager().create(0);
        placeholder.setName("module_method:decoder2");

        input.add(placeholder);

        return input;
    }

    @Override
    public CausalLMOutput processOutput(TranslatorContext ctx, NDList output) {
        NDArray logitsOutput = output.get(0);
        NDList pastKeyValuesOutput = output.subNDList(1, 12 * 4 + 1);

        for (NDArray array : pastKeyValuesOutput) {
            array.setName(tupleName);
        }

        return new CausalLMOutput(logitsOutput, pastKeyValuesOutput);
    }
}

package cn.smartjavaai.translation.utils;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import cn.smartjavaai.translation.config.NllbSearchConfig;


import java.util.ArrayList;

/**
 *
 *
 * @author lwx
 * @date 2025/4/22
 */
public final class TokenUtils {

    private TokenUtils() {
    }

    /**
     * 语言解码
     *
     * @param tokenizer
     * @param output
     * @return
     */
    public static String decode(NllbSearchConfig config, HuggingFaceTokenizer tokenizer, NDArray output) {
        long[] outputIds = output.toLongArray();
        ArrayList<Long> outputIdsList = new ArrayList<>();

        for (long id : outputIds) {
            if (id == config.getEosTokenId() || id==config.getSrcLangId() || id==config.getForcedBosTokenId()) {
                continue;
            }
            outputIdsList.add(id);
        }

        Long[] objArr =  outputIdsList.toArray(new Long[0]);
        long[] ids = new long[objArr.length];
        for (int i = 0; i < objArr.length; i++) {
            ids[i] = objArr[i];
        }
        String text = tokenizer.decode(ids);
        return text;
    }
}

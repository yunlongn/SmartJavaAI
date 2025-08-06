package cn.smartjavaai.speech.asr.entity;

import cn.smartjavaai.common.entity.Language;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperSamplingStrategy;
import lombok.Data;

/**
 * @author dwj
 */
@Data
public class VoskParams extends RecParams{

    /**
     * 最大候选结果数
     */
    private int maxAlternatives;

    /**
     * 限定词汇表 例：["yes", "no", "hello"]
     */
    private String grammar;

    /**
     * 是否返回词级别的识别结果（包含每个词的开始/结束时间和置信度）。
     */
    private boolean words = true;


}

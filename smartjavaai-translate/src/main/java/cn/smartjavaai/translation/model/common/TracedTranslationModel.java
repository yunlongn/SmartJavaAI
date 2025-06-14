package cn.smartjavaai.translation.model.common;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.pool.ZooModelFactory;
import cn.smartjavaai.translation.config.MachineTranslationModelConfig;
import cn.smartjavaai.translation.config.SearchConfig;
import cn.smartjavaai.translation.entity.CausalLMOutput;
import cn.smartjavaai.translation.entity.GreedyBatchTensorList;
import cn.smartjavaai.translation.enums.MachineTranslationModeEnum;
import cn.smartjavaai.translation.exception.TranslationException;
import cn.smartjavaai.translation.factory.TranslationModelFactory;
import cn.smartjavaai.translation.model.common.translator.Decoder2Translator;
import cn.smartjavaai.translation.model.common.translator.DecoderTranslator;
import cn.smartjavaai.translation.model.common.translator.EncoderTranslator;
import cn.smartjavaai.translation.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 机器翻译通用检测模型
 *
 * @author lwx
 * @date 2025/6/05
 */
@Slf4j
public class TracedTranslationModel implements TranslationCommonModel {

    private ObjectPool<ZooModel<NDList, NDList>> detPredictorPool;
    private ZooModel<NDList, NDList> nllbModel;
    private HuggingFaceTokenizer tokenizer;
    private Predictor<long[], NDArray> encoderPredictor;
    private Predictor<NDList, CausalLMOutput> decoderPredictor;
    private Predictor<NDList, CausalLMOutput> decoder2Predictor;
    private SearchConfig searchConfig;
    private MachineTranslationModelConfig config;
    private NDManager manager;

    @Test
    public void detect(){
        Config.setCachePath("E:\\ai\\models\\libs");
        MachineTranslationModelConfig config = new MachineTranslationModelConfig();
        SearchConfig searchConfig = new SearchConfig();
        // 设置输出文字的最大长度
        searchConfig.setMaxSeqLength(128);
        // 设置源语言：中文 "zho_Hans": 256200
        searchConfig.setSrcLangId(256200);
        // 设置目标语言：英文 "eng_Latn": 256047
        searchConfig.setForcedBosTokenId(256047);
        config.setSearchConfig(searchConfig);
        config.setDevice(DeviceEnum.CPU);
        config.setModelEnum(MachineTranslationModeEnum.TRACED_TRANSLATION_CPU);
        config.setModelPath("E:\\ai\\models\\nlp\\");
        config.setModelName("traced_translation_cpu.pt");
        // 输入文字
        String input2 = "智利北部的丘基卡马塔矿是世界上最大的露天矿之一，长约4公里，宽3公里，深1公里。";
        String input = "你好，欢迎使用SmartJavaAI！";
        TranslationCommonModel detModel = TranslationModelFactory.getInstance().getDetModel(config);
       // detModel.loadModel(config);
        String translate = detModel.translate(input);
        System.out.println("识别结果 translate"+translate);
    }
    @Override
    public void loadModel(MachineTranslationModelConfig config) {
        if (StringUtils.isBlank(config.getModelPath())) {
            throw new TranslationException("modelPath is null");
        }
        Device device = null;
        if (!Objects.isNull(config.getDevice())) {
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        this.config = config;
        this.searchConfig = config.getSearchConfig();
        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelPath(Paths.get(config.getModelPath()+config.getModelName()))
                        .optEngine("PyTorch")
                        .optDevice(device)
                        .optTranslator(new NoopTranslator())
                        .build();
        try {
             nllbModel = ModelZoo.loadModel(criteria);
            // 创建池子：每个线程独享 Predictor
            this.detPredictorPool = new GenericObjectPool<>(new ZooModelFactory<>(nllbModel));
            log.info("当前设备: " + nllbModel.getNDManager().getDevice());
            log.info("当前引擎: " + Engine.getInstance().getEngineName());
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new TranslationException("模型加载失败", e);
        }
    }

    @Override
    public String translate(String input) throws TranslationException {
        ZooModel<NDList, NDList> zooModel = null;
        try (NDManager manager = NDManager.newBaseManager()) {
            zooModel = detPredictorPool.borrowObject();
            tokenizer = HuggingFaceTokenizer.newInstance(Paths.get(config.getModelPath() + "tokenizer.json"));
            encoderPredictor = zooModel.newPredictor(new EncoderTranslator());
            decoderPredictor = zooModel.newPredictor(new DecoderTranslator());
            decoder2Predictor = zooModel.newPredictor(new Decoder2Translator());
            this.manager=manager;
            return translateLanguage(input);
        } catch (Exception e) {
            throw new TranslationException("翻译错误", e);
        } finally {
            if (zooModel != null) {
                try {
                    detPredictorPool.returnObject(zooModel); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        zooModel.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }

        }
    }

    private String translateLanguage(String input) throws TranslateException {

        Encoding encoding = tokenizer.encode(input);
        long[] ids = encoding.getIds();
        // 1. Encoder
        long[] inputIds = new long[ids.length];
        // 设置源语言编码
        inputIds[0] = searchConfig.getSrcLangId();
        for (int i = 0; i < ids.length - 1; i++) {
            inputIds[i + 1] = ids[i];
        }

        long[] attentionMask = encoding.getAttentionMask();
        NDArray attentionMaskArray = manager.create(attentionMask).expandDims(0);

        NDArray encoderHiddenStates = encoder(inputIds);

        NDArray decoder_input_ids = manager.create(new long[]{searchConfig.getDecoderStartTokenId()}).reshape(1, 1);
        NDList decoderInput = new NDList(decoder_input_ids, encoderHiddenStates, attentionMaskArray);

        // 2. Initial Decoder
        CausalLMOutput modelOutput = decoder(decoderInput);
        modelOutput.getLogits().attach(manager);
        modelOutput.getPastKeyValuesList().attach(manager);

        GreedyBatchTensorList searchState =
                new GreedyBatchTensorList(null, decoder_input_ids, modelOutput.getPastKeyValuesList(), encoderHiddenStates, attentionMaskArray);

        while (true) {
//            try (NDScope ignore = new NDScope()) {
            NDArray pastOutputIds = searchState.getPastOutputIds();

            if (searchState.getNextInputIds() != null) {
                decoderInput = new NDList(searchState.getNextInputIds(), searchState.getEncoderHiddenStates(), searchState.getAttentionMask());
                decoderInput.addAll(searchState.getPastKeyValues());
                // 3. Decoder loop
                modelOutput = decoder2(decoderInput);
            }

            NDArray outputIds = greedyStepGen(searchConfig, pastOutputIds, modelOutput.getLogits());

            searchState.setNextInputIds(outputIds);
            pastOutputIds = pastOutputIds.concat(outputIds, 1);
            searchState.setPastOutputIds(pastOutputIds);

            searchState.setPastKeyValues(modelOutput.getPastKeyValuesList());

            long id = searchState.getNextInputIds().toLongArray()[0];
            if (searchConfig.getEosTokenId() == id) {
                searchState.setNextInputIds(null);
                break;
            }
            if (searchState.getPastOutputIds() != null && searchState.getPastOutputIds().getShape().get(1) + 1 >= searchConfig.getMaxSeqLength()) {
                break;
            }
        }

        if (searchState.getNextInputIds() == null) {
            NDArray resultIds = searchState.getPastOutputIds();
            String result = TokenUtils.decode(searchConfig, tokenizer, resultIds);
            return result;
        } else {
            NDArray resultIds = searchState.getPastOutputIds(); // .concat(searchState.getNextInputIds(), 1)
            String result = TokenUtils.decode(searchConfig, tokenizer, resultIds);
            return result;
        }

    }

    public NDArray greedyStepGen(SearchConfig config, NDArray pastOutputIds, NDArray next_token_scores) {
        next_token_scores = next_token_scores.get(":, -1, :");

        NDArray new_next_token_scores = manager.create(next_token_scores.getShape(), next_token_scores.getDataType());
        next_token_scores.copyTo(new_next_token_scores);

        // LogitsProcessor 1. ForcedBOSTokenLogitsProcessor
        // 设置目标语言
        long cur_len = pastOutputIds.getShape().getLastDimension();
        if (cur_len == 1) {
            long num_tokens = new_next_token_scores.getShape().getLastDimension();
            for (long i = 0; i < num_tokens; i++) {
                if (i != config.getForcedBosTokenId()) {
                    new_next_token_scores.set(new NDIndex(":," + i), Float.NEGATIVE_INFINITY);
                }
            }
            new_next_token_scores.set(new NDIndex(":," + config.getForcedBosTokenId()), 0);
        }

        NDArray probs = new_next_token_scores.softmax(-1);
        NDArray next_tokens = probs.argMax(-1);

        return next_tokens.expandDims(0);
    }

    public NDArray encoder(long[] ids) throws TranslateException {
        return encoderPredictor.predict(ids);
    }

    public CausalLMOutput decoder(NDList input) throws TranslateException {
        return decoderPredictor.predict(input);
    }

    public CausalLMOutput decoder2(NDList input) throws TranslateException {
        return decoder2Predictor.predict(input);
    }
}

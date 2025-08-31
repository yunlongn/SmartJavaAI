package smartai.examples.face;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.audio.Audio;
import ai.djl.modality.audio.AudioFactory;
import ai.djl.modality.audio.translator.SpeechRecognitionTranslatorFactory;
import ai.djl.repository.Artifact;
import ai.djl.repository.MRL;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author dwj
 * @date 2025/7/29
 */
@Slf4j
public class Test {

    public static void main(String[] args) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {
//        PythonTranslator translator = new PythonTranslator();
//        Criteria<byte[], Classifications> criteria =
//                Criteria.builder()
//                        .setTypes(byte[].class, Classifications.class)
//                        .optModelPath(Paths.get("/Users/wenjie/Documents/develop/model/arcfaceresnet100-11-int8.onnx"))
//                        .optEngine("OnnxRuntime")
//                        .optTranslator(translator)
//                        .build();
//        String path = "/Users/wenjie/Downloads/facetest/jsy.jpg";
//        try (ZooModel<byte[], Classifications> model = criteria.loadModel();
//             Predictor<byte[], Classifications> predictor = model.newPredictor()) {
//            byte[] data = Files.readAllBytes(Paths.get(path));
//            Classifications ret = predictor.predict(data);
//            System.out.println(ret);
//        }
//
//        // unload python model
//        translator.close();


        // Load model.
        // Wav2Vec2 model is a speech model that accepts a float array corresponding to the raw
        // waveform of the speech signal.

//        String url = "/Users/wenjie/Downloads/20210601_u2++_conformer_exp/final.pt";
//        Criteria<Audio, String> criteria =
//                Criteria.builder()
//                        .setTypes(Audio.class, String.class)
////                        .optModelUrls(url)
//                        .optModelPath(Paths.get(url))
//                        .optDevice(Device.cpu()) // torchscript model only support CPU
//                        .optTranslatorFactory(new SpeechRecognitionTranslatorFactory())
////                        .optModelName("data.pkl")
//                        .optEngine("PyTorch")
//                        .build();
//
//        // Read in audio file
//        String wave = "https://resources.djl.ai/audios/speech.wav";
//        Audio audio = AudioFactory.newInstance().fromUrl(wave);
//        try (ZooModel<Audio, String> model = criteria.loadModel();
//             Predictor<Audio, String> predictor = model.newPredictor()) {
//             String result = predictor.predict(audio);
//             log.info("Result: {}", result);
//        }

        boolean withArtifacts =
                args.length > 0 && ("--artifact".equals(args[0]) || "-a".equals(args[0]));
        if (!withArtifacts) {
            log.info("============================================================");
            log.info("user ./gradlew listModel --args='-a' to show artifact detail");
            log.info("============================================================");
        }
//        Map<Application, List<Artifact>> models = ModelZoo.listModels();
//        for (Map.Entry<Application, List<Artifact>> entry : models.entrySet()) {
//            String appName = entry.getKey().toString();
//            for (Artifact artifact : entry.getValue()) {
//                if (withArtifacts) {
//                    log.info("{} djl://{}", appName, artifact);
//                } else {
//                    log.info("{} {}", appName, artifact);
//                }
//            }
//        }
    }
}

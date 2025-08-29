import ai.djl.Application;
import ai.djl.repository.Artifact;
import ai.djl.repository.MRL;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.enums.SimilarityType;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.FaceRecModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import cn.smartjavaai.face.utils.SimilarityUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author dwj
 * @date 2025/7/25
 */
@Slf4j
public class Test {

    /**
     * 获取人脸检测模型
     * @return
     */
    public static FaceDetModel getFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        config.setModelEnum(FaceDetModelEnum.RETINA_FACE);//人脸检测模型
        config.setConfidenceThreshold(FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD);//只返回相似度大于该值的人脸
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);//用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        return FaceDetModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸识别模型
     * @return
     */
    public static FaceRecModel getFaceRecModel(){
        FaceRecConfig config = new FaceRecConfig();
        config.setModelEnum(FaceRecModelEnum.ELASTIC_FACE_MODEL);
        config.setModelPath("/Users/wenjie/Documents/develop/model/arcfaceresnet100-11-int8.onnx");
//        config.setModelPath("/Users/xxx/Documents/develop/model/InsightFace/model_mobilefacenet.pt");
        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
        config.setCropFace(true);
        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
        config.setAlign(true);
        //指定人脸检测模型
        config.setDetectModel(getFaceDetModel());
        return FaceRecModelFactory.getInstance().getModel(config);
    }

    public static void main(String[] args) throws ModelNotFoundException, IOException {
//        boolean withArtifacts =
//                args.length > 0 && ("--artifact".equals(args[0]) || "-a".equals(args[0]));
//        if (!withArtifacts) {
//            logger.info("============================================================");
//            logger.info("user ./gradlew listModel --args='-a' to show artifact detail");
//            logger.info("============================================================");
//        }
//        Map<Application, List<MRL>> models = ModelZoo.listModels();
//        for (Map.Entry<Application, List<MRL>> entry : models.entrySet()) {
//            String appName = entry.getKey().toString();
//            for (MRL mrl : entry.getValue()) {
//                if (withArtifacts) {
//                    for (Artifact artifact : mrl.listArtifacts()) {
//                        log.info("{} djl://{}", appName, artifact);
//                    }
//                } else {
//                    log.info("{} {}", appName, mrl);
//                }
//            }
//        }
    }


}

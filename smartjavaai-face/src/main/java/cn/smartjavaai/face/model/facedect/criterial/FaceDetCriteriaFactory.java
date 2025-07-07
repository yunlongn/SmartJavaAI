package cn.smartjavaai.face.model.facedect.criterial;

import ai.djl.Device;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.FaceExpressionConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.constant.RetinaFaceConstant;
import cn.smartjavaai.face.constant.UltraLightFastGenericFaceConstant;
import cn.smartjavaai.face.enums.ExpressionModelEnum;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.model.expression.translator.DenseNetEmotionTranslator;
import cn.smartjavaai.face.model.expression.translator.FrEmotionTranslator;
import cn.smartjavaai.face.translator.FaceDetectionTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * 人脸检测 Criteria构建工厂
 * @author dwj
 */
public class FaceDetCriteriaFactory {

    public static Criteria<Image, DetectedObjects> createCriteria(FaceDetConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        Criteria<Image, DetectedObjects> criteria = null;
        if(config.getModelEnum() == FaceDetModelEnum.RETINA_FACE){
            FaceDetectionTranslator translator =
                    new FaceDetectionTranslator(config.getConfidenceThreshold(), config.getNmsThresh(), RetinaFaceConstant.variance, FaceDetectConstant.MAX_FACE_LIMIT, RetinaFaceConstant.scales, RetinaFaceConstant.steps);
            criteria =
                    Criteria.builder()
                            .setTypes(Image.class, DetectedObjects.class)
                            .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null : RetinaFaceConstant.MODEL_URL)
                            // Load model from local file, e.g:
                            .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                            .optModelName("retinaface") // specify model file prefix
                            .optTranslator(translator)
                            .optDevice(device)
                            .optProgress(new ProgressBar())
                            .optEngine("PyTorch") // Use PyTorch engine
                            .build();
        }else if (config.getModelEnum() == FaceDetModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE){
            FaceDetectionTranslator translator =
                    new FaceDetectionTranslator(config.getConfidenceThreshold(), config.getNmsThresh(), UltraLightFastGenericFaceConstant.variance, FaceDetectConstant.MAX_FACE_LIMIT, UltraLightFastGenericFaceConstant.scales, UltraLightFastGenericFaceConstant.steps);
            criteria =
                    Criteria.builder()
                            .setTypes(Image.class, DetectedObjects.class)
                            .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null : UltraLightFastGenericFaceConstant.MODEL_URL)
                            .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                            .optTranslator(translator)
                            .optProgress(new ProgressBar())
                            .optDevice(device)
                            .optEngine("PyTorch") // Use PyTorch engine
                            .build();
        }
        return criteria;
    }

}

package cn.smartjavaai.face.model.facerec.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.constant.FaceNetConstant;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.model.facerec.translator.FaceFeatureTranslator;
import cn.smartjavaai.face.model.facerec.translator.FaceNetRecTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 人脸识别 Criteria构建工厂
 * @author dwj
 */
public class FaceRecCriteriaFactory {

    public static Criteria<Image, float[]> createCriteria(FaceRecConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        Criteria<Image, float[]> criteria = null;
        if(config.getModelEnum() == FaceRecModelEnum.FACENET_MODEL){
            criteria =
                    Criteria.builder()
                            .setTypes(Image.class, float[].class)
                            .optModelName("face_feature") // specify model file prefix
                            .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null :
                                    FaceNetConstant.MODEL_URL)
                            .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                            .optTranslator(new FaceNetRecTranslator())
                            .optDevice(device)
                            .optEngine("PyTorch") // Use PyTorch engine
                            .optProgress(new ProgressBar())
                            .build();
        }else if (config.getModelEnum() == FaceRecModelEnum.INSIGHT_FACE_MOBILE_FACENET_MODEL){
            if(StringUtils.isBlank(config.getModelPath())){
                throw new RuntimeException("请指定模型路径");
            }
            List<Float> mean = Arrays.asList(0.5f,0.5f,0.5f,0.5f,0.5f,0.5f);
            String normalize = mean.stream().map(Object::toString).collect(Collectors.joining(","));
            criteria = Criteria.builder()
                    .setTypes(Image.class, float[].class)
                    .optModelPath(Paths.get(config.getModelPath()))
//                    .optTranslatorFactory(new ImageFeatureExtractorFactory())
//                    .optArgument("normalize", normalize)
//                    .optArgument("resize", "112,112")
                    .optTranslator(new FaceFeatureTranslator())
                    .optEngine("PyTorch") // Use PyTorch engine
                    .optProgress(new ProgressBar())
                    .build();
        }else if (config.getModelEnum() == FaceRecModelEnum.INSIGHT_FACE_IRSE50_MODEL){
            if(StringUtils.isBlank(config.getModelPath())){
                throw new RuntimeException("请指定模型路径");
            }
            List<Float> mean = Arrays.asList(0.5f,0.5f,0.5f,0.5f,0.5f,0.5f);
            String normalize = mean.stream().map(Object::toString).collect(Collectors.joining(","));
            criteria = Criteria.builder()
                    .setTypes(Image.class, float[].class)
                    .optModelPath(Paths.get(config.getModelPath()))
//                    .optTranslatorFactory(new ImageFeatureExtractorFactory())
//                    .optArgument("normalize", normalize)
//                    .optArgument("resize", "112,112")
                    .optTranslator(new FaceFeatureTranslator())
                    .optEngine("PyTorch") // Use PyTorch engine
                    .optProgress(new ProgressBar())
                    .build();
        }else if (config.getModelEnum() == FaceRecModelEnum.ELASTIC_FACE_MODEL){
            if(StringUtils.isBlank(config.getModelPath())){
                throw new RuntimeException("请指定模型路径");
            }
            List<Float> mean = Arrays.asList(0.5f,0.5f,0.5f,0.5f,0.5f,0.5f);
            String normalize = mean.stream().map(Object::toString).collect(Collectors.joining(","));
            criteria = Criteria.builder()
                    .setTypes(Image.class, float[].class)
                    .optModelPath(Paths.get(config.getModelPath()))
//                    .optTranslatorFactory(new ImageFeatureExtractorFactory())
//                    .optArgument("normalize", normalize)
//                    .optArgument("resize", "112,112")
                    .optTranslator(new FaceFeatureTranslator())
                    .optEngine("PyTorch") // Use PyTorch engine
                    .optProgress(new ProgressBar())
                    .build();
        }
        return criteria;
    }

}

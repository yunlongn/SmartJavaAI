package cn.smartjavaai.objectdetection.criteria;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.YoloV8TranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.objectdetection.constant.DetectorConstant;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;

import java.nio.file.Paths;

/**
 * YOLO模型Criteria 构建器
 * @author dwj
 * @date 2025/5/14
 */
public class YoloCriteriaBuilder implements CriteriaBuilderStrategy {
    @Override
    public Criteria<Image, DetectedObjects> buildCriteria(DetectorModelConfig config) {
        Criteria.Builder criteriaBuilder = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                //.optModelUrls("/Users/wenjie/Documents/develop/face_model/yolo")
                .optModelPath(Paths.get(config.getModelPath()))
                .optEngine("OnnxRuntime")
                .optArgument("width", 640) //将输入图像的宽度缩放为 640 像素
                .optArgument("height", 640)
                .optArgument("resize", true)
                .optArgument("toTensor", true)
                .optArgument("applyRatio", true)
                .optTranslatorFactory(new YoloV8TranslatorFactory())
                .optProgress(new ProgressBar())
                .optArgument("threshold", config.getThreshold() > 0 ? config.getThreshold() : DetectorConstant.DEFAULT_THRESHOLD);
        if(config.getMaxBox() >  0){
            criteriaBuilder.optArgument("maxBox", config.getMaxBox());
        }
        Criteria<Image, DetectedObjects> criteria = criteriaBuilder.build();
        return criteria;
    }
}

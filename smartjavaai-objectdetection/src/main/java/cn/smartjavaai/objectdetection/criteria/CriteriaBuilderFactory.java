package cn.smartjavaai.objectdetection.criteria;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;
import cn.smartjavaai.objectdetection.enums.DetectorModelEnum;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import org.apache.commons.lang3.StringUtils;

/**
 * Criteria构建工厂
 * @author dwj
 * @date 2025/5/14
 */
public class CriteriaBuilderFactory {

    public static Criteria<Image, DetectedObjects> createCriteria(DetectorModelConfig config) {
        //以下模型modelPath不允许为空
        if(config.getModelEnum() == DetectorModelEnum.YOLOV8_OFFICIAL ||
                config.getModelEnum() == DetectorModelEnum.YOLOV12_OFFICIAL ||
                config.getModelEnum() == DetectorModelEnum.YOLOV8_CUSTOM ||
                config.getModelEnum() == DetectorModelEnum.YOLOV12_CUSTOM){
            if(StringUtils.isBlank(config.getModelPath())){
                throw new DetectionException("modelPath is null");
            }
        }
        switch (config.getModelEnum()) {
            case YOLOV8_OFFICIAL:
                return new YoloCriteriaBuilder().buildCriteria(config);
            case YOLOV12_OFFICIAL:
                return new YoloCriteriaBuilder().buildCriteria(config);
            case YOLOV8_CUSTOM:
                return new YoloCriteriaBuilder().buildCriteria(config);
            case YOLOV12_CUSTOM:
                return new YoloCriteriaBuilder().buildCriteria(config);
            // 其他类型
            default:
                return new DJLModelCriteriaBuilder().buildCriteria(config);
        }
    }

}

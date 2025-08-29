package cn.smartjavaai.action.criteria;

import ai.djl.Device;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.action.config.ActionRecModelConfig;
import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.action.model.CommonActionTranslator;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.action.exception.ActionException;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动作识别模型工厂
 * @author dwj
 */
public class ActionRecCriteriaFactory {


    public static Criteria<Image, Classifications> createCriteria(ActionRecModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, Classifications> criteria = null;
        ConcurrentHashMap params = new ConcurrentHashMap<String, String>();
        params.putAll(config.getCustomParams());
        if(config.getModelEnum() == ActionRecModelEnum.VIT_BASE_PATCH16_224){
            criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null :
                                    config.getModelEnum().getModelUri())
                            .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                            .optEngine("PyTorch")
                            .optDevice(device)
                            .optProgress(new ProgressBar())
                            .build();
        }else {
            if (StringUtils.isBlank(config.getModelPath())){
                throw new ActionException("请指定模型路径");
            }
            int width = 224;
            int height = 224;
            if(config.getModelEnum() == ActionRecModelEnum.INCEPTIONV3_KINETICS400){
                width = 299;
                height = 299;
            }
            criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optTranslator(new CommonActionTranslator(width, height))
                            .optEngine("OnnxRuntime")
                            .optModelPath(Paths.get(config.getModelPath()))
                            .optDevice(device)
                            .optProgress(new ProgressBar())
                            .build();
        }
        return criteria;
    }
}

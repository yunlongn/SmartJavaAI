package cn.smartjavaai.instanceseg.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.instanceseg.config.InstanceSegModelConfig;
import cn.smartjavaai.instanceseg.translator.YoloSegmentationTranslatorFactory2;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实例分割Criteria工厂
 * @author dwj
 */
public class InstanceSegCriteriaFactory {


    public static Criteria<Image, DetectedObjects> createCriteria(InstanceSegModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, DetectedObjects> criteria = null;
        ConcurrentHashMap params = new ConcurrentHashMap<String, String>();
        params.putAll(config.getCustomParams());
//        YoloV5Translator.Builder builder = new YoloV5Translator.Builder()
//                .optSynsetArtifactName("synset.txt").setPipeline()
        criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null :
                                config.getModelEnum().getModelUri())
                        .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                        .optDevice(device)
                        .optEngine("PyTorch")
                        .optTranslatorFactory(new YoloSegmentationTranslatorFactory2())
                        .optProgress(new ProgressBar())
                        .build();
        return criteria;
    }
}

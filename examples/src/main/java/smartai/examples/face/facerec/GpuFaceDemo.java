package smartai.examples.face.facerec;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.factory.FaceModelFactory;
import cn.smartjavaai.face.model.facerec.FaceModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * GPU 人脸检测
 * @author dwj
 * @date 2025/4/14
 */
@Slf4j
public class GpuFaceDemo {

    /**
     * 人脸检测(GPU)
     * 图片参数：图片路径
     */
    @Test
    public void testFaceGpu(){
        FaceModelConfig config = new FaceModelConfig();
        config.setModelEnum(FaceModelEnum.RETINA_FACE);//人脸模型
        config.setDevice(DeviceEnum.GPU);
        FaceModel faceModel = FaceModelFactory.getInstance().getModel(config);
        DetectionResponse detectedResult = faceModel.detect("src/main/resources/largest_selfie.jpg");
        log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult));
    }

}

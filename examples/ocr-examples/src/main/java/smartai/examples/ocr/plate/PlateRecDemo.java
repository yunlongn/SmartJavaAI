package smartai.examples.ocr.plate;

import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.PlateDetModelConfig;
import cn.smartjavaai.ocr.config.PlateRecModelConfig;
import cn.smartjavaai.ocr.entity.PlateInfo;
import cn.smartjavaai.ocr.enums.PlateDetModelEnum;
import cn.smartjavaai.ocr.enums.PlateRecModelEnum;
import cn.smartjavaai.ocr.factory.PlateModelFactory;
import cn.smartjavaai.ocr.model.plate.PlateDetModel;
import cn.smartjavaai.ocr.model.plate.PlateRecModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author dwj
 */
@Slf4j
public class PlateRecDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    /**
     * 获取车牌检测模型
     * @return
     */
    public PlateDetModel getPlateDetModel() {
        PlateDetModelConfig config = new PlateDetModelConfig();
        config.setModelEnum(PlateDetModelEnum.YOLOV5);
        config.setModelPath("/Users/xxx/Documents/develop/model/plate/yolov5_plate_detect.onnx");
        config.setDevice(device);
        return PlateModelFactory.getInstance().getDetModel(config);
    }

    /**
     * 获取车牌识别模型
     * @return
     */
    public PlateRecModel getPlateRecModel() {
        PlateRecModelConfig recModelConfig = new PlateRecModelConfig();
        recModelConfig.setModelEnum(PlateRecModelEnum.PLATE_REC_CRNN);
        recModelConfig.setModelPath("/Users/xxx/Documents/develop/model/plate/plate_rec_color.onnx");
        recModelConfig.setPlateDetModel(getPlateDetModel());
        return PlateModelFactory.getInstance().getRecModel(recModelConfig);
    }

    @Test
    public void testDetect() {
        PlateRecModel plateRecModel = getPlateRecModel();
        R<List<PlateInfo>> result = plateRecModel.recognize("src/main/resources/plate/Quicker_20220930_180856.png");
        if(result.isSuccess()){
            log.info("车牌识别结果：{}", JsonUtils.toJson(result.getData()));
        }else{
            log.error("车牌识别失败：{}", result.getMessage());
        }
    }

    @Test
    public void recognizeAndDraw() {
        PlateRecModel plateRecModel = getPlateRecModel();
        R<Void> result = plateRecModel.recognizeAndDraw("src/main/resources/plate/single_green.jpg", "output/plate_recognized2.jpg");
        if(result.isSuccess()){
            log.info("车牌识别成功");
        }else{
            log.error("车牌识别失败：{}", result.getMessage());
        }

    }


}

package smartai.examples.ocr;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

/**
 * OCR 文本方向检测 示例
 * 模型下载地址：https://pan.baidu.com/s/1MLfd73Vjdpnuls9-oqc9uw?pwd=1234 提取码: 1234
 * @author dwj
 * @date 2025/5/25
 */
@Slf4j
public class OcrDirectionDetDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    /**
     * 获取方向检测模型
     * @return
     */
    public OcrDirectionModel getDirectionModel(){
        DirectionModelConfig directionModelConfig = new DirectionModelConfig();
        //指定检测模型
        directionModelConfig.setDetModelEnum(CommonDetModelEnum.PADDLEOCR_V5_DET_MODEL);
        //指定检测模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        directionModelConfig.setDetModelPath("/Users/xxx/Documents/develop/ocr模型/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        //指定文本方向检测模型
        directionModelConfig.setModelEnum(DirectionModelEnum.CH_PPOCR_MOBILE_V2_CLS);
        //指定文本方向检测模型路径，需要更改为自己的模型路径（下载地址请查看文档）
        directionModelConfig.setModelPath("/Users/xxx/Documents/develop/ocr模型/ch_ppocr_mobile_v2.0_cls.onnx");
        directionModelConfig.setDevice(device);
        return OcrModelFactory.getInstance().getDirectionModel(directionModelConfig);
    }


    /**
     * 文本方向检测
     * 流程：文本检测 -> 方向分类
     * 检测图像中文字的整体方向
     * 支持返回四种可能的方向角度：0°, 90°, 180°, 270°
     * 模型需要放在单独文件夹
     */
    @Test
    public void detect(){
        try (OcrDirectionModel directionModel = getDirectionModel()){
            List<OcrItem> itemList = directionModel.detect("src/main/resources/ocr_3.jpg");
            log.info("OCR方向检测结果：{}", JSONObject.toJSONString(itemList));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 文本检测并绘制结果
     * 流程：文本检测 -> 方向分类
     * 检测图像中的文本区域，仅检测文本框位置，不识别文字内容
     * 模型需要放在单独文件夹
     */
    @Test
    public void detectAndDraw(){
        try (OcrDirectionModel directionModel = getDirectionModel()){
            directionModel.detectAndDraw("src/main/resources/ocr_3.jpg",  "output/ocr_3_detected.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

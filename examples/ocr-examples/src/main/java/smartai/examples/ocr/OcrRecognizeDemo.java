package smartai.examples.ocr;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * OCR 文本识别 示例
 * 模型下载地址：https://pan.baidu.com/s/1MLfd73Vjdpnuls9-oqc9uw?pwd=1234 提取码: 1234
 * @author dwj
 * @date 2025/5/25
 */
@Slf4j
public class OcrRecognizeDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    /**
     * 获取通用识别模型（不带方向矫正）
     * @return
     */
    public OcrCommonRecModel getRecModel(){
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定检测模型
        recModelConfig.setDetModelEnum(CommonDetModelEnum.PADDLEOCR_V5_DET_MODEL);
        //指定检测模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setDetModelPath("/Users/xxx/Documents/develop/ocr模型/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        //指定识别模型
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PADDLEOCR_V5_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/Users/xxx/Documents/develop/ocr模型/PP-OCRv5_server_rec_infer/PP-OCRv5_server_rec.onnx");
        recModelConfig.setDevice(device);
        return OcrModelFactory.getInstance().getRecModel(recModelConfig);
    }

    /**
     * 获取通用识别模型(带方向矫正)
     * @return
     */
    public OcrCommonRecModel getRecModelWithDirection() {
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定检测模型
        recModelConfig.setDetModelEnum(CommonDetModelEnum.PADDLEOCR_V5_DET_MODEL);
        //指定检测模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setDetModelPath("/Users/xxx/Documents/develop/ocr模型/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        //指定识别模型
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PADDLEOCR_V5_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/Users/xxx/Documents/develop/ocr模型/PP-OCRv5_server_rec_infer/PP-OCRv5_server_rec.onnx");
        //指定方向检测模型
        recModelConfig.setDirectionModelEnum(DirectionModelEnum.CH_PPOCR_MOBILE_V2_CLS);
        //指定方向模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setDirectionModelPath("/Users/xxx/Documents/develop/ocr模型/ch_ppocr_mobile_v2.0_cls.onnx");
        recModelConfig.setDevice(device);
        return OcrModelFactory.getInstance().getRecModel(recModelConfig);
    }


    /**
     * 文本识别
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 流程：文本检测 -> 文本识别
     * 模型需要放在单独文件夹
     */
    @Test
    public void recognize(){
        try (OcrCommonRecModel recModel = getRecModel()){
            OcrInfo ocrInfo = recModel.recognize("src/main/resources/ocr_2.jpg");
            log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 文本识别（手写字）
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 流程：文本检测 -> 文本识别
     * 模型需要放在单独文件夹
     */
    @Test
    public void recognizeHandWriting(){
        try (OcrCommonRecModel recModel = getRecModel()){
            OcrInfo ocrInfo = recModel.recognize("src/main/resources/handwriting_1.jpg");
            log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文本识别（带方向矫正）
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 本方法支持多角度文字识别
     * 流程：文本检测 -> 方向检测 -> 方向矫正 ->  文本识别
     * 模型需要放在单独文件夹
     */
    @Test
    public void recognize2(){
        try (OcrCommonRecModel recModel = getRecModelWithDirection()){
            OcrInfo ocrInfo = recModel.recognize("src/main/resources/ocr_4.jpg");
            log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 文本识别并绘制结果
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 流程：文本检测 -> 文本识别
     * 模型需要放在单独文件夹
     */
    @Test
    public void recognizeAndDraw(){
        try (OcrCommonRecModel recModel = getRecModelWithDirection()){
            int fontSize = 25;
            recModel.recognizeAndDraw("src/main/resources/ocr_4.jpg", "output/ocr_4_recognized.jpg", fontSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}

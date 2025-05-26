package smartai.examples.ocr;

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

import java.util.List;

/**
 * OCR 文本识别 示例
 * @author dwj
 * @date 2025/5/25
 */
@Slf4j
public class OcrRecognizeDemo {


    /**
     * 文本识别
     * 本方法支持旋转角度范围为 -90 到 90 度的文字
     * 同时兼容印刷体和手写体文字。
     * 流程：文本检测 -> 文本识别
     * 模型需要放在单独文件夹
     */
    @Test
    public void recognize(){
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定检测模型
        recModelConfig.setDetModelEnum(CommonDetModelEnum.PADDLEOCR_V5_DET_MODEL);
        //指定检测模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setDetModelPath("/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        //指定识别模型
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PADDLEOCR_V5_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/PP-OCRv5_server_rec_infer/PP-OCRv5_server_rec.onnx");
        OcrCommonRecModel recModel = OcrModelFactory.getInstance().getRecModel(recModelConfig);
        OcrInfo ocrInfo = recModel.recognize("src/main/resources/general_ocr_002.png");
        log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
    }


    /**
     * 文本识别（手写字）
     * 本方法支持旋转角度范围为 -90 到 90 度的文字
     * 同时兼容印刷体和手写体文字。
     * 流程：文本检测 -> 文本识别
     * 模型需要放在单独文件夹
     */
    @Test
    public void recognizeHandWriting(){
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定检测模型
        recModelConfig.setDetModelEnum(CommonDetModelEnum.PADDLEOCR_V5_DET_MODEL);
        //指定检测模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setDetModelPath("/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        //指定识别模型
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PADDLEOCR_V5_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/PP-OCRv5_server_rec_infer/PP-OCRv5_server_rec.onnx");
        OcrCommonRecModel recModel = OcrModelFactory.getInstance().getRecModel(recModelConfig);
        OcrInfo ocrInfo = recModel.recognize("src/main/resources/handwriting_1.jpg");
        log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
    }

    /**
     * 文本识别（带方向矫正）
     * 本方法支持任意角度文字识别
     * 同时兼容印刷体和手写体文字。
     * 流程：文本检测 -> 方向检测 -> 方向矫正 ->  文本识别
     * 模型需要放在单独文件夹
     */
    @Test
    public void recognize2(){
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定检测模型
        recModelConfig.setDetModelEnum(CommonDetModelEnum.PADDLEOCR_V5_DET_MODEL);
        //指定检测模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setDetModelPath("/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        //指定识别模型
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PADDLEOCR_V5_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/PP-OCRv5_server_rec_infer/PP-OCRv5_server_rec.onnx");
        //指定方向检测模型
        recModelConfig.setDirectionModelEnum(DirectionModelEnum.CH_PPOCR_MOBILE_V2_CLS);
        //指定方向模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setDirectionModelPath("/cls/ch_ppocr_mobile_v2.0_cls.onnx");
        OcrCommonRecModel recModel = OcrModelFactory.getInstance().getRecModel(recModelConfig);
        OcrInfo ocrInfo = recModel.recognize("src/main/resources/ocr_4.jpg");
        log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
    }



    /**
     * 文本识别并绘制结果
     * 本方法支持旋转角度范围为 -90 到 90 度的文字
     * 同时兼容印刷体和手写体文字。
     * 流程：文本检测 -> 文本识别
     * 模型需要放在单独文件夹
     */
    @Test
    public void recognizeAndDraw(){
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定检测模型
        recModelConfig.setDetModelEnum(CommonDetModelEnum.PADDLEOCR_V5_DET_MODEL);
        //指定检测模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setDetModelPath("/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        //指定识别模型
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PADDLEOCR_V5_REC_MODEL);
        //directionModelConfig.setDirectionModelEnum(DirectionModelEnum.CH_PPOCR_MOBILE_V2_CLS);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/PP-OCRv5_server_rec_infer/PP-OCRv5_server_rec.onnx");
        //directionModelConfig.setDirectionModelPath("/Users/wenjie/Documents/develop/ocr模型/ch_ppocr_mobile_v2.0_cls.onnx");
        OcrCommonRecModel recModel = OcrModelFactory.getInstance().getRecModel(recModelConfig);
        int fontSize = 20;
        recModel.recognizeAndDraw("src/main/resources/general_ocr_002.png", "output/general_ocr_002_recognized.png", fontSize);
    }




}

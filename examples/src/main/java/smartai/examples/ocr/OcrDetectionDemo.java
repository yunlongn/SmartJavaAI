package smartai.examples.ocr;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.objectdetection.model.DetectorModel;
import cn.smartjavaai.objectdetection.model.ObjectDetectionModelFactory;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * OCR 文本检测 示例
 * 模型下载地址：https://pan.baidu.com/s/1MLfd73Vjdpnuls9-oqc9uw?pwd=1234 提取码: 1234
 * @author dwj
 * @date 2025/5/25
 */
@Slf4j
public class OcrDetectionDemo {


    /**
     * 文本检测
     * 检测图像中的文本区域，仅返回文本框位置，不识别文字内容
     * 模型需要放在单独文件夹
     */
    @Test
    public void detect(){
        OcrDetModelConfig config = new OcrDetModelConfig();
        //指定检测模型
        config.setModelEnum(CommonDetModelEnum.PADDLEOCR_V5_DET_MODEL);
        //指定模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        config.setDetModelPath("/Users/xxx/Documents/develop/ocr模型/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        OcrCommonDetModel model = OcrModelFactory.getInstance().getDetModel(config);
        List<OcrBox> boxes = model.detect("src/main/resources/ocr_1.jpg");
        log.info("OCR检测结果：{}", JSONObject.toJSONString(boxes));
    }

    /**
     * 文本检测并绘制结果
     * 检测图像中的文本区域，仅检测文本框位置，不识别文字内容
     * 模型需要放在单独文件夹
     */
    @Test
    public void detectAndDraw(){
        OcrDetModelConfig config = new OcrDetModelConfig();
        //指定检测模型
        config.setModelEnum(CommonDetModelEnum.PADDLEOCR_V5_DET_MODEL);
        //指定模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        config.setDetModelPath("/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        OcrCommonDetModel model = OcrModelFactory.getInstance().getDetModel(config);
        model.detectAndDraw("src/main/resources/ocr_1.jpg",  "output/ocr_1_detected.jpg");
    }




}

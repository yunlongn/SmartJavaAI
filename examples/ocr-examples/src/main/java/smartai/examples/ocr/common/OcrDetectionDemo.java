package smartai.examples.ocr.common;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.OcrRecOptions;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.utils.OcrUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * OCR 文本检测 示例
 * 模型下载地址：https://pan.baidu.com/s/15Noz2xHQzqMQSl1B19BobQ?pwd=1234 提取码: 1234
 * @author dwj
 */
@Slf4j
public class OcrDetectionDemo {


    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    /**
     * 获取文本检测模型
     * @return
     */
    public OcrCommonDetModel getDetectionModel() {
        OcrDetModelConfig config = new OcrDetModelConfig();
        //指定检测模型
        config.setModelEnum(CommonDetModelEnum.PP_OCR_V5_MOBILE_DET_MODEL);
        //指定模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        config.setDetModelPath("/Users/xxx/Documents/develop/model/ocr/PP-OCRv5_mobile_det_infer/PP-OCRv5_mobile_det_infer.onnx");
        config.setDevice(device);
        return OcrModelFactory.getInstance().getDetModel(config);
    }


    /**
     * 文本检测
     * 检测图像中的文本区域，仅返回文本框位置，不识别文字内容
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void detect(){
        try {
            OcrCommonDetModel model = getDetectionModel();
            List<OcrBox> boxes = model.detect("src/main/resources/ocr_1.jpg");
            log.info("OCR检测结果：{}", JSONObject.toJSONString(boxes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文本检测并绘制结果
     * 检测图像中的文本区域，仅检测文本框位置，不识别文字内容
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void detectAndDraw(){
        try {
            OcrCommonDetModel model = getDetectionModel();
            model.detectAndDraw("src/main/resources/ocr_1.jpg",  "output/ocr_1_detected.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 批量文本检测：批量检测要求图片宽高一致
     * 检测图像中的文本区域，仅返回文本框位置，不识别文字内容
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void batchDetect(){
        try {
            OcrCommonDetModel model = getDetectionModel();
            //批量检测要求图片宽高一致
            String folderPath = "/Users/xxx/Downloads/testing33";
            //读取文件夹中所有图片
            List<Image> images = ImageUtils.readImagesFromFolder(folderPath);
            List<List<OcrBox>> ocrResult = model.batchDetectDJLImage(images);
            for(int i = 0; i < ocrResult.size(); i++){
                log.info("图片" + i + "文本检测结果：{}", JSONObject.toJSONString(ocrResult.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

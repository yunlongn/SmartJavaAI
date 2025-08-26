package smartai.examples.ocr.common;

import ai.djl.modality.cv.Image;
import ai.djl.util.JsonUtils;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.config.OcrRecOptions;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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


    @BeforeClass
    public static void beforeAll() throws IOException {
        //修改缓存路径
        //Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }

    /**
     * 获取通用识别模型（不带方向矫正）
     * @return
     */
    public OcrCommonRecModel getRecModel(){
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定文本识别模型
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PP_OCR_V5_MOBILE_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_mobile_rec_infer/PP-OCRv5_mobile_rec_infer.onnx");
        recModelConfig.setDevice(device);
        recModelConfig.setTextDetModel(getDetectionModel());
        return OcrModelFactory.getInstance().getRecModel(recModelConfig);
    }

    /**
     * 获取文本检测模型
     * @return
     */
    public OcrCommonDetModel getDetectionModel() {
        OcrDetModelConfig config = new OcrDetModelConfig();
        //指定检测模型
        config.setModelEnum(CommonDetModelEnum.PP_OCR_V5_MOBILE_DET_MODEL);
        //指定模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        config.setDetModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_mobile_det_infer/PP-OCRv5_mobile_det_infer.onnx");
        config.setDevice(device);
        return OcrModelFactory.getInstance().getDetModel(config);
    }

    /**
     * 获取方向检测模型
     * @return
     */
    public OcrDirectionModel getDirectionModel(){
        DirectionModelConfig directionModelConfig = new DirectionModelConfig();
        //指定行文本方向检测模型
        directionModelConfig.setModelEnum(DirectionModelEnum.PP_LCNET_X0_25);
        //指定行文本方向检测模型路径，需要更改为自己的模型路径（下载地址请查看文档）
        directionModelConfig.setModelPath("/Users/xxx/Documents/develop/model/ocr/PP-LCNet_x0_25_textline_ori_infer/PP-LCNet_x0_25_textline_ori_infer.onnx");
        directionModelConfig.setDevice(device);
        return OcrModelFactory.getInstance().getDirectionModel(directionModelConfig);
    }


    /**
     * 获取通用识别模型(带方向矫正)
     * @return
     */
    public OcrCommonRecModel getRecModelWithDirection() {
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定文本识别模型
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PP_OCR_V5_MOBILE_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/Users/xxx/Documents/develop/model/ocr/PP-OCRv5_mobile_rec_infer/PP-OCRv5_mobile_rec_infer.onnx");
        recModelConfig.setDevice(device);
        recModelConfig.setTextDetModel(getDetectionModel());
        recModelConfig.setDirectionModel(getDirectionModel());
        return OcrModelFactory.getInstance().getRecModel(recModelConfig);
    }


    /**
     * 文本识别
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 流程：文本检测 -> 文本识别
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void recognize(){
        try {
            OcrCommonRecModel recModel = getRecModel();
            //不带方向矫正，分行返回文本
            OcrRecOptions options = new OcrRecOptions(false, true);
            OcrInfo ocrInfo = recModel.recognize("src/main/resources/ocr_2.jpg",options);
            log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 文本识别（手写字）
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 流程：文本检测 -> 文本识别
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void recognizeHandWriting(){
        try {
            OcrCommonRecModel recModel = getRecModel();
            OcrInfo ocrInfo = recModel.recognize("src/main/resources/handwriting_1.jpg",new OcrRecOptions());
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
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void recognize2(){
        try {
            OcrCommonRecModel recModel = getRecModelWithDirection();
            //带方向矫正，分行返回文本
            OcrRecOptions options = new OcrRecOptions(true, true);
            OcrInfo ocrInfo = recModel.recognize("src/main/resources/ocr_3.jpg",options);
            log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 文本识别并绘制结果
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 流程：文本检测 -> 文本识别
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void recognizeAndDraw(){
        try {
            OcrCommonRecModel recModel = getRecModelWithDirection();
            int fontSize = 18;
            recModel.recognizeAndDraw("src/main/resources/general_ocr_002.png", "output/ocr_4_recognized.jpg", fontSize, new OcrRecOptions());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void recognizeAndDraw2(){
        try {
            OcrCommonRecModel recModel = getRecModel();
            int fontSize = 18;
            //创建保存路径
            Path inputImagePath = Paths.get("src/main/resources/general_ocr_002.png");
            Path imageOutputPath = Paths.get("output/ocr_4_recognized.jpg");
            BufferedImage image = null;
            image = ImageIO.read(new File(inputImagePath.toAbsolutePath().toString()));
            BufferedImage resultImage = recModel.recognizeAndDraw(image, fontSize, new OcrRecOptions());
            ImageUtils.saveImage(resultImage, imageOutputPath.toAbsolutePath().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文本识别并绘制结果（返回base64）
     */
    @Test
    public void recognizeAndDrawToBase64(){
        try {
            OcrCommonRecModel recModel = getRecModel();
            int fontSize = 18;
            //创建保存路径
            Path inputImagePath = Paths.get("src/main/resources/general_ocr_002.png");
            byte[] imageBytes = FileUtil.readBytes(inputImagePath);
            String base64 = recModel.recognizeAndDrawToBase64(imageBytes, fontSize, new OcrRecOptions());
            log.info("base64:{}", base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文本识别并绘制结果（返回OcrInfo,OcrInfo中包含base64）
     */
    @Test
    public void recognizeAndDraw3(){
        try {
            OcrCommonRecModel recModel = getRecModel();
            int fontSize = 18;
            //创建保存路径
            Path inputImagePath = Paths.get("src/main/resources/general_ocr_002.png");
            byte[] imageBytes = FileUtil.readBytes(inputImagePath);
            OcrInfo ocrInfo = recModel.recognizeAndDraw(imageBytes, fontSize, new OcrRecOptions());
            log.info("ocrInfo:{}", JsonUtils.toJson(ocrInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量识别
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void batchRecognize(){
        try {
            OcrCommonRecModel recModel = getRecModelWithDirection();
            //批量检测要求图片宽高一致
            String folderPath = "/Users/xxx/Downloads/testing33";
            //读取文件夹中所有图片
            List<Image> images = ImageUtils.readImagesFromFolder(folderPath);
            //带方向矫正，分行返回文本
            OcrRecOptions options = new OcrRecOptions(true, true);
            List<OcrInfo> ocrResult = recModel.batchRecognizeDJLImage(images, options);
            for(int i = 0; i < ocrResult.size(); i++){
                log.info("图片" + i + "文本识别结果：{}", JSONObject.toJSONString(ocrResult.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}

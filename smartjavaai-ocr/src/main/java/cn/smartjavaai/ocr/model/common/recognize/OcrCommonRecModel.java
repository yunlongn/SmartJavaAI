package cn.smartjavaai.ocr.model.common.recognize;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * OCR 通用识别模型
 * @author dwj
 */
public interface OcrCommonRecModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(OcrRecModelConfig config); // 加载模型

    /**
     * 文本识别
     * @param imagePath 图片路径
     * @return
     */
    default OcrInfo recognize(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本检测
     * @param image BufferedImage
     * @return
     */
    default OcrInfo recognize(BufferedImage image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本检测
     * @param imageData 图片字节数组
     * @return
     */
    default OcrInfo recognize(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 识别并绘制结果
     * @param imagePath
     * @param outputPath
     */
    default void recognizeAndDraw(String imagePath, String outputPath, int fontSize) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 识别并绘制结果
     * @param sourceImage
     * @return
     */
    default BufferedImage recognizeAndDraw(BufferedImage sourceImage, int fontSize){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}

package cn.smartjavaai.ocr.model.common.detect;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * OCR 通用检测模型
 * @author dwj
 */
public interface OcrCommonDetModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(OcrDetModelConfig config); // 加载模型

    /**
     * 文本检测
     * @param imagePath 图片路径
     * @return
     */
    default List<OcrBox> detect(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本检测
     * @param image BufferedImage
     * @return
     */
    default List<OcrBox> detect(BufferedImage image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本检测
     * @param imageData 图片字节数组
     * @return
     */
    default List<OcrBox> detect(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 文本检测
     * @param image DJL Image
     * @return
     */
    default List<OcrBox> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制结果
     * @param imagePath 图片输入路径（包含文件名称）
     * @param outputPath 图片输出路径（包含文件名称）
     */
    default void detectAndDraw(String imagePath, String outputPath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制结果
     * @param sourceImage
     * @return
     */
    default BufferedImage detectAndDraw(BufferedImage sourceImage){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 文本检测（批量）
     * @param imageList BufferedImage
     * @return
     */
    default List<List<OcrBox>> batchDetect(List<BufferedImage> imageList) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 文本检测（批量）
     * @param imageList DJL Image
     * @return
     */
    default List<List<OcrBox>> batchDetectDJLImage(List<Image> imageList){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


}

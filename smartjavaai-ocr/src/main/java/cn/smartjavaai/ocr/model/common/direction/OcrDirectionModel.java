package cn.smartjavaai.ocr.model.common.direction;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDManager;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.entity.DirectionInfo;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.entity.OcrItem;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * OCR 文本方向分类模型
 * @author dwj
 */
public interface OcrDirectionModel {

    /**
     * 加载模型
     * @param config
     */
    void loadModel(DirectionModelConfig config); // 加载模型

    /**
     * 文本方向检测
     * @param imagePath 图片路径
     * @return
     */
    default List<OcrItem> detect(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本方向检测
     * @param image BufferedImage
     * @return
     */
    default List<OcrItem> detect(BufferedImage image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本方向检测
     * @param imageData 图片字节数组
     * @return
     */
    default List<OcrItem> detect(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 文本方向检测
     * @param image
     * @return
     */
    default List<OcrItem> detect(Image image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本方向检测（基于检测结果）
     * @param boxList
     * @param srcMat
     * @param manager
     * @return
     */
    default List<OcrItem> detect(List<OcrBox> boxList, Mat srcMat, NDManager manager) {
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

}

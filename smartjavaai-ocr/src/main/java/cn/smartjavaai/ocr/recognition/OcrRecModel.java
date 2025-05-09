package cn.smartjavaai.ocr.recognition;

import cn.smartjavaai.common.entity.DetectionResponse;

/**
 * OCR模型
 * @author dwj
 */
public interface OcrRecModel {

    /**
     * 加载模型
     * @param config
     */
    void loadModel(OcrRecModelConfig config); // 加载模型

    /**
     * 人脸检测
     * @param imagePath 图片路径
     * @return
     */
    default DetectionResponse detect(String imagePath) {
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



}

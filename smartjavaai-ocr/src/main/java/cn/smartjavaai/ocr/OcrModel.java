package cn.smartjavaai.ocr;

import cn.smartjavaai.common.entity.DetectionResponse;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * 人脸识别算法
 * @author dwj
 */
public interface OcrModel {

    /**
     * 加载模型
     * @param config
     */
    void loadModel(OcrModelConfig config); // 加载模型


    /**
     * 人脸检测
     * @param imagePath 图片路径
     * @return
     */
    DetectionResponse detect(String imagePath);

    /**
     * 检测并绘制结果
     * @param imagePath 图片输入路径（包含文件名称）
     * @param outputPath 图片输出路径（包含文件名称）
     */
    void detectAndDraw(String imagePath, String outputPath);

}

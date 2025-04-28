package cn.smartjavaai.ocr;

import cn.smartjavaai.common.entity.DetectionResponse;

/**
 * 人脸识别算法
 * @author dwj
 */
public abstract class AbstractOcrModel implements OcrModel {
    @Override
    public void loadModel(OcrModelConfig config) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public DetectionResponse detect(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public void detectAndDraw(String imagePath, String outputPath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }
}

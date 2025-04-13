package cn.smartjavaai.face;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.face.entity.FaceResult;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * 人脸识别算法
 * @author dwj
 */
public abstract class AbstractFaceModel implements FaceModel {
    @Override
    public void loadModel(FaceModelConfig config) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public DetectionResponse detect(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public DetectionResponse detect(InputStream imageInputStream) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public DetectionResponse detect(BufferedImage image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public DetectionResponse detect(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public void detectAndDraw(String imagePath, String outputPath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public BufferedImage detectAndDraw(BufferedImage sourceImage) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float[] featureExtraction(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float[] featureExtraction(InputStream inputStream) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float calculSimilar(float[] feature1, float[] feature2) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float featureComparison(String imagePath1, String imagePath2) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float featureComparison(InputStream inputStream1, InputStream inputStream2) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public boolean register(String key, String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public boolean register(String key, InputStream inputStream) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public boolean register(String key, BufferedImage sourceImage) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public boolean register(String key, byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public FaceResult search(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public FaceResult search(InputStream inputStream) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public long removeRegister(String... keys) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public long clearFace() {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float[] featureExtraction(BufferedImage sourceImage) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float[] featureExtraction(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float featureComparison(BufferedImage sourceImage1, BufferedImage sourceImag2) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float featureComparison(byte[] imageData1, byte[] imageData2) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public FaceResult search(BufferedImage sourceImage) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public FaceResult search(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }
}

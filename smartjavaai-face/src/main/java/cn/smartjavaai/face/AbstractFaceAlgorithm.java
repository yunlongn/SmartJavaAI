package cn.smartjavaai.face;

import java.io.InputStream;

/**
 * 人脸识别算法
 * @author dwj
 */
public abstract class AbstractFaceAlgorithm implements FaceAlgorithm{
    @Override
    public void loadModel(ModelConfig config) throws Exception {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public FaceDetectedResult detect(String imagePath) throws Exception {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public FaceDetectedResult detect(InputStream imageInputStream) throws Exception {
        return null;
    }

    @Override
    public float[] featureExtraction(String imagePath) throws Exception {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float[] featureExtraction(InputStream inputStream) throws Exception {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float calculSimilar(float[] feature1, float[] feature2) throws Exception {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float featureComparison(String imagePath1, String imagePath2) throws Exception {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public float featureComparison(InputStream inputStream1, InputStream inputStream2) throws Exception {
        throw new UnsupportedOperationException("默认不支持该功能");
    }
}

package cn.smartjavaai.face;

import cn.smartjavaai.face.entity.FaceResult;

import java.io.IOException;
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
    public void loadFaceFeatureModel(ModelConfig config) throws Exception {
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

    @Override
    public boolean register(String key, String imagePath) throws Exception {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public boolean register(String key, InputStream inputStream) throws Exception {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public FaceResult search(String imagePath) throws Exception{
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public FaceResult search(InputStream inputStream) throws Exception{
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public long removeRegister(String... keys) throws Exception{
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Override
    public long clearFace() throws Exception{
        throw new UnsupportedOperationException("默认不支持该功能");
    }
}

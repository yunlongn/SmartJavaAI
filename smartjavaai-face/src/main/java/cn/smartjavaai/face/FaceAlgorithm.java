package cn.smartjavaai.face;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import cn.smartjavaai.face.entity.FaceResult;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * 人脸识别算法
 * @author dwj
 */
public interface FaceAlgorithm {

    /**
     * 加载模型
     * @param config
     * @throws Exception
     */
    void loadModel(ModelConfig config) throws Exception; // 加载模型

    /**
     * 加载人脸特征提取模型
     * @param config
     * @throws Exception
     */
    void loadFaceFeatureModel(ModelConfig config) throws Exception; // 加载模型

    /**
     * 人脸检测
     * @param imagePath 图片路径
     * @return
     * @throws Exception
     */
    FaceDetectedResult detect(String imagePath) throws Exception;

    /**
     * 人脸检测
     * @param imageInputStream 图片输入流
     * @return
     * @throws Exception
     */
    FaceDetectedResult detect(InputStream imageInputStream) throws Exception;

    /**
     * 特征提取
     * @param imagePath 图片路径
     * @return
     * @throws Exception
     */
    float[] featureExtraction(String imagePath) throws Exception;

    /**
     * 特征提取
     * @param inputStream 输入流
     * @return
     * @throws Exception
     */
    float[] featureExtraction(InputStream inputStream) throws Exception;

    /**
     * 计算相似度
     * @param feature1 图1特征
     * @param feature2 图2特征
     * @return
     * @throws Exception
     */
    float calculSimilar(float[] feature1, float[] feature2) throws Exception;

    /**
     * 特征比较
     * @param imagePath1 图1路径
     * @param imagePath2 图2路径
     * @return
     * @throws Exception
     */
    float featureComparison(String imagePath1, String imagePath2) throws Exception;

    /**
     * 特征比较
     * @param inputStream1 图1输入流
     * @param inputStream2 图2输入流
     * @return
     * @throws Exception
     */
    float featureComparison(InputStream inputStream1, InputStream inputStream2) throws Exception;

    /**
     * 注册人脸
     * @param key
     * @param imagePath
     * @return
     */
    boolean register(String key, String imagePath) throws Exception;

    /**
     * 注册人脸
     * @param key
     * @param inputStream
     * @return
     */
    boolean register(String key, InputStream inputStream) throws Exception;

    /**
     * 查询人脸
     * @param imagePath
     * @return
     */
    FaceResult search(String imagePath) throws Exception;

    /**
     * 查询人脸
     * @param inputStream
     * @return
     */
    FaceResult search(InputStream inputStream) throws Exception;

    /**
     * 删除已标记人脸
     * @param keys
     * @return
     */
    long removeRegister(String... keys) throws Exception;

    /**
     * 清空人脸库数据
     */
    long clearFace() throws Exception;

}

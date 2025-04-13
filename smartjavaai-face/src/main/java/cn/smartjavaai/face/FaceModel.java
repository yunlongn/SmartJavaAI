package cn.smartjavaai.face;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.face.entity.FaceResult;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * 人脸识别算法
 * @author dwj
 */
public interface FaceModel {

    /**
     * 加载模型
     * @param config
     */
    void loadModel(FaceModelConfig config); // 加载模型


    /**
     * 人脸检测
     * @param imagePath 图片路径
     * @return
     */
    DetectionResponse detect(String imagePath);

    /**
     * 人脸检测
     * @param imageInputStream 图片输入流
     * @return
     */
    DetectionResponse detect(InputStream imageInputStream);

    /**
     * 人脸检测
     * @param image BufferedImage
     * @return
     */
    DetectionResponse detect(BufferedImage image);

    /**
     * 人脸检测
     * @param imageData
     * @return
     */
    DetectionResponse detect(byte[] imageData);

    /**
     * 检测并绘制人脸
     * @param imagePath 图片输入路径（包含文件名称）
     * @param outputPath 图片输出路径（包含文件名称）
     */
    void detectAndDraw(String imagePath, String outputPath);

    /**
     * 检测并绘制人脸
     * @param sourceImage
     * @return
     */
    BufferedImage detectAndDraw(BufferedImage sourceImage);

    /**
     * 特征提取
     * @param imagePath 图片路径
     * @return
     */
    float[] featureExtraction(String imagePath);

    /**
     * 特征提取
     * @param inputStream 输入流
     * @return
     */
    float[] featureExtraction(InputStream inputStream);

    /**
     * 特征提取
     * @param sourceImage BufferedImage图片数据
     * @return
     */
    float[] featureExtraction(BufferedImage sourceImage);

    /**
     * 特征提取
     * @param imageData 图片字节流
     * @return
     */
    float[] featureExtraction(byte[] imageData);

    /**
     * 计算相似度
     * @param feature1 图1特征
     * @param feature2 图2特征
     * @return
     */
    float calculSimilar(float[] feature1, float[] feature2);

    /**
     * 特征比较
     * @param imagePath1 图1路径
     * @param imagePath2 图2路径
     * @return
     */
    float featureComparison(String imagePath1, String imagePath2);

    /**
     * 特征比较
     * @param sourceImage1 图1BufferedImage
     * @param sourceImag2 图2BufferedImage
     * @return
     */
    float featureComparison(BufferedImage sourceImage1, BufferedImage sourceImag2);

    /**
     * 特征比较
     * @param inputStream1 图1输入流
     * @param inputStream2 图2输入流
     * @return
     */
    float featureComparison(InputStream inputStream1, InputStream inputStream2);


    /**
     * 特征比较
     * @param imageData1
     * @param imageData2
     * @return
     */
    float featureComparison(byte[] imageData1, byte[] imageData2);

    /**
     * 注册人脸
     * @param key
     * @param imagePath
     * @return
     */
    boolean register(String key, String imagePath);

    /**
     * 注册人脸
     * @param key
     * @param inputStream
     * @return
     */
    boolean register(String key, InputStream inputStream);

    /**
     * 注册人脸
     * @param key
     * @param sourceImage
     * @return
     */
    boolean register(String key, BufferedImage sourceImage);


    /**
     * 注册人脸
     * @param key
     * @param imageData
     * @return
     */
    boolean register(String key, byte[] imageData);

    /**
     * 查询人脸
     * @param imagePath
     * @return
     */
    FaceResult search(String imagePath);

    /**
     * 查询人脸
     * @param inputStream
     * @return
     */
    FaceResult search(InputStream inputStream);

    /**
     * 查询人脸
     * @param sourceImage
     * @return
     */
    FaceResult search(BufferedImage sourceImage);

    /**
     * 查询人脸
     * @param imageData
     * @return
     */
    FaceResult search(byte[] imageData);

    /**
     * 删除已标记人脸
     * @param keys
     * @return
     */
    long removeRegister(String... keys);

    /**
     * 清空人脸库数据
     */
    long clearFace();

}

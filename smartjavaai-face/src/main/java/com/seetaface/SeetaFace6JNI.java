package com.seetaface;


import com.seetaface.model.RecognizeResult;
import com.seetaface.model.SeetaImageData;
import com.seetaface.model.SeetaPointF;
import com.seetaface.model.SeetaRect;

/**
 * seetaface6 sdk
 * @author dwj
 */
public class SeetaFace6JNI {

    /**
     * 初始化，指定人脸识别模型文件目录
     *
     * @param modelDir
     * @return
     */

    public native boolean initModel(String modelDir);

    /**
     * 检测人脸
     *
     * @param img
     * @return
     */
    public native SeetaRect[] detect(SeetaImageData img);

    /**
     * 根据人脸检测关键点
     * 关键定定位输入的是原始图片和人脸检测结果，给出指定人脸上的关键点的依次坐标。
     * 这里检测到的5点坐标循序依次为，左眼中心、右眼中心、鼻尖、左嘴角和右嘴角。
     * 注意这里的左右是基于图片内容的左右，并不是图片中人的左右，即左眼中心就是图片中左边的眼睛的中心。
     *
     * @param img
     * @param faces
     * @return
     */
    public native SeetaPointF[] mark(SeetaImageData img, SeetaRect faces);

    /**
     * 1 v 1 人脸比对
     *
     * @param img1
     * @param img2
     * @return 相似度范围在0~1,返回负数表示出错
     */
    public native float compare(SeetaImageData img1, SeetaImageData img2);

    /**
     * 提取人脸区域特性
     * @param face crop方法返回的人脸图像
     * @return
     */
    public native float[] extractCroppedFace(byte[] face);

    /**
     * 提取一个图像中最大人脸的特征
     * @param img
     * @return
     */
    public native float[] extractMaxFace(SeetaImageData img);

    /**
     * 计算两个特性的相似度
     * @param features1
     * @param features2
     * @return
     */
    public native float calculateSimilarity(float[] features1, float[] features2);

    /**
     * 注册人脸
     *
     * @param img
     * @return The returned value is the index of face database. Reture -1 if failed
     */
    public native long register(SeetaImageData img);

    /**
     * 注册裁剪后的人脸，推荐使用该方法
     * @param bytes
     * @return
     */
    public native long registerCroppedFace(byte[] bytes);

    /**
     * 从人脸库中搜索，返回相似度最高的索引
     *
     * @param img
     * @return index saves the index of face databese, which is same as the retured value by Register. similar saves the most similar.
     */
    public native RecognizeResult query(SeetaImageData img);

    /**
     * 用裁剪后的人脸进行搜索
     * @param bytes
     * @return
     */
    public native RecognizeResult queryByCroppedFace(byte[] bytes);

    /**
     * 将人脸从数据库中删除
     * @param index -1: 删除所有
     * @return 返回删除记录数
     */
    public native long delete(long[] index);

    /**
     * 人脸提取
     *
     * @param img
     * @return The returned value is face data. Reture null if failed
     */
    public native byte[][] crop(SeetaImageData img);

    /**
     * 图片活体检测
     * @param img
     * @return
     */
    public native int predictImage(SeetaImageData img);

    public native void dispose();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.dispose();
    }

}

package cn.smartjavaai.common.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.util.RandomUtils;
import cn.smartjavaai.common.entity.DetectionInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;
import java.util.Objects;

/**
 * OpenCV 工具类
 */
public class OpenCVUtils {
    /**
     * canny算法，边缘检测
     *
     * @param src
     * @return
     */
    public static Mat canny(Mat src) {
        Mat mat = src.clone();
        Imgproc.Canny(src, mat, 100, 200);
        return mat;
    }

    /**
     * 画线
     *
     * @param mat
     * @param point1
     * @param point2
     */
    public static void line(Mat mat, Point point1, Point point2) {
        Imgproc.line(mat, point1, point2, new Scalar(255, 255, 255), 1);
    }

    /**
     * NDArray to opencv_core.Mat
     *
     * @param manager
     * @param srcPoints
     * @param dstPoints
     * @return
     */
    public static Mat toOpenCVMat(NDManager manager, NDArray srcPoints, NDArray dstPoints) {
        NDArray svdMat = SVDUtils.transformationFromPoints(manager, srcPoints, dstPoints);
        double[] doubleArray = svdMat.toDoubleArray();
        Mat newSvdMat = new Mat(2, 3, CvType.CV_64F);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                newSvdMat.put(i, j, doubleArray[i * 3 + j]);
            }
        }
        return newSvdMat;
    }

    /**
     * double[][] points array to Mat
     * @param points
     * @return
     */
    public static Mat toOpenCVMat(double[][] points) {
        Mat mat = new Mat(5, 2, CvType.CV_64F);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                mat.put(i, j, points[i * 5 + j]);
            }
        }
        return mat;
    }

    /**
     * 变换矩阵的逆矩阵
     *
     * @param src
     * @return
     */
    public static Mat invertAffineTransform(Mat src) {
        Mat dst = src.clone();
        Imgproc.invertAffineTransform(src, dst);
        return dst;
    }

    /**
     * Mat to BufferedImage
     *
     * @param mat
     * @return
     */
    public static BufferedImage mat2Image(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        byte[] data = new byte[width * height * (int) mat.elemSize()];
        Imgproc.cvtColor(mat, mat, 4);
        mat.get(0, 0, data);
        BufferedImage ret = new BufferedImage(width, height, 5);
        ret.getRaster().setDataElements(0, 0, width, height, data);
        return ret;
    }

    /**
     * BufferedImage to Mat
     *
     * @param img
     * @return
     */
    public static Mat image2Mat(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // 强制转换为 TYPE_3BYTE_BGR，自动去除透明通道
        BufferedImage convertedImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = convertedImg.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        byte[] data = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(height, width, CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    /**
     * 透视变换
     *
     * @param src
     * @param srcPoints
     * @param dstPoints
     * @return
     */
    public static Mat perspectiveTransform(Mat src, Mat srcPoints, Mat dstPoints) {
        Mat dst = src.clone();
        Mat warp_mat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        Imgproc.warpPerspective(src, dst, warp_mat, dst.size());
        warp_mat.release();
        return dst;
    }

    /**
     * 绘制矩形框和文字
     *
     * @param image
     * @param detectionInfoList
     */
    public static void drawRectAndText(Image image, List<DetectionInfo> detectionInfoList) {
        if(CollectionUtils.isEmpty(detectionInfoList))
            return;
        for(DetectionInfo detectionInfo : detectionInfoList){
            drawRectAndText(image, detectionInfo);
        }
    }


    /**
     * 绘制矩形框和文字
     *
     * @param image
     * @param detectionInfo
     */
    public static void drawRectAndText(Image image, DetectionInfo detectionInfo) {


        Mat mat = (Mat)image.getWrappedImage();
        if (image == null) return;
        int x = detectionInfo.getDetectionRectangle().getX();
        int y = detectionInfo.getDetectionRectangle().getY();
        int width = detectionInfo.getDetectionRectangle().getWidth();
        int height = detectionInfo.getDetectionRectangle().getHeight();

        Scalar rectangleColor = new Scalar((double)RandomUtils.nextInt(178), (double)RandomUtils.nextInt(178), (double)RandomUtils.nextInt(178));

        // 绘制矩形框
        Point pt1 = new Point(x, y);
        Point pt2 = new Point(x + width, y + height);
        Imgproc.rectangle(mat, pt1, pt2, rectangleColor, 2);

        // 绘制文字
        if (Objects.nonNull(detectionInfo.getObjectDetInfo()) && StringUtils.isNotBlank(detectionInfo.getObjectDetInfo().getClassName())) {
            String className = detectionInfo.getObjectDetInfo().getClassName();
            Size size = Imgproc.getTextSize(className, 1, 1.3, 1, (int[])null);
            Point br = new Point((double)x + size.width + 4.0, (double)y + size.height + 4.0);
            Imgproc.rectangle(mat, pt1, br, rectangleColor, -1);
            Point point = new Point((double)x, (double)y + size.height + 2.0);
            Scalar color = new Scalar(255.0, 255.0, 255.0);
            Imgproc.putText(mat, className, point, 1, 1.3, color, 1);
        }
        image = ImageFactory.getInstance().fromImage(mat);
    }

    /**
     * 在Mat上绘制矩形框和文字
     *
     * @param mat       待绘制的Mat
     * @param x         矩形左上角X
     * @param y         矩形左上角Y
     * @param width     矩形宽度
     * @param height    矩形高度
     * @param color     框的颜色，例如 new Scalar(0, 255, 0) 绿色
     * @param thickness 框线宽度
     * @param text      需要绘制的文字，可以为null或空
     * @param fontScale 文字缩放比例
     * @param textColor 文字颜色
     */
    public static void drawRectAndText(Mat mat,
                                       int x, int y, int width, int height,
                                       Scalar color, int thickness,
                                       String text, double fontScale, Scalar textColor) {

        if (mat == null || mat.empty()) return;

        // 绘制矩形框
        Point pt1 = new Point(x, y);
        Point pt2 = new Point(x + width, y + height);
        Imgproc.rectangle(mat, pt1, pt2, color, thickness);

        // 绘制文字
        if (text != null && !text.isEmpty()) {
            int baseline[] = new int[1];
            Size textSize = Imgproc.getTextSize(text, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, thickness, baseline);
            // 保证文字不超出矩形
            Point textOrg = new Point(x, y - 5 < 0 ? y + textSize.height + 5 : y - 5);
            Imgproc.putText(mat, text, textOrg, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, textColor, thickness);
        }
    }

    /**
     * 将 Bytedeco 的 Mat 转换为 OpenCV 官方的 Mat
     * @param src Bytedeco Mat (BGR 或 BGRA)
     * @return OpenCV Mat (BGR 或 BGRA)
     */
    public static org.opencv.core.Mat convertToOpenCVMat(org.bytedeco.opencv.opencv_core.Mat bMat) {


        try {
            int width = bMat.cols();
            int height = bMat.rows();
            int channels = bMat.channels();

            // 创建 OpenCV Mat
            org.opencv.core.Mat cvMat = new org.opencv.core.Mat(height, width, channels == 3 ? CvType.CV_8UC3 : CvType.CV_8UC1);

            // 从 bytedeco Mat 获取像素数据
            byte[] data = new byte[width * height * channels];
            bMat.data().get(data);

            // 填充到 OpenCV Mat
            cvMat.put(0, 0, data);
            return cvMat;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}

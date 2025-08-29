package cn.smartjavaai.common.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.CvMat;
import org.bytedeco.opencv.opencv_core.Mat;
import org.opencv.core.CvType;

import java.awt.image.BufferedImage;

/**
 * @author dwj
 * @date 2025/8/27
 */
public class FrameConverterUtil {

    /**
     * 将 Bytedeco Mat 转为 DJL Image
     * 支持 1/3/4 通道
     */
    public static Image matToDJLImage(Mat cvMat) {
        if (cvMat == null || cvMat.empty()) {
            return null;
        }

        int width = cvMat.cols();
        int height = cvMat.rows();
        int channels = cvMat.channels();

        int[] pixels = new int[width * height];

        if (channels == 1) { // 灰度图
            byte[] data = new byte[width * height];
            cvMat.data().get(data);
            for (int i = 0; i < width * height; i++) {
                int gray = data[i] & 0xFF;
                pixels[i] = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
            }
        } else if (channels == 3) { // BGR
            byte[] data = new byte[width * height * 3];
            cvMat.data().get(data);
            for (int i = 0; i < width * height; i++) {
                int b = data[i * 3] & 0xFF;
                int g = data[i * 3 + 1] & 0xFF;
                int r = data[i * 3 + 2] & 0xFF;
                pixels[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
            }
        } else if (channels == 4) { // BGRA
            byte[] data = new byte[width * height * 4];
            cvMat.data().get(data);
            for (int i = 0; i < width * height; i++) {
                int b = data[i * 4] & 0xFF;
                int g = data[i * 4 + 1] & 0xFF;
                int r = data[i * 4 + 2] & 0xFF;
                int a = data[i * 4 + 3] & 0xFF;
                pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        } else {
            throw new IllegalArgumentException("只支持 1/3/4 通道图像");
        }

        return ImageFactory.getInstance().fromPixels(pixels, width, height);
    }
}

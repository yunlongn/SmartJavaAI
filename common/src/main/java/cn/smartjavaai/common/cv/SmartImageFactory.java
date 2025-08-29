package cn.smartjavaai.common.cv;

import ai.djl.modality.cv.BufferedImageFactory;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.util.Utils;
import cn.smartjavaai.common.utils.Base64ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

/**
 * 图片处理工厂类
 * @author dwj
 */
public class SmartImageFactory extends BufferedImageFactory {

    private static volatile SmartImageFactory instance;

    public static SmartImageFactory newInstance() {
        if (instance == null) {
            synchronized (SmartImageFactory.class) {
                if (instance == null) {
                    instance = new SmartImageFactory();
                }
            }
        }
        return instance;
    }

    public static SmartImageFactory getInstance(){
        return newInstance();
    }

    public Image fromBufferedImage(BufferedImage sourceImage){
        return fromImage(OpenCVUtils.image2Mat(sourceImage));
    }

    public Image fromBase64(String base64Image) throws IOException {
        return fromUrl(base64Image);
    }

    public Image fromBytes(byte[] imageData){
        return fromImage(new ByteArrayInputStream(imageData));
    }

}

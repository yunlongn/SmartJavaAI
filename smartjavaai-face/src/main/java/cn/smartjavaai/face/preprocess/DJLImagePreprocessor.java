package cn.smartjavaai.face.preprocess;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.face.utils.FaceAlignUtils;
import cn.smartjavaai.face.utils.FaceUtils;
import org.opencv.core.Mat;

import java.util.Objects;

/**
 * 图片预处理
 * @author dwj
 * @date 2025/6/27
 */
public class DJLImagePreprocessor {

    private Image image;

    private NDManager manager;

    // 裁剪参数
    private boolean enableCrop = false;
    private DetectionRectangle cropRect;

    // 仿射变换参数
    private boolean enableAffine = false;
    private double[][] keyPoints;
    private int affineTargetWidth;
    private int affineTargetHeight;

    public DJLImagePreprocessor(Image image, NDManager manager) {
        this.image = image;
        this.manager = manager;
    }

    // 启用裁剪
    public DJLImagePreprocessor enableCrop(DetectionRectangle rect) {
        this.enableCrop = true;
        this.cropRect = rect;
        return this;
    }

    // 启用仿射变换
    public DJLImagePreprocessor enableAffine(double[][] keyPoints, int targetWidth, int targetHeight) {
        if(Objects.isNull(keyPoints)){
            throw new IllegalArgumentException("keyPoints must be not null");
        }
        this.enableAffine = true;
        this.affineTargetWidth = targetWidth;
        this.affineTargetHeight = targetHeight;
        this.keyPoints = keyPoints;
        return this;
    }

    // 处理流程
    public Image process() {
        Image result = image;
        if(enableAffine){
            result = warpAffine(keyPoints, affineTargetWidth, affineTargetHeight);
        }else {
            if(enableCrop){
                result = result.getSubImage(cropRect.x, cropRect.y, cropRect.width, cropRect.height);
            }
        }
        return result;
    }

    // 仿射变换
    private Image warpAffine(double[][] keyPoints, int width, int height) {
        NDArray srcPoints = manager.create(keyPoints);
        NDArray dstPoints = null;
        if(width == 512 && height == 512){
            dstPoints = FaceUtils.faceTemplate512x512(manager);
        }else if(width == 112 && height == 112){
            dstPoints = FaceUtils.faceTemplate112x112(manager);
        }else if(width == 96 && height == 112){
            dstPoints = FaceUtils.faceTemplate96x112(manager);
        }
        // 5点仿射变换
        Mat affine_matrix = OpenCVUtils.toOpenCVMat(manager, srcPoints, dstPoints);
        Mat mat = FaceAlignUtils.warpAffine((Mat) image.getWrappedImage(), affine_matrix, width, height);
        Image alignedImg = OpenCVImageFactory.getInstance().fromImage(mat);
        return alignedImg;
    }


}

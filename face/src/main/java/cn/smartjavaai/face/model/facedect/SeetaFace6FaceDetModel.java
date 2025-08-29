package cn.smartjavaai.face.model.facedect;

import ai.djl.engine.Engine;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.Base64ImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.seetaface.NativeLoader;
import cn.smartjavaai.face.utils.FaceUtils;
import com.seeta.pool.*;
import com.seeta.sdk.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SeetaFace6 人脸检测模型
 * @author dwj
 */
@Slf4j
public class SeetaFace6FaceDetModel implements FaceDetModel{
    private FaceDetConfig config;

    private FaceDetectorPool faceDetectorPool;
    private FaceLandmarkerPool faceLandmarkerPool;

    /**
     * 阈值
     */
    private static final double THRESHOLD = 0.9d;


    @Override
    public void loadModel(FaceDetConfig config) {
        this.config = config;
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
        //加载依赖库
        NativeLoader.loadNativeLibraries(config.getDevice());
        log.debug("Loading seetaFace6 library successfully.");
        String[] faceDetectorModelPath = {config.getModelPath() + File.separator + "face_detector.csta"};
        String[] faceLandmarkerModelPath = {config.getModelPath() + File.separator + "face_landmarker_pts5.csta"};
        SeetaDevice device = SeetaDevice.SEETA_DEVICE_AUTO;
        int gpuId = config.getGpuId();
        if(Objects.nonNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? SeetaDevice.SEETA_DEVICE_CPU : SeetaDevice.SEETA_DEVICE_GPU;
        }
        try {
            SeetaModelSetting faceDetectorPoolSetting = new SeetaModelSetting(gpuId, faceDetectorModelPath, device);
            SeetaConfSetting faceDetectorPoolConfSetting = new SeetaConfSetting(faceDetectorPoolSetting);

            SeetaModelSetting faceLandmarkerPoolSetting = new SeetaModelSetting(gpuId, faceLandmarkerModelPath, device);
            SeetaConfSetting faceLandmarkerPoolConfSetting = new SeetaConfSetting(faceLandmarkerPoolSetting);

            this.faceDetectorPool = new FaceDetectorPool(faceDetectorPoolConfSetting);
            this.faceLandmarkerPool = new FaceLandmarkerPool(faceLandmarkerPoolConfSetting);

            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            faceDetectorPool.setMaxTotal(predictorPoolSize);
            faceLandmarkerPool.setMaxTotal(predictorPoolSize);
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (FileNotFoundException e) {
            throw new FaceException(e);
        }

    }

    @Override
    public R<DetectionResponse> detect(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return detect(image);
    }

    @Override
    public R<DetectionResponse> detect(InputStream imageInputStream) {
        if(Objects.isNull(imageInputStream)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageInputStream);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }
        return detect(image);
    }

    @Override
    public R<DetectionResponse> detect(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        FaceDetector predictor = null;
        FaceLandmarker faceLandmarker = null;
        try {
            predictor = faceDetectorPool.borrowObject();
            predictor.set(FaceDetector.Property.PROPERTY_THRESHOLD, config.getConfidenceThreshold() > 0 ? config.getConfidenceThreshold() : THRESHOLD);
            faceLandmarker = faceLandmarkerPool.borrowObject();
            SeetaRect[] seetaResult = predictor.Detect(imageData);
            List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
            for(SeetaRect seetaRect : seetaResult){
                //提取人脸的5点人脸标识
                SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaRect, pointFS);
                seetaPointFSList.add(pointFS);
            }
            return R.ok(FaceUtils.convertToDetectionResponse(seetaResult, seetaPointFSList));
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    faceDetectorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<DetectionResponse> detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<DetectionResponse> detectBase64(String base64Image) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detect(imageData);
    }

    @Override
    public R<Void> detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        try {
            //创建保存路径
            Path imageOutputPath = Paths.get(outputPath);
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
            } catch (IOException e) {
                throw new FaceException("无效图片路径", e);
            }
            R<DetectionResponse> result = detect(image);
            if(result.getCode() != R.Status.SUCCESS.getCode()){
                return R.fail(result.getCode(), result.getMessage());
            }
            if(Objects.isNull(result.getData()) || Objects.isNull(result.getData().getDetectionInfoList()) || result.getData().getDetectionInfoList().isEmpty()){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            //绘制人脸框
            FaceUtils.drawBoundingBoxes(image, result.getData(), imageOutputPath.toAbsolutePath().toString());
            return R.ok();
        } catch (IOException e) {
            throw new FaceException(e);
        }
    }

    @Override
    public R<BufferedImage> detectAndDraw(BufferedImage sourceImage) {
        if(!ImageUtils.isImageValid(sourceImage)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        R<DetectionResponse> result = detect(sourceImage);
        if(result.getCode() != R.Status.SUCCESS.getCode()){
            return R.fail(result.getCode(), result.getMessage());
        }
        if(Objects.isNull(result.getData()) || Objects.isNull(result.getData().getDetectionInfoList()) || result.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        //绘制人脸框
        try {
            return R.ok(FaceUtils.drawBoundingBoxes(sourceImage, result.getData()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public FaceDetectorPool getFaceDetectorPool() {
        return faceDetectorPool;
    }

    public FaceLandmarkerPool getFaceLandmarkerPool() {
        return faceLandmarkerPool;
    }

    @Override
    public void close() throws Exception {
        try {
            if (faceDetectorPool != null) {
                faceDetectorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (faceLandmarkerPool != null) {
                faceLandmarkerPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
    }

}

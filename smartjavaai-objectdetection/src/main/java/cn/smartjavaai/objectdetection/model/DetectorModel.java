package cn.smartjavaai.objectdetection.model;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.ModelPredictorPoolManager;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.objectdetection.DetectorConfig;
import cn.smartjavaai.objectdetection.DetectorModelConfig;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.objectdetection.utils.DetectorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;

/**
 * 目标检测模型
 * @author dwj
 * @date 2025/4/4
 */
@Slf4j
public class DetectorModel implements AutoCloseable{

    private ZooModel<Image, DetectedObjects> model;

    //private Predictor<Image, DetectedObjects> predictor;

    private static final String DJL_MODEL_PREFIX = "djl://";

    private ObjectPool<Predictor<Image, DetectedObjects>> predictorPool;

    public void loadModel(DetectorModelConfig config){
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .setTypes(Image.class, DetectedObjects.class)
                        .optArgument("threshold", config.getThreshold() > 0 ? config.getThreshold() : DetectorConfig.DEFAULT_THRESHOLD)
                        .optModelUrls(DJL_MODEL_PREFIX + config.getModelEnum().getModelUri())
                        .optDevice(device)
                        .optProgress(new ProgressBar())
                        .build();
        try {
            model = criteria.loadModel();
            // 创建池子：每个线程独享 Predictor
            this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(model));
            log.info("当前设备: " + model.getNDManager().getDevice());
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new DetectionException("模型加载失败", e);
        }
    }

    /**
     * 目标检测
     * @param imagePath
     * @return
     * @throws Exception
     */
    public DetectionResponse detect(String imagePath){
        if(!FileUtils.isFileExists(imagePath)){
            throw new DetectionException("图像文件不存在");
        }
        Image image = null;
        try {
            image = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
        } catch (IOException e) {
            throw new DetectionException("图片转换错误", e);
        }
        DetectedObjects detectedObjects = detect(image);
        return DetectorUtils.convertToDetectionResponse(detectedObjects, image);
    }


    /**
     * 目标检测-将检测结果绘制到原图
     * @param imagePath
     * @return
     */
    public void detectAndDraw(String imagePath, String outputPath){
        if(!FileUtils.isFileExists(imagePath)){
            throw new DetectionException("图像文件不存在");
        }
        try {
            Image img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            DetectedObjects detectedObjects = detect(img);
            img.drawBoundingBoxes(detectedObjects);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 调用 save 方法将 Image 写入字节流
            img.save(new FileOutputStream(Paths.get(outputPath).toAbsolutePath().toString()), "png");
        } catch (IOException e) {
            throw new DetectionException(e);
        }
    }

    /**
     * 目标检测
     * @param imageData
     * @return
     */
    public DetectionResponse detect(byte[] imageData){
        if(Objects.isNull(imageData)){
            throw new DetectionException("图像无效");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            return detect(image);
        } catch (IOException e) {
            throw new DetectionException("错误的图像", e);
        }

    }


    /**
     * 目标检测
     * @param image
     * @return
     */
    public DetectionResponse detect(BufferedImage image){
        if(!ImageUtils.isImageValid(image)){
            throw new DetectionException("图像无效");
        }
        Image img = ImageFactory.getInstance().fromImage(image);
        DetectedObjects detectedObjects = detect(img);
        return DetectorUtils.convertToDetectionResponse(detectedObjects, img);
    }

    /**
     * 目标检测-将检测结果绘制到原图
     * @param sourceImage
     * @return
     */
    public BufferedImage detectAndDraw(BufferedImage sourceImage){
        if(!ImageUtils.isImageValid(sourceImage)){
            throw new DetectionException("图像无效");
        }
        Image img = ImageFactory.getInstance().fromImage(sourceImage);
        DetectedObjects detectedObjects = detect(img);
        img.drawBoundingBoxes(detectedObjects);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 调用 save 方法将 Image 写入字节流
            img.save(outputStream, "png");
            // 将字节流转换为 BufferedImage
            byte[] imageBytes = outputStream.toByteArray();
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            throw new DetectionException("导出图片失败", e);
        }
    }

    /**
     * 目标检测
     * @param image
     * @return
     */
    private DetectedObjects detect(Image image){
        Predictor<Image, DetectedObjects> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            return predictor.predict(image);
        } catch (Exception e) {
            throw new DetectionException("目标检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    predictorPool.returnObject(predictor); //归还
                    log.info("释放资源");
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        predictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }


    /**
     * 显式释放资源（必须调用！）
     */
    @Override
    public void close() {
        if (predictorPool != null) {
            predictorPool.close();
        }
    }
}

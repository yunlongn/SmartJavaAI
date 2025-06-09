package cn.smartjavaai.ocr.model.common.detect;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.model.common.detect.translator.PPOCRV5DetTranslator;
import cn.smartjavaai.ocr.utils.OcrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PPOCRV5 检测模型
 * @author dwj
 * @date 2025/4/21
 */
@Slf4j
public class PpOCRV5DetModel implements OcrCommonDetModel {


    private ObjectPool<Predictor<Image, NDList>> detPredictorPool;

    private OcrDetModelConfig config;

    @Override
    public void loadModel(OcrDetModelConfig config){
        if(StringUtils.isBlank(config.getDetModelPath())){
            throw new OcrException("modelPath is null");
        }
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        this.config = config;
        //初始化 检测Criteria
        Criteria<Image, NDList> detCriteria =
                Criteria.builder()
                        .optEngine("OnnxRuntime")
                        .setTypes(Image.class, NDList.class)
                        .optModelPath(Paths.get(config.getDetModelPath()))
                        .optTranslator(new PPOCRV5DetTranslator(new ConcurrentHashMap<String, String>()))
                        .optDevice(device)
                        .optProgress(new ProgressBar())
                        .build();

        try{
            ZooModel detectionModel = ModelZoo.loadModel(detCriteria);
            // 创建池子：每个线程独享 Predictor
            this.detPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(detectionModel));
            log.debug("当前设备: " + detectionModel.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new OcrException("检测模型加载失败", e);
        }
    }

    @Override
    public List<OcrBox> detect(String imagePath){
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        }
        List<OcrBox> ocrBoxList = detect(img);
        ((Mat)img.getWrappedImage()).release();
        return ocrBoxList;
    }

    @Override
    public List<OcrBox> detect(Image image){
        Predictor<Image, NDList> predictor = null;
        try (NDManager manager = NDManager.newBaseManager()) {
            predictor = detPredictorPool.borrowObject();
            NDList result = predictor.predict(image);
            result.attach(manager);
            return OcrUtils.convertToOcrBox(result, image);
        } catch (Exception e) {
            throw new OcrException("OCR检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    detPredictorPool.returnObject(predictor); //归还
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

    @Override
    public void detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        try (NDManager manager = NDManager.newBaseManager()) {
            Image img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            List<OcrBox> boxList = detect(img);
            if(Objects.isNull(boxList) || boxList.isEmpty()){
                throw new OcrException("未检测到文字");
            }
            OcrUtils.drawRect((Mat)img.getWrappedImage(), boxList);
            Path output = Paths.get(outputPath);
            log.debug("Saving to {}", output.toAbsolutePath().toString());
            img.save(Files.newOutputStream(output), "png");
            ((Mat) img.getWrappedImage()).release();
        } catch (IOException e) {
            throw new OcrException(e);
        }
    }


    @Override
    public List<OcrBox> detect(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            throw new OcrException("图像无效");
        }
        Image img = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        List<OcrBox> ocrBoxList = detect(img);
        ((Mat)img.getWrappedImage()).release();
        return ocrBoxList;
    }

    @Override
    public List<OcrBox> detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new OcrException("图像无效");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            return detect(image);
        } catch (IOException e) {
            throw new OcrException("错误的图像", e);
        }
    }

    @Override
    public BufferedImage detectAndDraw(BufferedImage sourceImage) {
        if(!ImageUtils.isImageValid(sourceImage)){
            throw new OcrException("图像无效");
        }
        Image img = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(sourceImage));
        List<OcrBox> ocrBoxList = detect(img);
        if(Objects.isNull(ocrBoxList) || ocrBoxList.isEmpty()){
            throw new OcrException("未检测到文字");
        }
        OcrUtils.drawRect((Mat)img.getWrappedImage(), ocrBoxList);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 调用 save 方法将 Image 写入字节流
            img.save(outputStream, "png");
            // 将字节流转换为 BufferedImage
            byte[] imageBytes = outputStream.toByteArray();
            ((Mat) img.getWrappedImage()).release();
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            throw new OcrException("导出图片失败", e);
        }
    }
}

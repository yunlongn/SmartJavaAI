package cn.smartjavaai.ocr.model.common.direction;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.Point;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
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
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.entity.*;
import cn.smartjavaai.ocr.enums.AngleEnum;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.translator.PpWordRotateTranslator;
import cn.smartjavaai.ocr.opencv.OcrNDArrayUtils;
import cn.smartjavaai.ocr.opencv.OcrOpenCVUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * PPOCRMobileV2Model 方向分类模型
 * @author dwj
 * @date 2025/4/21
 */
@Slf4j
public class PPOCRMobileV2Model implements OcrDirectionModel {


    private ObjectPool<Predictor<Image, DirectionInfo>> predictorPool;

    private DirectionModelConfig config;

    private OcrCommonDetModel detModel;


    @Override
    public void loadModel(DirectionModelConfig config){
        if(StringUtils.isBlank(config.getModelPath())){
            throw new OcrException("modelPath is null");
        }

        this.config = config;
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        Criteria<Image, DirectionInfo> criteria =
                Criteria.builder()
                        .optEngine("OnnxRuntime")
                        .setTypes(Image.class, DirectionInfo.class)
                        .optModelPath(Paths.get(config.getModelPath()))
                        .optDevice(device)
                        .optTranslator(new PpWordRotateTranslator())
                        .optProgress(new ProgressBar())
                        .build();
        try{
            ZooModel model = ModelZoo.loadModel(criteria);
            // 创建池子：每个线程独享 Predictor
            this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(model));
            log.debug("当前设备: " + model.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new OcrException("模型加载失败", e);
        }

        //获取检测模型
        if(StringUtils.isNotBlank(config.getDetModelPath()) && Objects.nonNull(config.getDetModelEnum())){
            OcrDetModelConfig detModelConfig = new OcrDetModelConfig();
            detModelConfig.setModelEnum(config.getDetModelEnum());
            detModelConfig.setDetModelPath(config.getDetModelPath());
            detModel = OcrModelFactory.getInstance().getDetModel(detModelConfig);
        }
    }

    @Override
    public List<OcrItem> detect(String imagePath){
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        }
        List<OcrItem> ocrItemList = detect(img);
        ((Mat)img.getWrappedImage()).release();
        return ocrItemList;
    }


    @Override
    public List<OcrItem> detect(Image image){
        //检测文本
        List<OcrBox> boxeList = detModel.detect(image);
        if(Objects.isNull(boxeList) || boxeList.isEmpty()){
            throw new OcrException("未检测到文本");
        }
        Predictor<Image, DirectionInfo> predictor = null;
        List<OcrItem> ocrItemList = new ArrayList<>();
        try (NDManager manager = NDManager.newBaseManager()) {
            Mat srcMat = (Mat) image.getWrappedImage();
            predictor = predictorPool.borrowObject();
            for (OcrBox box : boxeList){
                OcrItem ocrItem = detect(box, srcMat, predictor, manager);
                ocrItemList.add(ocrItem);
            }
            return ocrItemList;
        } catch (Exception e) {
            throw new OcrException("OCR检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    predictorPool.returnObject(predictor); //归还
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
     * 基于文本框检测方向
     * @param box
     * @param srcMat
     * @param predictor
     * @param manager
     * @return
     */
    private OcrItem detect(OcrBox box, Mat srcMat, Predictor<Image, DirectionInfo> predictor, NDManager manager){
        if(Objects.isNull(box)){
            throw new OcrException("box参数为空");
        }
        try {
            //透视变换及裁剪
            Image subImg = OcrUtils.transformAndCrop(srcMat, box);
            DirectionInfo directionInfo = null;
            String angle;
            //高宽比 > 1.5 纵向
            if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
                //旋转图片90度
                subImg = OcrUtils.rotateImg(manager, subImg);
                //检测方向
                directionInfo = predictor.predict(subImg);
                if (directionInfo.getName().equalsIgnoreCase("Rotate")) {
                    angle = "270";
                } else {
                    angle = "90";
                }
            }else{ //横向
                directionInfo = predictor.predict(subImg);
                if (directionInfo.getName().equalsIgnoreCase("No Rotate")) {
                    angle = "0";
                } else {
                    angle = "180";
                }
            }
            ((Mat)subImg.getWrappedImage()).release();
            return new OcrItem(box, AngleEnum.fromValue(angle), directionInfo.getProb().floatValue());
        } catch (Exception e) {
            throw new OcrException("OCR检测错误", e);
        }
    }

    @Override
    public List<OcrItem> detect(List<OcrBox> boxList,Mat srcMat,NDManager manager){
        if(Objects.isNull(boxList) || boxList.isEmpty()){
            throw new OcrException("boxList为空");
        }
        Predictor<Image, DirectionInfo> predictor = null;
        List<OcrItem> ocrItemList = new ArrayList<>();
        try {
            predictor = predictorPool.borrowObject();
            for (OcrBox box : boxList){
                OcrItem ocrItem = detect(box, srcMat, predictor, manager);
                ocrItemList.add(ocrItem);
            }
        } catch (Exception e) {
            throw new OcrException("OCR检测错误", e);
        }
        return ocrItemList;
    }

    @Override
    public void detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        try (NDManager manager = NDManager.newBaseManager()) {
            Image img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            List<OcrItem> itemList = detect(img);
            if(Objects.isNull(itemList) || itemList.isEmpty()){
                throw new OcrException("未检测到文字");
            }
            OcrUtils.drawRectWithText((Mat) img.getWrappedImage(), itemList);
            Path output = Paths.get(outputPath);
            log.debug("Saving to {}", output.toAbsolutePath().toString());
            img.save(Files.newOutputStream(output), "png");
            ((Mat) img.getWrappedImage()).release();
        } catch (IOException e) {
            throw new OcrException(e);
        }
    }

    @Override
    public List<OcrItem> detect(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            throw new OcrException("图像无效");
        }
        Image img = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        List<OcrItem> ocrItemList = detect(img);
        ((Mat)img.getWrappedImage()).release();
        return ocrItemList;
    }

    @Override
    public List<OcrItem> detect(byte[] imageData) {
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
        List<OcrItem> ocrItemList = detect(img);
        if(Objects.isNull(ocrItemList) || ocrItemList.isEmpty()){
            throw new OcrException("未检测到文字");
        }
        OcrUtils.drawRectWithText((Mat) img.getWrappedImage(), ocrItemList);
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

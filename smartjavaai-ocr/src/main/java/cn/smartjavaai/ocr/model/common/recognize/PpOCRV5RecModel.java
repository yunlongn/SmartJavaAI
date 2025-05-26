package cn.smartjavaai.ocr.model.common.recognize;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.Point;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
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
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.entity.*;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.detect.translator.PPOCRV5DetTranslator;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.recognize.translator.PPOCRV5RecTranslator;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PPOCRV5 识别模型
 * @author dwj
 * @date 2025/4/21
 */
@Slf4j
public class PpOCRV5RecModel implements OcrCommonRecModel {

    private ObjectPool<Predictor<Image, String>> recPredictorPool;

    private OcrRecModelConfig config;

    private OcrCommonDetModel detModel;

    private OcrDirectionModel directionModel;

    @Override
    public void loadModel(OcrRecModelConfig config){
        if(StringUtils.isBlank(config.getRecModelPath())){
            throw new OcrException("recModelPath is null");
        }
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu();
        }
        this.config = config;
        //初始化 识别Criteria
        Criteria<Image, String> recCriteria =
                Criteria.builder()
                        .optEngine("OnnxRuntime")
                        .setTypes(Image.class, String.class)
                        .optModelPath(Paths.get(config.getRecModelPath()))
                        .optTranslator(new PPOCRV5RecTranslator(new ConcurrentHashMap<String, String>()))
                        .optProgress(new ProgressBar())
                        .optDevice(device)
                        .build();
        try{
            ZooModel recognitionModel = ModelZoo.loadModel(recCriteria);
            this.recPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(recognitionModel));
            log.info("当前设备: " + recognitionModel.getNDManager().getDevice());
            log.info("当前引擎: " + Engine.getInstance().getEngineName());
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new OcrException("识别模型加载失败", e);
        }


        //获取检测模型
        if(StringUtils.isNotBlank(config.getDetModelPath()) && Objects.nonNull(config.getDetModelEnum())){
            OcrDetModelConfig detModelConfig = new OcrDetModelConfig();
            detModelConfig.setModelEnum(config.getDetModelEnum());
            detModelConfig.setDetModelPath(config.getDetModelPath());
            detModel = OcrModelFactory.getInstance().getDetModel(detModelConfig);
        }

        //获取方向检测模型
        if(StringUtils.isNotBlank(config.getDirectionModelPath()) && Objects.nonNull(config.getDirectionModelEnum())){
            DirectionModelConfig directionModelConfig = new DirectionModelConfig();
            directionModelConfig.setModelEnum(config.getDirectionModelEnum());
            directionModelConfig.setModelPath(config.getDirectionModelPath());
            directionModel = OcrModelFactory.getInstance().getDirectionModel(directionModelConfig);
        }
    }


    @Override
    public OcrInfo recognize(String imagePath) {
        if(StringUtils.isBlank(config.getRecModelPath())){
            throw new OcrException("recModelPath为空，无法识别");
        }
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        }
        OcrInfo ocrInfo = recognize(img);
        ((Mat)img.getWrappedImage()).release();
        return ocrInfo;
    }

    private OcrInfo recognize(Image image) {
        //检测文本
        List<OcrBox> boxeList = detModel.detect(image);
        if(Objects.isNull(boxeList) || boxeList.isEmpty()){
            throw new OcrException("未检测到文本");
        }
        Predictor<Image, String> predictor = null;
        List<RotatedBox> rotatedBoxes = new ArrayList<>();
        List<OcrItem> ocrItemList = new ArrayList<>();
        try (NDManager manager = NDManager.newBaseManager()) {
            Mat srcMat = (Mat) image.getWrappedImage();
            predictor = recPredictorPool.borrowObject();
            //检测方向
            if(directionModel != null){
                ocrItemList = directionModel.detect(boxeList, srcMat, manager);
                if(Objects.isNull(ocrItemList) || ocrItemList.isEmpty()){
                    throw new OcrException("方向检测失败");
                }
                for (OcrItem ocrItem : ocrItemList){
                    //放射变换+裁剪
                    Image subImage = OcrUtils.transformAndCrop(srcMat, ocrItem.getOcrBox());
                    //纠正文本框
                    subImage = OcrUtils.rotateImg(subImage, ocrItem.getAngle());
                    //识别
                    String name = predictor.predict(subImage);
                    ocrItem.setText(name);
                    NDArray ndArray = manager.create(ocrItem.getOcrBox().toFloatArray());
                    rotatedBoxes.add(new RotatedBox(ndArray, ocrItem.getText()));
                    ((Mat)subImage.getWrappedImage()).release();
                }
            }else{
                for (OcrBox box : boxeList){
                    RotatedBox rotatedBox = recognize(box, srcMat, predictor, manager);
                    rotatedBoxes.add(rotatedBox);
                }
            }
            //后处理
            return postProcessOcrResult(rotatedBoxes);
        } catch (Exception e) {
            throw new OcrException("OCR检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    recPredictorPool.returnObject(predictor); //归还
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



    private RotatedBox recognize(OcrBox box,Mat srcMat,Predictor<Image, String> recPredictor,NDManager manager){
        try {
            //透视变换 + 裁剪
            Image subImg = OcrUtils.transformAndCrop(srcMat, box);
            //ImageUtils.saveImage(subImg, i + "crop.png", "build/output");
            //高宽比 > 1.5
            if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
                //旋转图片90度
                subImg = OcrUtils.rotateImg(manager, subImg);
                //ImageUtils.saveImage(subImg, i + "rotate.png", "build/output");
            }
            String name = recPredictor.predict(subImg);
            ((Mat)subImg.getWrappedImage()).release();
            NDArray pointsArray = manager.create(box.toFloatArray());
            return new RotatedBox(pointsArray, name);
        } catch (Exception e) {
            throw new OcrException("OCR检测错误", e);
        }
    }


    /**
     * 后处理：排序,分行
     * @param rotatedBoxes
     */
    private OcrInfo postProcessOcrResult(List<RotatedBox> rotatedBoxes){
        //Y坐标升序排序
        List<RotatedBox> initList = new ArrayList<>();
        for (RotatedBox result : rotatedBoxes) {
            initList.add(result);
        }
        Collections.sort(initList);
        //多行文本框的集合
        List<ArrayList<RotatedBoxCompX>> lines = new ArrayList<>();
        List<RotatedBoxCompX> line = new ArrayList<>();
        RotatedBoxCompX firstBox = new RotatedBoxCompX(initList.get(0).getBox(), initList.get(0).getText());
        line.add(firstBox);
        lines.add((ArrayList) line);
        //分行判断
        for (int i = 1; i < initList.size(); i++) {
            RotatedBoxCompX tmpBox = new RotatedBoxCompX(initList.get(i).getBox(), initList.get(i).getText());
            float y1 = firstBox.getBox().toFloatArray()[1];
            float y2 = tmpBox.getBox().toFloatArray()[1];
            float dis = Math.abs(y2 - y1);
            if (dis < 20) { // 认为是同 1 行  - Considered to be in the same line
                line.add(tmpBox);
            } else { // 换行 - Line break
                firstBox = tmpBox;
                Collections.sort(line);
                line = new ArrayList<>();
                line.add(firstBox);
                lines.add((ArrayList) line);
            }
        }
        return OcrUtils.convertToOcrInfo(lines);
    }


    @Override
    public void recognizeAndDraw(String imagePath, String outputPath, int fontSize) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        try {
            Image img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            OcrInfo ocrInfo = recognize(img);
            if(Objects.isNull(ocrInfo) || Objects.isNull(ocrInfo.getLineList()) || ocrInfo.getLineList().isEmpty()){
                throw new OcrException("未检测到文字");
            }
            Mat wrappedImage = (Mat) img.getWrappedImage();
            BufferedImage bufferedImage = OcrOpenCVUtils.mat2Image(wrappedImage);
            OcrUtils.drawRectWithText(bufferedImage, ocrInfo, fontSize);
            ImageUtils.saveImage(bufferedImage, outputPath);
            wrappedImage.release();
        } catch (IOException e) {
            throw new OcrException(e);
        }
    }

    @Override
    public OcrInfo recognize(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            throw new OcrException("图像无效");
        }
        Image img = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
        OcrInfo ocrInfo = recognize(img);
        ((Mat)img.getWrappedImage()).release();
        return ocrInfo;
    }

    @Override
    public OcrInfo recognize(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new OcrException("图像无效");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            return recognize(image);
        } catch (IOException e) {
            throw new OcrException("错误的图像", e);
        }
    }

    @Override
    public BufferedImage recognizeAndDraw(BufferedImage sourceImage, int fontSize) {
        if(!ImageUtils.isImageValid(sourceImage)){
            throw new OcrException("图像无效");
        }
        Image img = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(sourceImage));
        OcrInfo ocrInfo = recognize(img);
        if(Objects.isNull(ocrInfo) || Objects.isNull(ocrInfo.getLineList()) || ocrInfo.getLineList().isEmpty()){
            throw new OcrException("未检测到文字");
        }
        try {
            OcrUtils.drawRectWithText(sourceImage, ocrInfo, fontSize);
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

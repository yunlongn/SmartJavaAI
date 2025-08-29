package cn.smartjavaai.ocr.model.common.direction;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
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
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.entity.*;
import cn.smartjavaai.ocr.enums.AngleEnum;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.criteria.DirectionCriteriaFactory;
import cn.smartjavaai.ocr.model.common.direction.translator.PpWordRotateTranslator;
import cn.smartjavaai.ocr.utils.OcrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
 * PPOCRMobileV2Model 方向分类模型
 * @author dwj
 * @date 2025/4/21
 */
@Slf4j
public class PPOCRMobileV2ClsModel implements OcrDirectionModel {


    private GenericObjectPool<Predictor<Image, DirectionInfo>> predictorPool;

    private DirectionModelConfig config;

    private ZooModel<Image, DirectionInfo> model;

    private OcrCommonDetModel textDetModel;


    @Override
    public void loadModel(DirectionModelConfig config){
        if(StringUtils.isBlank(config.getModelPath())){
            throw new OcrException("modelPath is null");
        }
        this.config = config;
        this.textDetModel = config.getTextDetModel();
        ConcurrentHashMap params = new ConcurrentHashMap<String, String>();
        if(StringUtils.isNotBlank(config.getBatchifier())){
            params.put("batchifier", config.getBatchifier());
        }
        Criteria<Image, DirectionInfo> criteria = DirectionCriteriaFactory.createCriteria(config);
        try{
            model = ModelZoo.loadModel(criteria);
            // 创建池子：每个线程独享 Predictor
            this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(model));
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            predictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + model.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new OcrException("模型加载失败", e);
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
            return detect(img);
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        }finally {
            if(img != null){
                ((Mat)img.getWrappedImage()).release();
            }
        }
    }


    @Override
    public List<OcrItem> detect(Image image){
        if(Objects.isNull(textDetModel)){
            throw new OcrException("textDetModel is null");
        }
        //检测文本
        List<OcrBox> boxeList = textDetModel.detect(image);
        if(Objects.isNull(boxeList) || boxeList.isEmpty()){
            throw new OcrException("未检测到文本");
        }
        Mat srcMat = (Mat) image.getWrappedImage();
        return detect(boxeList, srcMat);
    }


//    /**
//     * 基于文本框检测方向
//     * @param box
//     * @param srcMat
//     * @param predictor
//     * @param manager
//     * @return
//     */
//    private OcrItem detect(OcrBox box, Mat srcMat, Predictor<Image, DirectionInfo> predictor, NDManager manager){
//        if(Objects.isNull(box)){
//            throw new OcrException("box参数为空");
//        }
//        try {
//            //透视变换及裁剪
//            Image subImg = OcrUtils.transformAndCrop(srcMat, box);
//            DirectionInfo directionInfo = null;
//            String angle;
//            //高宽比 > 1.5 纵向
//            if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
//                //旋转图片90度
//                subImg = OcrUtils.rotateImg(manager, subImg);
//                //检测方向
//                directionInfo = predictor.predict(subImg);
//                if (directionInfo.getName().equalsIgnoreCase("Rotate")) {
//                    angle = "270";
//                } else {
//                    angle = "90";
//                }
//            }else{ //横向
//                directionInfo = predictor.predict(subImg);
//                if (directionInfo.getName().equalsIgnoreCase("No Rotate")) {
//                    angle = "0";
//                } else {
//                    angle = "180";
//                }
//            }
//            ((Mat)subImg.getWrappedImage()).release();
//            return new OcrItem(box, AngleEnum.fromValue(angle), directionInfo.getProb().floatValue());
//        } catch (Exception e) {
//            throw new OcrException("OCR检测错误", e);
//        }
//    }

    @Override
    public List<OcrItem> detect(List<OcrBox> boxList,Mat srcMat){
        if(Objects.isNull(boxList) || boxList.isEmpty()){
            throw new OcrException("boxList为空");
        }
        List<List<OcrItem>> ocrItemList = batchDetect(Collections.singletonList(boxList), Collections.singletonList(srcMat));
        if(Objects.isNull(ocrItemList) || ocrItemList.isEmpty()){
            throw new OcrException("方向检测失败");
        }
        return ocrItemList.get(0);
    }

    @Override
    public void detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            List<OcrItem> itemList = detect(img);
            if(Objects.isNull(itemList) || itemList.isEmpty()){
                throw new OcrException("未检测到文字");
            }
            OcrUtils.drawRectWithText((Mat) img.getWrappedImage(), itemList);
            Path output = Paths.get(outputPath);
            log.debug("Saving to {}", output.toAbsolutePath().toString());
            img.save(Files.newOutputStream(output), "png");
        } catch (IOException e) {
            throw new OcrException(e);
        } finally {
            if (img != null){
                ((Mat)img.getWrappedImage()).release();
            }
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
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            throw new OcrException("导出图片失败", e);
        } finally {
            if (img != null){
                ((Mat) img.getWrappedImage()).release();
            }
        }
    }

    @Override
    public List<List<OcrItem>> batchDetect(List<List<OcrBox>> boxList, List<Mat> srcMatList) {
        if(CollectionUtils.isEmpty(boxList)){
            throw new OcrException("boxList 不能为空");
        }
        if(CollectionUtils.isEmpty(srcMatList)){
            throw new OcrException("srcMatList 不能为空");
        }
        //检查参数
        for (int i = 0; i < srcMatList.size(); i++) {
            List<OcrBox> ocrBoxes = boxList.get(i);
            Mat mat = srcMatList.get(i);
            if (ocrBoxes == null) {
                throw new OcrException("第 " + i + " 个 boxList 为 null");
            }
            if (ocrBoxes.isEmpty()) {
                throw new OcrException("第 " + i + " 个 boxList 没有检测结果");
            }
            if (mat.empty()) {
                throw new OcrException("第 " + i + " 张图片为空 Mat");
            }
        }
        List<Image> imageList = new ArrayList<Image>();
        List<Boolean> isRotatedList = new ArrayList<Boolean>();
        int index = 0;
        try (NDManager manager = model.getNDManager().newSubManager()){
            for(int i = 0; i < srcMatList.size(); i++){
                for (int j = 0; j < boxList.get(i).size(); j++){
                    //透视变换及裁剪
                    Image subImg = OcrUtils.transformAndCrop(srcMatList.get(i), boxList.get(i).get(j));
                    //高宽比 > 1.5 纵向
                    if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
                        //旋转图片90度
                        subImg = OcrUtils.rotateImg(manager, subImg);
                        isRotatedList.add(true);
                        imageList.add(subImg);
                    }else{
                        isRotatedList.add(false);
                        imageList.add(subImg);
                    }
                    index++;
                }
            }
            List<List<OcrItem>> result = new ArrayList<>();
            List<DirectionInfo> directionInfos = batchDetect(imageList);
            if(CollectionUtils.isEmpty(directionInfos)){
                throw new OcrException("方向检测失败");
            }
            index = 0;
            for(int i = 0; i < srcMatList.size(); i++){
                List<OcrItem> ocrItemList = new ArrayList<>();
                for (int j = 0; j < boxList.get(i).size(); j++){
                    DirectionInfo directionInfo = directionInfos.get(index);
                    if(Objects.isNull(directionInfo)){
                        throw new OcrException("方向检测失败: 第" + i + "张图片, 第" + j + "个文本块，未检测到方向");
                    }
                    String angle;
                    if(isRotatedList.get(index)){
                        if (directionInfo.getName().equalsIgnoreCase("Rotate")) {
                            angle = "270";
                        } else {
                            angle = "90";
                        }
                    }else{
                        if (directionInfo.getName().equalsIgnoreCase("No Rotate")) {
                            angle = "0";
                        } else {
                            angle = "180";
                        }
                    }
                    OcrItem ocrItem = new OcrItem(boxList.get(i).get(j), AngleEnum.fromValue(angle), directionInfo.getProb().floatValue());
                    ocrItemList.add(ocrItem);
                    index++;
                }
                result.add(ocrItemList);
            }
            return result;
        }
    }

    private List<DirectionInfo> batchDetect(List<Image> imageList) {
        Predictor<Image, DirectionInfo> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            return predictor.batchPredict(imageList);
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

    @Override
    public void setTextDetModel(OcrCommonDetModel detModel) {
        this.textDetModel = detModel;
    }

    @Override
    public OcrCommonDetModel getTextDetModel() {
        return textDetModel;
    }

    @Override
    public GenericObjectPool<Predictor<Image, DirectionInfo>> getPool() {
        return predictorPool;
    }

    @Override
    public void close() throws Exception {
        try {
            if (predictorPool != null) {
                predictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (model != null) {
                model.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
    }
}

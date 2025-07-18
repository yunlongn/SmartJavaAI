package cn.smartjavaai.ocr.model.common.detect;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.model.common.detect.criteria.OcrCommonDetCriterialFactory;
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
import java.util.*;

/**
 * ocr通用检测模型实现类
 * @author dwj
 */
@Slf4j
public class OcrCommonDetModelImpl implements OcrCommonDetModel{

    private ObjectPool<Predictor<Image, NDList>> detPredictorPool;

    private ZooModel<Image, NDList> detectionModel;

    private OcrDetModelConfig config;

    @Override
    public void loadModel(OcrDetModelConfig config){
        if(StringUtils.isBlank(config.getDetModelPath())){
            throw new OcrException("modelPath is null");
        }
        this.config = config;
        //初始化 检测Criteria
        Criteria<Image, NDList> detCriteria = OcrCommonDetCriterialFactory.createCriteria(config);
        try{
            detectionModel = ModelZoo.loadModel(detCriteria);
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
        List<Image> imageList = Collections.singletonList(image);
        List<List<OcrBox>> result = batchDetectDJLImage(imageList);
        return result.get(0);
    }

    @Override
    public void detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        try {
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
    public List<List<OcrBox>> batchDetect(List<BufferedImage> imageList) {
        List<Image> djlImageList = new ArrayList<>(imageList.size());
        try {
            for (BufferedImage bufferedImage : imageList) {
                djlImageList.add(ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(bufferedImage)));
            }
            return batchDetectDJLImage(djlImageList);
        } catch (Exception e) {
            throw new OcrException(e);
        } finally {
            djlImageList.forEach(image -> ((Mat)image.getWrappedImage()).release());
        }
    }

    @Override
    public List<List<OcrBox>> batchDetectDJLImage(List<Image> imageList) {
        if(!ImageUtils.isAllImageSizeEqual(imageList)){
            throw new OcrException("图片尺寸不一致");
        }
        Predictor<Image, NDList> predictor = null;
        try (NDManager manager = NDManager.newBaseManager()) {
            predictor = detPredictorPool.borrowObject();
            List<NDList> result = predictor.batchPredict(imageList);
            result.forEach(ndList -> ndList.attach(manager));
            return OcrUtils.convertToOcrBox(result);
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
    public void close() throws Exception {
        try {
            if (detPredictorPool != null) {
                detPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (detectionModel != null) {
                detectionModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
    }


}

package cn.smartjavaai.ocr.ppv4.model;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.ocr.detection.OcrDetModelConfig;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.detection.OcrDetModel;
import cn.smartjavaai.ocr.ppv4.translator.PaddleOCRV4DetectTranslator;
import cn.smartjavaai.ocr.utils.ImageUtils;
import cn.smartjavaai.ocr.utils.OcrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dwj
 * @date 2025/4/21
 */
@Slf4j
public class PaddleOCRV4DetModel implements OcrDetModel {

    private ZooModel detectionModel;

    private ObjectPool<Predictor<Image, NDList>> predictorPool;

    @Override
    public void loadModel(OcrDetModelConfig config){
        if(StringUtils.isBlank(config.getModelPath())){
            throw new OcrException("modelPath is null");
        }
        Criteria<Image, NDList> criteria =
                Criteria.builder()
                        .optEngine("OnnxRuntime")
                        .setTypes(Image.class, NDList.class)
                        .optModelPath(Paths.get(config.getModelPath()))
                        .optTranslator(new PaddleOCRV4DetectTranslator(new ConcurrentHashMap<String, String>()))
                        .optProgress(new ProgressBar())
                        .build();
        try{
            detectionModel = ModelZoo.loadModel(criteria);
            // 创建池子：每个线程独享 Predictor
            this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(detectionModel));
            log.info("当前设备: " + detectionModel.getNDManager().getDevice());
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new OcrException("模型加载失败", e);
        }
    }

    @Override
    public DetectionResponse detect(String imagePath){
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        }
        return detect(img);
    }

    private DetectionResponse detect(Image image){
        Predictor<Image, NDList> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            NDList result = predictor.predict(image);
            return OcrUtils.convertToDetectionResponse(result, image);
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
    public void detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new OcrException("图像文件不存在");
        }
        try {
            Image img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            DetectionResponse detectionResponse = detect(img);
            if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getDetectionInfoList()) || detectionResponse.getDetectionInfoList().isEmpty()){
                throw new OcrException("未识别到文字");
            }
            ImageUtils.drawRect((Mat)img.getWrappedImage(), detectionResponse);
            Path output = Paths.get(outputPath);
            log.info("Saving to {}", output.toAbsolutePath().toString());
            img.save(Files.newOutputStream(output), "png");
        } catch (IOException e) {
            throw new OcrException(e);
        }
    }
}

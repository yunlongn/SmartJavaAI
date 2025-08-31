package cn.smartjavaai.face.model.facedect;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.NoopTranslator;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.*;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.facedect.criterial.FaceDetCriteriaFactory;
import cn.smartjavaai.face.model.facedect.mtcnn.*;
import cn.smartjavaai.face.utils.FaceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * MTCNN 人脸检测模型实现
 * @author dwj
 */
@Slf4j
public class MtcnnFaceDetModel implements FaceDetModel{


    public ZooModel<NDList, NDList> pNetModel;

    public ZooModel<NDList, NDList> rNetModel;

    public ZooModel<NDList, NDList> oNetModel;
    private GenericObjectPool<Predictor<NDList, NDList>> pnetPredictorPool;
    private GenericObjectPool<Predictor<NDList, NDList>> rnetPredictorPool;
    private GenericObjectPool<Predictor<NDList, NDList>> onetPredictorPool;


    /**
     * 加载模型
     * @param config
     */
    @Override
    public void loadModel(FaceDetConfig config){
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
        Path modelPath = Paths.get(config.getModelPath());
        if(!Files.isDirectory(modelPath)){
            throw new FaceException("MTCNN 模型需要指定存放模型文件的目录路径");
        }
        try {
            Path pnetPath = modelPath.resolve("pnet_script.pt");
            Path rnetPath = modelPath.resolve("rnet_script.pt");
            Path onetPath = modelPath.resolve("onet_script.pt");
            pNetModel = getModel(pnetPath);
            rNetModel = getModel(rnetPath);
            oNetModel = getModel(onetPath);

            this.pnetPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(pNetModel));
            this.rnetPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(rNetModel));
            this.onetPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(oNetModel));
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            pnetPredictorPool.setMaxTotal(predictorPoolSize);
            rnetPredictorPool.setMaxTotal(predictorPoolSize);
            onetPredictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + pNetModel.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new FaceException("mtcnn人脸检测模型加载失败", e);
        }
    }

    /**
     * 加载模型
     * @param modelPath
     * @throws ModelNotFoundException
     * @throws MalformedModelException
     * @throws IOException
     */
    public ZooModel<NDList, NDList> getModel(Path modelPath) throws ModelNotFoundException, MalformedModelException, IOException {
        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optTranslator(new NoopTranslator())
                        .optEngine("PyTorch")
                        .optModelPath(modelPath)
                        .optProgress(new ProgressBar())
                        .build();
        return criteria.loadModel();
    }



    /**
     * 检测人脸
     * @param imagePath 图片路径
     * @return
     * @throws Exception
     */
    @Override
    public R<DetectionResponse> detect(String imagePath){
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            return detect(img);
        } catch (IOException e) {
            throw new FaceException("无效的图片", e);
        } finally {
            if (img != null) {
                ((Mat)img.getWrappedImage()).release();
            }
        }

    }

    /**
     * 检测人脸
     * @param imageInputStream 图片流
     * @return
     * @throws Exception
     */
    @Override
    public R<DetectionResponse> detect(InputStream imageInputStream){
        if(Objects.isNull(imageInputStream)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromInputStream(imageInputStream);
            return detect(img);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        } finally {
            if (img != null) {
                ((Mat)img.getWrappedImage()).release();
            }
        }
    }

    @Override
    public R<DetectionResponse> detect(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromImage(OpenCVUtils.image2Mat(image));
            return detect(img);
        } catch (Exception e) {
            throw new FaceException(e);
        } finally {
            if (img != null) {
                ((Mat)img.getWrappedImage()).release();
            }
        }

    }

    @Override
    public R<DetectionResponse> detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        return detect(new ByteArrayInputStream(imageData));
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
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            R<DetectionResponse> detectionResponseR = detect(img);
            if(!detectionResponseR.isSuccess()){
                return R.fail(detectionResponseR.getCode(), detectionResponseR.getMessage());
            }
            if(Objects.isNull(detectionResponseR.getData()) ||
                    CollectionUtils.isEmpty(detectionResponseR.getData().getDetectionInfoList())){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            BufferedImage sourceImage = OpenCVUtils.mat2Image((Mat)img.getWrappedImage());
            FaceUtils.drawBoundingBoxes(sourceImage, detectionResponseR.getData(), outputPath);
            return R.ok();
        } catch (IOException e) {
            throw new FaceException(e);
        } finally {
            if (img != null){
                ((Mat)img.getWrappedImage()).release();
            }
        }
    }

    @Override
    public R<BufferedImage> detectAndDraw(BufferedImage sourceImage) {
        if(!ImageUtils.isImageValid(sourceImage)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            R<DetectionResponse> detectionResponseR = detect(sourceImage);
            if(!detectionResponseR.isSuccess()){
                return R.fail(detectionResponseR.getCode(), detectionResponseR.getMessage());
            }
            if(Objects.isNull(detectionResponseR.getData()) ||
                    CollectionUtils.isEmpty(detectionResponseR.getData().getDetectionInfoList())){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            return R.ok(FaceUtils.drawBoundingBoxes(sourceImage, detectionResponseR.getData()));
        } catch (IOException e) {
            throw new FaceException("导出图片失败", e);
        }
    }

    /**
     * 人脸检测
     * @param image
     * @return
     */
    public R<DetectionResponse> detect(Image image){
        Predictor<NDList, NDList> pNetPredictor = null;
        Predictor<NDList, NDList> rNetPredictor = null;
        Predictor<NDList, NDList> oNetPredictor = null;
        try (NDManager manager = pNetModel.getNDManager().newSubManager();){
            pNetPredictor = pnetPredictorPool.borrowObject();
            rNetPredictor = rnetPredictorPool.borrowObject();
            oNetPredictor = onetPredictorPool.borrowObject();
            int h = image.getHeight();
            int w = image.getWidth();
            //第一阶段
            NDList outputPnet = PNetModel.firstStage(manager, pNetPredictor, image);
            if(CollectionUtils.isEmpty(outputPnet)){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            NDArray boxes = outputPnet.get(0);
            NDArray image_inds = outputPnet.get(1);
            NDArray imgs = outputPnet.get(2);
            if(DJLCommonUtils.isNDArrayEmpty(boxes) || DJLCommonUtils.isNDArrayEmpty(image_inds) || DJLCommonUtils.isNDArrayEmpty(imgs)){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            NDList pad = MtcnnUtils.pad(boxes, w, h);
            //第二阶段
            NDList outputRnet = RNetModel.secondStage(manager, rNetPredictor, imgs,boxes,pad, image_inds);
            if(CollectionUtils.isEmpty(outputRnet)){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            NDArray image_indsFiltered = outputRnet.get(0);
            NDArray scoresFiltered = outputRnet.get(1);
            boxes = outputRnet.get(2);
            if(DJLCommonUtils.isNDArrayEmpty(boxes) || DJLCommonUtils.isNDArrayEmpty(image_indsFiltered) || DJLCommonUtils.isNDArrayEmpty(scoresFiltered)){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            //第三阶段
            MtcnnBatchResult oNetResult = ONetModel.thirdStage(manager, oNetPredictor, imgs,boxes, w, h, scoresFiltered, image_indsFiltered);
            DetectionResponse detectionResponse = convertToDetectionResponse(oNetResult);
            if(Objects.isNull(detectionResponse)){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            return R.ok(detectionResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (pNetPredictor != null) {
                try {
                    pnetPredictorPool.returnObject(pNetPredictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        pNetPredictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
            if (rNetPredictor != null) {
                try {
                    rnetPredictorPool.returnObject(rNetPredictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        rNetPredictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
            if (oNetPredictor != null) {
                try {
                    onetPredictorPool.returnObject(oNetPredictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        oNetPredictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }




    /**
     * 转换为FaceDetectedResult
     * @param mtcnnBatchResult
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(MtcnnBatchResult mtcnnBatchResult){
        if(Objects.isNull(mtcnnBatchResult) || CollectionUtils.isEmpty(mtcnnBatchResult.boxes)
                || CollectionUtils.isEmpty(mtcnnBatchResult.points)
                || CollectionUtils.isEmpty(mtcnnBatchResult.probs)){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();

        NDArray boxes = mtcnnBatchResult.boxes.get(0);
        NDArray probs = mtcnnBatchResult.probs.get(0);
        NDArray points = mtcnnBatchResult.points.get(0);

        if (DJLCommonUtils.isNDArrayEmpty(boxes) || DJLCommonUtils.isNDArrayEmpty(probs) || DJLCommonUtils.isNDArrayEmpty(points)){
            return null;
        }
        long numBoxes = boxes.getShape().get(0);
        for (int i = 0; i < numBoxes; i++) {
            float[] boxCoords = boxes.get(i).toFloatArray(); // [x1, y1, x2, y2]
            float score = probs.getFloat(i);
            NDArray pointND = points.get(i); // shape [5,2]
            float[] flatPoints = pointND.toFloatArray(); // 一维长度 10
            List<Point> keyPoints = new ArrayList<Point>();
            for (int p = 0; p < 5; p++) {
                keyPoints.add(new Point(flatPoints[p * 2], flatPoints[p * 2 + 1]));
            }
            int x = Math.round(boxCoords[0]);
            int y = Math.round(boxCoords[1]);
            int w = Math.round(boxCoords[2] - boxCoords[0]);
            int h = Math.round(boxCoords[3] - boxCoords[1]);

            DetectionRectangle rectangle = new DetectionRectangle(x, y, w, h);
            FaceInfo faceInfo = new FaceInfo(keyPoints);
            DetectionInfo detectionInfo = new DetectionInfo(rectangle, score, faceInfo);
            detectionInfoList.add(detectionInfo);
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }



    public GenericObjectPool<Predictor<NDList, NDList>> getPnetPredictorPool() {
        return pnetPredictorPool;
    }

    public GenericObjectPool<Predictor<NDList, NDList>> getRnetPredictorPool() {
        return rnetPredictorPool;
    }

    public GenericObjectPool<Predictor<NDList, NDList>> getOnetPredictorPool() {
        return onetPredictorPool;
    }

    @Override
    public void close() {
        try {
            if (pnetPredictorPool != null) {
                pnetPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (rnetPredictorPool != null) {
                rnetPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (onetPredictorPool != null) {
                onetPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (pNetModel != null) {
                pNetModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (pNetModel != null) {
                pNetModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (rNetModel != null) {
                rNetModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (oNetModel != null) {
                oNetModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
    }
}

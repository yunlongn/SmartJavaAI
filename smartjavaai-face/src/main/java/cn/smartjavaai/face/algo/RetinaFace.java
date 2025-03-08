package cn.smartjavaai.face.algo;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.ImageFeatureExtractorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.Rectangle;
import cn.smartjavaai.face.*;
import org.apache.commons.lang3.StringUtils;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * RetinaFace实现
 * @author dwj
 */
public class RetinaFace extends AbstractFaceAlgorithm {

    private Criteria<Image, DetectedObjects> criteria;

    private Criteria<Image, float[]> faceFeatureCriteria;

    private Predictor<Image, DetectedObjects> predictor;

    private ZooModel<Image, DetectedObjects> model;

    /**
     * 特征图层的基础缩放比例
     */
    public static final int[][] scales = {{16, 32}, {64, 128}, {256, 512}};
    /**
     * 特征图相对于原图的采样步长
     */
    public static final int[] steps = {8, 16, 32};
    /**
     * 缩放系数
     */
    public static final double[] variance = {0.1f, 0.2f};


    /**
     * 加载模型
     * @param config
     */
    @Override
    public void loadModel(ModelConfig config) throws ModelNotFoundException, MalformedModelException, IOException {
        FaceDetectionTranslator translator =
                new FaceDetectionTranslator(config.getConfidenceThreshold(), config.getNmsThresh(), variance, config.getMaxFaceCount(), scales, steps);
        criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null : "https://resources.djl.ai/test-models/pytorch/retinaface.zip")
                        // Load model from local file, e.g:
                        .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                        .optModelName(StringUtils.isNotBlank(config.getAlgorithmName()) ? config.getAlgorithmName() : "retinaface") // specify model file prefix
                        .optTranslator(translator)
                        .optProgress(new ProgressBar())
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();
        model = criteria.loadModel();
        predictor = model.newPredictor();
    }



    /**
     * 检测人脸
     * @param imagePath 图片路径
     * @return
     * @throws Exception
     */
    @Override
    public FaceDetectedResult detect(String imagePath) throws Exception{
        Path facePath = Paths.get(imagePath);
        Image img = ImageFactory.getInstance().fromFile(facePath);
        DetectedObjects detection = predictor.predict(img);
        return convertToFaceDetectedResult(detection,img);
    }

    /**
     * 检测人脸
     * @param imageInputStream 图片流
     * @return
     * @throws Exception
     */
    @Override
    public FaceDetectedResult detect(InputStream imageInputStream) throws Exception {
        Image img = ImageFactory.getInstance().fromInputStream(imageInputStream);
        DetectedObjects detection = predictor.predict(img);
        return convertToFaceDetectedResult(detection,img);
    }

    /**
     * 转换为FaceDetectedResult
     * @param detection
     * @param img
     * @return
     */
    private FaceDetectedResult convertToFaceDetectedResult(DetectedObjects detection, Image img){
        FaceDetectedResult faceDetectedResult = new FaceDetectedResult();
        List<Double> probabilities = new ArrayList<>(detection.getProbabilities());
        List<DetectedObjects.DetectedObject> detectedObjectList = detection.items();
        List<Rectangle> RectangleList = detectedObjectList.parallelStream()
                .map(obj -> {
                    Rectangle rectangle = new Rectangle();
                    List<Point> pointList = new ArrayList<>();
                    ai.djl.modality.cv.output.Rectangle rectangleDjl = obj.getBoundingBox().getBounds();
                    int x = (int)(rectangleDjl.getX() * (double)img.getWidth());
                    int y = (int)(rectangleDjl.getY() * (double)img.getHeight());
                    int width = (int)(rectangleDjl.getWidth() * (double)img.getWidth());
                    int height = (int)(rectangleDjl.getHeight() * (double)img.getHeight());
                    pointList.add(new Point(x,y));
                    pointList.add(new Point(x + width,y));
                    pointList.add(new Point(x,y + height));
                    pointList.add(new Point(x + width,y + height));
                    rectangle.setPointList(pointList);
                    rectangle.setHeight(height);
                    rectangle.setWidth(width);
                    return rectangle;
                })
                .collect(Collectors.toList());
        faceDetectedResult.setProbabilities(probabilities);
        faceDetectedResult.setRectangles(RectangleList);
        return faceDetectedResult;
    }
}

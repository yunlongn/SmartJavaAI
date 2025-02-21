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
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.compress.utils.Lists;

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
    public void loadModel(ModelConfig config) {
        FaceDetectionTranslator translator =
                new FaceDetectionTranslator(config.getConfidenceThreshold(), config.getNmsThresh(), variance, config.getMaxFaceCount(), scales, steps);
        criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelUrls("https://resources.djl.ai/test-models/pytorch/retinaface.zip")
                        //.optModelPath(modelPath)
                        // Load model from local file, e.g:
                        .optModelName("retinaface") // specify model file prefix
                        .optTranslator(translator)
                        .optProgress(new ProgressBar())
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();
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
        try (ZooModel<Image, DetectedObjects> model = criteria.loadModel();
             Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
            DetectedObjects detection = predictor.predict(img);
            return convertToFaceDetectedResult(detection,img);
        }
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
        try (ZooModel<Image, DetectedObjects> model = criteria.loadModel();
             Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
            DetectedObjects detection = predictor.predict(img);
            return convertToFaceDetectedResult(detection,img);
            /*saveBoundingBoxImage(img, detection);
            return detection;*/
        }
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

    /**
     * 特征提取
     * @param imagePath 图片路径
     * @return
     * @throws Exception
     */
    @Override
    public float[] featureExtraction(String imagePath) throws Exception {
        Path imageFile = Paths.get(imagePath);
        Image img = ImageFactory.getInstance().fromFile(imageFile);
        img.getWrappedImage();
        List<Float> mean =
                Arrays.asList(
                        127.5f / 255.0f,
                        127.5f / 255.0f,
                        127.5f / 255.0f,
                        128.0f / 255.0f,
                        128.0f / 255.0f,
                        128.0f / 255.0f);
        String normalize = mean.stream().map(Object::toString).collect(Collectors.joining(","));

        Criteria<Image, float[]> criteria =
                Criteria.builder()
                        .setTypes(Image.class, float[].class)
                        .optModelUrls(
                                "https://resources.djl.ai/test-models/pytorch/face_feature.zip")
                        .optModelName("face_feature") // specify model file prefix
                        .optArgument("normalize", normalize)
                        .optTranslatorFactory(new ImageFeatureExtractorFactory())
                        .optProgress(new ProgressBar())
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();

        try (ZooModel<Image, float[]> model = criteria.loadModel()) {
            Predictor<Image, float[]> predictor = model.newPredictor();
            return predictor.predict(img);
        }
    }

    /**
     * 特征提取
     * @param inputStream 输入流
     * @return
     * @throws Exception
     */
    @Override
    public float[] featureExtraction(InputStream inputStream) throws Exception {
        Image img = ImageFactory.getInstance().fromInputStream(inputStream);
        img.getWrappedImage();
        List<Float> mean =
                Arrays.asList(
                        127.5f / 255.0f,
                        127.5f / 255.0f,
                        127.5f / 255.0f,
                        128.0f / 255.0f,
                        128.0f / 255.0f,
                        128.0f / 255.0f);
        String normalize = mean.stream().map(Object::toString).collect(Collectors.joining(","));

        Criteria<Image, float[]> criteria =
                Criteria.builder()
                        .setTypes(Image.class, float[].class)
                        .optModelUrls(
                                "https://resources.djl.ai/test-models/pytorch/face_feature.zip")
                        .optModelName("face_feature") // specify model file prefix
                        .optArgument("normalize", normalize)
                        .optTranslatorFactory(new ImageFeatureExtractorFactory())
                        .optProgress(new ProgressBar())
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();

        try (ZooModel<Image, float[]> model = criteria.loadModel()) {
            Predictor<Image, float[]> predictor = model.newPredictor();
            return predictor.predict(img);
        }
    }

    /**
     * 计算相似度
     * @param feature1 图1特征
     * @param feature2 图2特征
     * @return
     * @throws Exception
     */
    @Override
    public float calculSimilar(float[] feature1, float[] feature2) throws Exception {
        float ret = 0.0f;
        float mod1 = 0.0f;
        float mod2 = 0.0f;
        int length = feature1.length;
        for (int i = 0; i < length; ++i) {
            ret += feature1[i] * feature2[i];
            mod1 += feature1[i] * feature1[i];
            mod2 += feature2[i] * feature2[i];
        }
        return (float) ((ret / Math.sqrt(mod1) / Math.sqrt(mod2) + 1) / 2.0f);
    }

    /**
     * 特征比较
     * @param imagePath1 图1路径
     * @param imagePath2 图2路径
     * @return
     * @throws Exception
     */
    @Override
    public float featureComparison(String imagePath1, String imagePath2) throws Exception {
        float[] feature1 = featureExtraction(imagePath1);
        float[] feature2 = featureExtraction(imagePath2);
        return calculSimilar(feature1, feature2);
    }

    /**
     * 特征比较
     * @param inputStream1 图1输入流
     * @param inputStream2 图2输入流
     * @return
     * @throws Exception
     */
    @Override
    public float featureComparison(InputStream inputStream1, InputStream inputStream2) throws Exception {
        float[] feature1 = featureExtraction(inputStream1);
        float[] feature2 = featureExtraction(inputStream2);
        return calculSimilar(feature1, feature2);
    }

    /*@Override
    public float[] recognize(FaceRegion region) {
        return new float[0];
    }*/

    /*@Override
    public void loadModel(ModelConfig config) throws Exception {

    }*/
}

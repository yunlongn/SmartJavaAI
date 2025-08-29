package smartai.examples.objectdetection;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.*;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;
import cn.smartjavaai.objectdetection.enums.DetectorModelEnum;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.objectdetection.model.DetectorModel;
import cn.smartjavaai.objectdetection.model.ObjectDetectionModelFactory;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 目标检测模型demo
 * 支持功能：目标检测
 * 模型下载地址：https://pan.baidu.com/s/10aTOLBlR6EG-sq6g0OkAWg?pwd=1234 提取码: 1234
 * @author dwj
 */
@Slf4j
public class ObjectDetection {


    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    public static void main(String[] args) throws ModelException, TranslateException, IOException {
        Classifications classification = predict();
        log.info("{}", classification);
    }


    public static Classifications predict() throws IOException, ModelException, TranslateException {

        Config.setCachePath("/Users/wenjie/smartjavaai_cache");
        URL url = new URL("https://resources.djl.ai/images/action_dance.jpg");
        // Use DJL PyTorch model zoo model
        Criteria<URL, Classifications> criteria =
                Criteria.builder()
                        .setTypes(URL.class, Classifications.class)
                        .optModelUrls(
                                "djl://ai.djl.mxnet/action_recognition")
                        .optEngine("MXNet")
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<URL, Classifications> inception = criteria.loadModel();
             Predictor<URL, Classifications> action = inception.newPredictor()) {
            return action.predict(url);
        }
    }

    @BeforeClass
    public static void beforeAll() throws IOException {
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }



    /**
     * 使用默认模型检测：YOLO11N
     */
    @Test
    public void objectDetection(){
        //默认cpu
        try {
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel();
            DetectionResponse detectionResponse = detectorModel.detect("src/main/resources/object_detection.jpg");
            log.info("目标检测结果：{}", JSONObject.toJSONString(detectionResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定模型检测(19种模型可选)
     */
    @Test
    public void objectDetection2(){
        try {
            DetectorModelConfig config = new DetectorModelConfig();
            config.setModelEnum(DetectorModelEnum.SSD_300_RESNET50);//检测模型，目前支持19种预置模型
            config.setModelEnum(DetectorModelEnum.YOLOV12_OFFICIAL);
            config.setModelPath("yolov11s");
            // 指定允许的类别
//            config.setAllowedClasses(Arrays.asList("person"));
            //指定返回检测数量
            config.setTopK(100);
            config.setDevice(device);
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
            DetectionResponse detectionResponse = detectorModel.detect("src/main/resources/dog_bike_car.jpg");
            log.info("目标检测结果：{}", JSONObject.toJSONString(detectionResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸检测并绘制检测结果
     */
    @Test
    public void objectDetectionAndDraw(){
        try {
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel();
            detectorModel.detectAndDraw("src/main/resources/object_detection.jpg","output/object_detection_detected.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸检测并绘制检测结果,返回BufferedImage
     */
    @Test
    public void objectDetectionAndDraw2(){
        try {
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel();
            String imagePath = "src/main/resources/object_detection.jpg";
            BufferedImage image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
            //可以根据后续业务场景使用detectedImage
            BufferedImage detectedImage = detectorModel.detectAndDraw(image);
            Assert.assertNotNull("detectedImage null", detectedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /**
     * 使用yolo官方模型检测物品识别
     */
    @Test
    public void objectDetectionWithOfficialModel(){
        try {
            DetectorModelConfig config = new DetectorModelConfig();
//            config.setThreshold(0.3f);
            //也支持YoloV8：YOLOV8_OFFICIAL 模型可以从文档中提供的地址下载
            config.setModelEnum(DetectorModelEnum.YOLOV12_OFFICIAL);//检测模型，目前支持19种模型
            // 指定模型路径，需要更改为自己的模型路径
            config.setModelPath("/Users/wenjie/Documents/develop/face_model/yolo11n.torchscript");
            config.setDevice(device);
            config.putCustomParam("width", 640);//resize 宽
            config.putCustomParam("height", 640);// resize 高
            config.putCustomParam("resize", true);
            config.putCustomParam("toTensor", true);
            config.putCustomParam("applyRatio", true);
            config.putCustomParam("threshold", 0.6f);
                    // for performance optimization maxBox parameter can reduce number of
                    // considered boxes from 8400
            config.putCustomParam("maxBox", 8400);
//            config.putCustomParam("pad", 114d);
//            List<Float> mean = Arrays.asList(0.5f,0.5f,0.5f,0.5f,0.5f,0.5f);
//            String normalize = mean.stream().map(Object::toString).collect(Collectors.joining(","));
//            config.putCustomParam("normalize", normalize);
//            config.putCustomParam("flag", Image.Flag.COLOR);
//            config.putCustomParam("pad", 114);
            //一定要将yolo官方的类别文件：synset.txt（文档中下载）放在模型同目录下，否则报错
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
            DetectionResponse detect = detectorModel.detect("src/main/resources/object_detection.jpg");
            log.info("目标检测结果：{}", JSONObject.toJSONString(detect));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用自己训练的模型检测
     */
    @Test
    public void objectDetectionWithCustomModel(){
        try {
            DetectorModelConfig config = new DetectorModelConfig();
            //也支持YoloV8：YOLOV8_CUSTOM 模型需要自己训练，训练教程可以查看文档
            config.setModelEnum(DetectorModelEnum.YOLOV12_CUSTOM);//自定义YOLOV12模型
            // 指定模型路径，需要更改为自己的模型路径
            config.setModelPath("/Users/xxx/Documents/develop/fire_model/best.onnx");
            config.putCustomParam("width", 640);//resize 宽
            config.putCustomParam("height", 640);// resize 高
            config.putCustomParam("nmsThreshold", 0.5f);
            // 指定允许的类别
//            config.setAllowedClasses(Arrays.asList("person"));
            //指定返回检测数量
            config.setTopK(100);
            config.setDevice(device);
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
            DetectionResponse detect = detectorModel.detect("src/main/resources/dog_bike_car.jpg");
            log.info("目标检测结果：{}", JSONObject.toJSONString(detect));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * tensorflow目标检测
     */
    @Test
    public void objectDetection3(){
        try {
            DetectorModelConfig config = new DetectorModelConfig();
            config.setModelEnum(DetectorModelEnum.TENSORFLOW2_OFFICIAL);
            config.setModelPath("/Users/wenjie/Documents/develop/model/tensorflow/ssd_mobilenet_v2_320x320_coco17_tpu-8");
//            config.putCustomParam("synsetUrl", "https://raw.githubusercontent.com/tensorflow/models/master/research/object_detection/data/mscoco_label_map.pbtxt");
//            config.putCustomParam("synsetPath", "/Users/wenjie/Downloads/mscoco_label_map.pbtxt.txt");
            config.putCustomParam("synsetFileName", "mscoco.pbtxt");
            // 指定允许的类别
//            config.setAllowedClasses(Arrays.asList("person"));
            //指定返回检测数量
            config.setTopK(100);
            config.setDevice(device);
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
            DetectionResponse detectionResponse = detectorModel.detect("src/main/resources/dog_bike_car.jpg");
            detectorModel.detectAndDraw("src/main/resources/dog_bike_car.jpg", "output/dog_bike_car_detect.jpg");
            log.info("目标检测结果：{}", JSONObject.toJSONString(detectionResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 摄像头目标检测
     * 注意事项：如果视频比较卡，可以使用轻量的检测模型
     */
    @Test
    public void testDetectCamera(){
        try {
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel();
            OpenCV.loadShared();
            VideoCapture capture = new VideoCapture(0);
            if (!capture.isOpened()) {
                System.out.println("No camera detected");
                return;
            }

            double ratio =
                    capture.get(Videoio.CAP_PROP_FRAME_WIDTH)
                            / capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int height = (int) (screenSize.height * 0.65f);
            int width = (int) (height * ratio);
            if (width > screenSize.width) {
                width = screenSize.width;
            }

            Mat image = new Mat();
            boolean captured = false;
            for (int i = 0; i < 10; ++i) {
                captured = capture.read(image);
                if (captured) {
                    break;
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignore) {
                    // ignore
                }
            }
            if (!captured) {
                JOptionPane.showConfirmDialog(null, "Failed to capture image from WebCam.");
            }
            ViewerFrame frame = new ViewerFrame(width, height);
            ImageFactory factory = ImageFactory.getInstance();
            Size size = new Size(width, height);

            while (capture.isOpened()) {
                if (!capture.read(image)) {
                    break;
                }
                Mat resizeImage = new Mat();
                Imgproc.resize(image, resizeImage, size);
                Image img = factory.fromImage(resizeImage);
                BufferedImage bufferedImage = OpenCVUtils.mat2Image(resizeImage);
                DetectionResponse detectedResult = detectorModel.detect(bufferedImage);
                if (Objects.isNull(detectedResult) || Objects.isNull(detectedResult.getDetectionInfoList()) || detectedResult.getDetectionInfoList().size() == 0){
                    log.debug("未检测到物体");
                    continue;
                }
                for(DetectionInfo detectionInfo : detectedResult.getDetectionInfoList()){
                    DetectionRectangle detectionRectangle = detectionInfo.getDetectionRectangle();
                    String text = detectionInfo.getObjectDetInfo().getClassName();
                    ImageUtils.drawImageRectWithText(bufferedImage, detectionRectangle, text, Color.RED);
                }
                frame.showImage(bufferedImage);
            }

            capture.release();
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}

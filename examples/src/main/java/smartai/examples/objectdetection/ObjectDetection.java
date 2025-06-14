package smartai.examples.objectdetection;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.*;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;
import cn.smartjavaai.objectdetection.enums.DetectorModelEnum;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.objectdetection.model.DetectorModel;
import cn.smartjavaai.objectdetection.model.ObjectDetectionModelFactory;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 目标检测模型demo
 * 支持功能：目标检测
 * @author dwj
 * @date 2025/4/11
 */
@Slf4j
public class ObjectDetection {

    /**
     * 使用默认模型检测：YOLO11N
     */
    @Test
    public void objectDetection(){
        DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel();
        DetectionResponse detectionResponse = detectorModel.detect("src/main/resources/object_detection.jpg");
        log.info("目标检测结果：{}", JSONObject.toJSONString(detectionResponse));
    }

    /**
     * 指定模型检测(19种模型可选)
     */
    @Test
    public void objectDetection2(){
        DetectorModelConfig config = new DetectorModelConfig();
        config.setModelEnum(DetectorModelEnum.SSD_300_RESNET50);//检测模型，目前支持19种预置模型
        DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
        DetectionResponse detectionResponse = detectorModel.detect("src/main/resources/dog_bike_car.jpg");
        log.info("目标检测结果：{}", JSONObject.toJSONString(detectionResponse));
    }

    /**
     * 人脸检测并绘制检测结果
     */
    @Test
    public void objectDetectionAndDraw(){
        DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel();
        detectorModel.detectAndDraw("src/main/resources/object_detection.jpg","output/object_detection_detected.png");
    }

    /**
     * 人脸检测并绘制检测结果,返回BufferedImage
     */
    @Test
    public void objectDetectionAndDraw2(){
        try {
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel();
            BufferedImage image = null;
            String imagePath = "src/main/resources/object_detection.jpg";
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
            //可以根据后续业务场景使用detectedImage
            BufferedImage detectedImage = detectorModel.detectAndDraw(image);
            Assert.assertNotNull("detectedImage null", detectedImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * GPU 目标检测
     */
    @Test
    public void gpuObjectDetection(){
        DetectorModelConfig config = new DetectorModelConfig();
        config.setModelEnum(DetectorModelEnum.YOLO11N);//检测模型，目前支持19种模型
        config.setDevice(DeviceEnum.GPU);
        DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
        DetectionResponse detectionResponse = detectorModel.detect("src/main/resources/dog_bike_car.jpg");
        log.info("目标检测结果：{}", JSONObject.toJSONString(detectionResponse));
    }


    /**
     * 使用yolo官方模型检测物品识别
     */
    @Test
    public void objectDetectionWithOfficialModel(){
        DetectorModelConfig config = new DetectorModelConfig();
        config.setThreshold(0.3f);
        //也支持YoloV8：YOLOV8_OFFICIAL 模型可以从文档中提供的地址下载
        config.setModelEnum(DetectorModelEnum.YOLOV12_OFFICIAL);//检测模型，目前支持19种模型
        // 指定模型路径，需要更改为自己的模型路径
        config.setModelPath("E:\\ai\\models\\yolo12m\\yolov12m.onnx");
        DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
        //一定要将yolo官方的类别文件：synset.txt（文档中下载）放在模型同目录下，否则报错
        DetectionResponse detect = detectorModel.detect("E:\\ai\\testimage\\1.jpg");
        log.info("目标检测结果：{}", JSONObject.toJSONString(detect));
        detectorModel.detectAndDraw("E:\\ai\\testimage\\1.jpg","E:\\ai\\outimage\\11.png");
    }

    /**
     * 使用自己训练的模型检测
     */
    @Test
    public void objectDetectionWithCustomModel(){
        DetectorModelConfig config = new DetectorModelConfig();
        //也支持YoloV8：YOLOV8_CUSTOM 模型需要自己训练，训练教程可以查看文档
        config.setModelEnum(DetectorModelEnum.YOLOV12_CUSTOM);//自定义YOLOV12模型
        // 指定模型路径，需要更改为自己的模型路径
        config.setModelPath("/Users/xxx/Documents/develop/fire_model/best.onnx");
        DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
        //一定要将类别文件：synset.txt 放在模型同目录下，否则报错(具体请参看文档)
        detectorModel.detectAndDraw("/Users/xxx/Downloads/test.jpg","output/test_detected.jpg");
    }


}

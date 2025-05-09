package cn.smartjavaai.objectdetection.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.ObjectDetInfo;
import cn.smartjavaai.common.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 目标检测相关工具类
 * @author dwj
 * @date 2025/4/9
 */
public class DetectorUtils {


    /**
     * 转换为FaceDetectedResult
     * @param detection
     * @param img
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(DetectedObjects detection, Image img){
        if(Objects.isNull(detection) || Objects.isNull(detection.getProbabilities())
                || detection.getProbabilities().isEmpty() || Objects.isNull(detection.items()) || detection.items().isEmpty()){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        List<DetectedObjects.DetectedObject> detectedObjectList = detection.items();
        Iterator iterator = detectedObjectList.iterator();
        int index = 0;
        while(iterator.hasNext()) {
            DetectedObjects.DetectedObject result = (DetectedObjects.DetectedObject)iterator.next();
            String className = result.getClassName();
            BoundingBox box = result.getBoundingBox();
            int x = (int)(box.getBounds().getX() * (double)img.getWidth());
            int y = (int)(box.getBounds().getY() * (double)img.getHeight());
            int width = (int)(box.getBounds().getWidth() * (double)img.getWidth());
            int height = (int)(box.getBounds().getHeight() * (double)img.getHeight());
            DetectionRectangle rectangle = new DetectionRectangle(x, y, width, height);
            DetectionInfo detectionInfo = new DetectionInfo(rectangle, detection.getProbabilities().get(index).floatValue());
            ObjectDetInfo objectDetInfo = new ObjectDetInfo(className);
            detectionInfo.setObjectDetInfo(objectDetInfo);
            detectionInfoList.add(detectionInfo);
            index++;
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }



}

package cn.smartjavaai.vision.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Mask;
import ai.djl.modality.cv.output.Rectangle;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.obb.entity.ObbResult;
import cn.smartjavaai.obb.entity.YoloRotatedBox;
import org.apache.commons.collections.CollectionUtils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
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
            //目标检测
            if(box instanceof Rectangle){
                ObjectDetInfo objectDetInfo = new ObjectDetInfo(className);
                detectionInfo.setObjectDetInfo(objectDetInfo);
            }else if(box instanceof Mask){
                Mask mask = (Mask)box;
                InstanceSegInfo instanceSegInfo = new InstanceSegInfo(className, mask.getProbDist());
                detectionInfo.setInstanceSegInfo(instanceSegInfo);
            }
            detectionInfoList.add(detectionInfo);
            index++;
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }

    public static DetectionResponse obbToToDetectionResponse(ObbResult obbResult){
        if(Objects.isNull(obbResult) || CollectionUtils.isEmpty(obbResult.getRotatedBoxeList())){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for (YoloRotatedBox box : obbResult.getRotatedBoxeList()){
            List<Point> points = box.toPoints();
            RotatedBox rotatedBox = new RotatedBox(points.get(0), points.get(1), points.get(2), points.get(3));
            ObbDetInfo obbDetInfo = new ObbDetInfo(box.className, rotatedBox);
            DetectionInfo detectionInfo = new DetectionInfo();
            detectionInfo.setScore(box.score);
            detectionInfo.setObbDetInfo(obbDetInfo);
            detectionInfoList.add(detectionInfo);
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }


    /**
     * 绘制文本框及文本
     * @param srcMat
     * @param rotatedBoxeList
     */
    public static void drawRectWithText(Mat srcMat, List<YoloRotatedBox> rotatedBoxeList) {
        for(YoloRotatedBox box : rotatedBoxeList){
            List<Point> points = box.toPoints();
            Imgproc.line(srcMat, points.get(0).toCvPoint(), points.get(1).toCvPoint(), new Scalar(0, 255, 0), 1);
            Imgproc.line(srcMat, points.get(1).toCvPoint(), points.get(2).toCvPoint(), new Scalar(0, 255, 0),1);
            Imgproc.line(srcMat, points.get(2).toCvPoint(), points.get(3).toCvPoint(), new Scalar(0, 255, 0),1);
            Imgproc.line(srcMat, points.get(3).toCvPoint(), points.get(1).toCvPoint(), new Scalar(0, 255, 0), 1);
            // 中文乱码
            Imgproc.putText(srcMat, box.className, points.get(0).toCvPoint(), Imgproc.FONT_HERSHEY_SCRIPT_SIMPLEX, 1.0, new Scalar(0, 255, 0), 1);
        }
    }


}

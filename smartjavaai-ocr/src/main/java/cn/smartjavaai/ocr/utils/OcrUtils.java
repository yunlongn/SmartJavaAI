package cn.smartjavaai.ocr.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author dwj
 * @date 2025/4/22
 */
@Slf4j
public class OcrUtils {


    /**
     * 转换为FaceDetectedResult
     * @param dt_boxes
     * @param img
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(NDList dt_boxes, Image img){
        if(Objects.isNull(dt_boxes) || dt_boxes.size() == 0){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for(NDArray box : dt_boxes){
            DetectionRectangle rectangle = new DetectionRectangle();
            float[] points = box.toFloatArray();
            log.info("points: {}", points);
            int x = (int)points[0];
            int y = (int)points[1];
            int width = new BigDecimal(points[4]).subtract(new BigDecimal(points[6])).intValue();
            int height = new BigDecimal(points[7]).subtract(new BigDecimal(points[1])).intValue();

            // 修正边界，防止越界
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x + width > img.getWidth()) width = img.getWidth() - x;
            if (y + height > img.getHeight()) height = img.getHeight() - y;

            rectangle.setX(x);
            rectangle.setY(y);
            rectangle.setHeight(height);
            rectangle.setWidth(width);
            detectionInfoList.add(new DetectionInfo(rectangle));
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }


}

package cn.smartjavaai.objectdetection.stream;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.entity.DetectionInfo;

import java.util.List;

/**
 * 视频流目标检测监听器
 * @author dwj
 */
public interface StreamDetectionListener {


    void onObjectDetected(List<DetectionInfo> detectionInfoList, Image image);
}

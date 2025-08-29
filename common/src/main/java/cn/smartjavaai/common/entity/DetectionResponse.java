package cn.smartjavaai.common.entity;

import ai.djl.modality.cv.Image;
import lombok.Data;

import java.util.List;

/**
 * 检测结果
 * @author dwj
 * @date 2025/4/12
 */
@Data
public class DetectionResponse {

    private List<DetectionInfo> detectionInfoList;

    private Image drawnImage;


    public DetectionResponse() {
    }

    public DetectionResponse(List<DetectionInfo> detectionInfoList) {
        this.detectionInfoList = detectionInfoList;
    }
}

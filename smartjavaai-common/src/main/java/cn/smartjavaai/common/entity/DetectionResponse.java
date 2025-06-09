package cn.smartjavaai.common.entity;

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


    public DetectionResponse() {
    }

    public DetectionResponse(List<DetectionInfo> detectionInfoList) {
        this.detectionInfoList = detectionInfoList;
    }
}

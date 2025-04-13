package cn.smartjavaai.common.entity;

import java.util.List;

/**
 * 检测结果
 * @author dwj
 * @date 2025/4/12
 */
public class DetectionResponse {

    private List<DetectionRectangle> rectangleList;

    public List<DetectionRectangle> getRectangleList() {
        return rectangleList;
    }

    public void setRectangleList(List<DetectionRectangle> rectangleList) {
        this.rectangleList = rectangleList;
    }
}

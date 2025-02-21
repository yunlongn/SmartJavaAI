package cn.smartjavaai.face;


import cn.smartjavaai.common.entity.Rectangle;

import java.util.List;

/**
 * 人脸检测结果
 * @author dwj
 * @date 2025/2/19
 */
public class FaceDetectedResult {


    /**
     * 置信度
     */
    private List<Double> probabilities;

    /**
     * 人脸框
     */
    private List<Rectangle> rectangles;

    public List<Double> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(List<Double> probabilities) {
        this.probabilities = probabilities;
    }

    public List<Rectangle> getRectangles() {
        return rectangles;
    }

    public void setRectangles(List<Rectangle> rectangles) {
        this.rectangles = rectangles;
    }
}

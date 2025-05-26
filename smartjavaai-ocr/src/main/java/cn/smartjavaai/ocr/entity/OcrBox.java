package cn.smartjavaai.ocr.entity;

import cn.smartjavaai.common.entity.Point;
import lombok.Data;

/**
 * OCR 检测框
 * @author dwj
 * @date 2025/5/20
 */
@Data
public class OcrBox {

    /**
     * 左上角
     */
    private Point topLeft;

    /**
     * 右上角
     */
    private Point topRight;

    /**
     * 右下角
     */
    private Point bottomRight;

    /**
     * 左下角
     */
    private Point bottomLeft;

    public OcrBox(Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
    }

    public OcrBox() {
    }

    public float[] toFloatArray() {
        return new float[]{
                (float)topLeft.getX(), (float)topLeft.getY(),
                (float)topRight.getX(), (float)topRight.getY(),
                (float)bottomRight.getX(), (float)bottomRight.getY(),
                (float)bottomLeft.getX(), (float)bottomLeft.getY()
        };
    }
}

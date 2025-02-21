package cn.smartjavaai.common.entity;

import java.util.List;

/**
 * 矩形区域
 * @author dwj
 * @date 2025/2/19
 */
public class Rectangle {

    /**
     * 矩形区域点集合
     */
    List<Point> pointList;

    /**
     * 矩形区域宽度
     */
    int width;

    /**
     * 矩形区域高度
     */
    int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<Point> getPointList() {
        return pointList;
    }

    public void setPointList(List<Point> pointList) {
        this.pointList = pointList;
    }
}

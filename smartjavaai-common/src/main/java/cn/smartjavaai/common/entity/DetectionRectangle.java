package cn.smartjavaai.common.entity;

/**
 * 检测结果-矩形区域
 * @author dwj
 */
public class DetectionRectangle {

    public int x;
    public int y;
    public int width;
    public int height;
    public float score;

    public String className;

    public DetectionRectangle() {
    }

    public DetectionRectangle(int x, int y, int width, int height, float score) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.score = score;
    }

    public DetectionRectangle(int x, int y, int width, int height, float score, String className) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.score = score;
        this.className = className;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

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

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}

package cn.smartjavaai.common.entity;

import cn.smartjavaai.common.enums.GenderType;
import cn.smartjavaai.common.enums.LivenessStatus;
import lombok.Data;

import java.util.List;

/**
 * 检测结果-矩形区域
 * @author dwj
 */
@Data
public class DetectionRectangle {

    public int x;
    public int y;
    public int width;
    public int height;

    public DetectionRectangle() {
    }

    public DetectionRectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}

package cn.smartjavaai.common.utils;

import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;

import java.util.Arrays;

/**
 * 按比例缩放，剩余空间用指定颜色填充
 * @author dwj
 */
public class LetterBoxUtils {

    public enum PaddingPosition {
        CENTER, LEFT_TOP, RIGHT_BOTTOM
    }

    public static class ResizeResult {
        public NDArray image;
        public float r;
        public int left;
        public int top;
    }

    public static ResizeResult letterboxWithMeta(NDArray paddingImg, float r, int left, int top) {
        // ... letterbox 逻辑不变
        ResizeResult result = new ResizeResult();
        result.image = paddingImg;
        result.r = r;
        result.left = left;
        result.top = top;
        return result;
    }


    /**
     * 按比例缩放 + padding
     *
     * @param img           原图 NDArray HWC
     * @param targetW       目标宽度
     * @param targetH       目标高度
     * @param padColor      padding 填充颜色，RGB 归一化 0-1
     * @param position      padding 位置：CENTER / LEFT_TOP / RIGHT_BOTTOM
     * @return              处理后的 NDArray
     */
    public static ResizeResult letterbox(NDManager manager, NDArray img, int targetW, int targetH, float padColor, PaddingPosition position) {
        long origH = img.getShape().get(0);
        long origW = img.getShape().get(1);

        float r = Math.min(targetW / (float) origW, targetH / (float) origH);
        int newW = Math.round(origW * r);
        int newH = Math.round(origH * r);

        img = NDImageUtils.resize(img, newW, newH);  // HWC 0~1

//        NDArray paddingImg = manager
//                .full(new Shape(targetW, targetH, 3), padColor, DataType.UINT8);

        NDArray paddingImg = manager.zeros(new Shape(targetW, targetH, 3), DataType.FLOAT32);
        paddingImg = paddingImg.add(114);

        int padW = targetW - newW;
        int padH = targetH - newH;
        int top = 0, left = 0;

        switch (position) {
            case CENTER:
                left = padW / 2;
                top = padH / 2;
                break;
            case LEFT_TOP:
                left = 0;
                top = 0;
                break;
            case RIGHT_BOTTOM:
                left = padW;
                top = padH;
                break;
        }
        paddingImg.set(new NDIndex(String.format("%d:%d,%d:%d", top, top + newH, left, left + newW)), img);
        return letterboxWithMeta(paddingImg, r, left, top);
    }


    /**
     * 恢复缩放后的 box
     * @param boxes
     * @param scaleRatio
     * @param left
     * @param top
     * @param keypointStart
     * @param keypointDim
     * @return
     */
    public static NDArray restoreBox(NDArray boxes, float scaleRatio, float left, float top, int keypointStart, int keypointDim) {
        // 处理 bbox
        NDArray x1 = boxes.get(":, 0").sub(left).div(scaleRatio);
        NDArray y1 = boxes.get(":, 1").sub(top).div(scaleRatio);
        NDArray x2 = boxes.get(":, 2").sub(left).div(scaleRatio);
        NDArray y2 = boxes.get(":, 3").sub(top).div(scaleRatio);

        boxes.set(new NDIndex(":, 0"), x1);
        boxes.set(new NDIndex(":, 1"), y1);
        boxes.set(new NDIndex(":, 2"), x2);
        boxes.set(new NDIndex(":, 3"), y2);

        if (keypointDim > 0) {
            for (int i = 0; i < keypointDim; i += 2) {
                int xIdx = keypointStart + i;
                int yIdx = keypointStart + i + 1;
                NDArray keyX = boxes.get(":, " + xIdx).sub(left).div(scaleRatio);
                NDArray keyY = boxes.get(":, " + yIdx).sub(top).div(scaleRatio);
                boxes.set(new NDIndex(":, " + xIdx), keyX);
                boxes.set(new NDIndex(":, " + yIdx), keyY);
            }
        }
        return boxes;
    }

}

package cn.smartjavaai.common.utils;

import ai.djl.ndarray.NDArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dwj
 * @date 2025/7/23
 */
public class NMSUtils {

    /**
     * 通用 NMS 方法，输入 NDArray 形式的 boxes 和 scores，返回保留的索引列表
     *
     * @param boxes   NDArray 形状为 (N, 4)，格式为 [x1, y1, x2, y2]
     * @param scores  NDArray 形状为 (N,) 或 (N,1)，每个 box 的置信度
     * @param iouThreshold IOU 阈值，超过该阈值则认为有重叠
     * @return 保留框的索引列表
     */
    public static int[] nms(NDArray boxes, NDArray scores, float iouThreshold) {
        if (boxes.isEmpty()) {
            return new int[0];
        }

        NDArray x1 = boxes.get(":, 0");
        NDArray y1 = boxes.get(":, 1");
        NDArray x2 = boxes.get(":, 2");
        NDArray y2 = boxes.get(":, 3");

        NDArray areas = x2.sub(x1).add(1).mul(y2.sub(y1).add(1));

        // 按照置信度降序排序
        NDArray order = scores.argSort().flip(0);

        List<Integer> keep = new ArrayList<>();

        while (order.size() > 0) {
            int idx = (int)order.getLong(0);
            keep.add(idx);

            if (order.size() == 1) break;

            NDArray currentBox = boxes.get(idx);
            NDArray others = boxes.get(order);

            NDArray xx1 = x1.get(order).maximum(x1.get(idx));
            NDArray yy1 = y1.get(order).maximum(y1.get(idx));
            NDArray xx2 = x2.get(order).minimum(x2.get(idx));
            NDArray yy2 = y2.get(order).minimum(y2.get(idx));

            NDArray w = xx2.sub(xx1).add(1).maximum(0);
            NDArray h = yy2.sub(yy1).add(1).maximum(0);
            NDArray inter = w.mul(h);

            NDArray remAreas = areas.get(order);
            NDArray union = remAreas.add(areas.get(idx)).sub(inter);
            NDArray iou = inter.div(union);

            NDArray mask = iou.lte(iouThreshold);
            order = order.get(mask);
        }
        return keep.stream().mapToInt(i -> i).toArray();
    }

}

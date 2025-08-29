package cn.smartjavaai.face.model.facedect.mtcnn;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.TranslateException;
import ai.djl.translate.TranslatorContext;
import cn.smartjavaai.common.utils.NMSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dwj
 */
public class PNetModel {



    /**
     * 输入图片
     * @param imgs
     * @param w
     * @param h
     * @param scale
     * @return
     */
    public static NDList processInput(NDArray imgs, int w, int h, double scale){
        int newH = (int) (h * scale + 1);
        int newW = (int) (w * scale + 1);
        //  (N, H, W, C)
        NDArray transposed = imgs.transpose(0, 2, 3, 1);
        transposed = NDImageUtils.resize(transposed, newW, newH, Image.Interpolation.AREA);
        // (N, C, H, W)
        transposed = transposed.transpose(0, 3, 1, 2);
        // 归一化
        transposed = transposed.sub(127.5).mul(0.0078125f);
        System.out.println("inputPnet: " + transposed.getShape());
        return new NDList(transposed);
    }

    public static NDList processOutput(NDList outputPnet, double scale, NDManager manager){
        NDArray reg = outputPnet.get(0);   // [B, 4, H, W]
        NDArray probs = outputPnet.get(1); // [B, 2, H, W]
        List<NDArray> boundingBox = generateBoundingBox(reg, probs.get(":, 1"), (float)scale, 0.6f);
        NDArray boxes_scale = boundingBox.get(0); // [N,9]
        NDArray imgIndND = boundingBox.get(1); // [N]
        NDArray pick = NMSUtils.batchedNms(boxes_scale.get(":,:4"), boxes_scale.get(":,4"), imgIndND, 0.5f, manager);
        return new NDList(boxes_scale, imgIndND, pick);
    }


    public static NDList firstStage(NDManager manager, Predictor<NDList, NDList> pnetPredictor, NDArray imgs, List<Double> scales, int width, int height) throws TranslateException {
        // 第一阶段
        NDList boxes_list = new NDList();
        NDList image_inds_list = new NDList();
        NDList scale_picks_list = new NDList();
        int offset = 0;
        for (double scale : scales) {
            NDList inputPnet = processInput(imgs, width, height, scale);
            NDList outputPnet = pnetPredictor.predict(inputPnet);
            NDList output = processOutput(outputPnet, scale, manager);
            NDArray boxes_scale = output.get(0);
            NDArray imgIndND = output.get(1);
            NDArray pick = output.get(2);
            boxes_list.add(boxes_scale);
            image_inds_list.add(imgIndND);
            scale_picks_list.add(pick.add(offset));
            offset += boxes_scale.getShape().get(0);
        }
        // 270 9
        NDArray boxes = NDArrays.concat(boxes_list, 0);
        NDArray image_inds = NDArrays.concat(image_inds_list, 0);
        NDArray scale_picks = NDArrays.concat(scale_picks_list, 0);

        // NMS within each scale + image
        boxes = boxes.get(scale_picks);         // scalePicksAll 是 NDArrays.concat 后的 INT64 NDArray
        image_inds = image_inds.get(scale_picks); // 同样索引

        // NMS within each image
        NDArray pick = NMSUtils.batchedNms(
                boxes.get(":, :4"),       // 坐标
                boxes.get(":, 4"),        // score
                image_inds,                // 每个框对应的图片编号
                0.7f,                      // IoU 阈值
                manager
        );
        // 8 9
        boxes = boxes.get(pick);
        image_inds = image_inds.get(pick);

        System.out.println(Arrays.toString(boxes.get(0).toFloatArray()));

        NDArray regw = boxes.get(":, 2").sub(boxes.get(":, 0"));
        NDArray regh = boxes.get(":, 3").sub(boxes.get(":, 1"));

        NDArray qq1 = boxes.get(":, 0").add(boxes.get(":, 5").mul(regw));
        NDArray qq2 = boxes.get(":, 1").add(boxes.get(":, 6").mul(regh));
        NDArray qq3 = boxes.get(":, 2").add(boxes.get(":, 7").mul(regw));
        NDArray qq4 = boxes.get(":, 3").add(boxes.get(":, 8").mul(regh));

        boxes = NDArrays.stack(new NDList(qq1, qq2, qq3, qq4, boxes.get(":, 4")), 1);
        boxes = MtcnnUtils.rerec(boxes);
        return new NDList(boxes, image_inds);
    }

    /**
     * 生成候选框，等价于 Python 版 generateBoundingBox
     *
     * @param reg       NDArray [B,4,H,W]，回归偏移量
     * @param probs     NDArray [B,H,W]，人脸概率
     * @param scale     当前金字塔缩放比例
     * @param threshold 阈值
     * @return 一个包含两个元素的 List：
     *         0 -> NDArray bounding boxes [N,9] (x1,y1,x2,y2,score,dx1,dy1,dx2,dy2)
     *         1 -> NDArray image_inds [N]
     */
    public static List<NDArray> generateBoundingBox(
            NDArray reg, NDArray probs, float scale, float threshold) {

        float stride = 2f;
        float cellSize = 12f;

        // mask = probs >= thresh  -> [B,H,W]
        NDArray mask = probs.gte(threshold);

        System.out.println("reg: " + reg.getShape());
        System.out.println("probs shape: " + probs.getShape());
        System.out.println("scale: " + scale);
        System.out.println("mask: " + mask.getShape());
        // mask_inds = mask.nonzero()  -> [N,3]  每行: (batch, y, x)
        NDArray maskInds = mask.nonzero();
        System.out.println("maskInds: " + maskInds.getShape());

        // image_inds = mask_inds[:, 0]
        NDArray imageInds = maskInds.get(":,0");

        // yx = mask_inds[:, 1:]  [N,2] -> (y, x)
        NDArray yx = maskInds.get(":,1:");

        // bb = mask_inds[:, 1:].flip(1)   Python 是 (y,x) -> (x,y)
        NDArray bb = yx.flip(1); // [N,2]  (x, y)

        // 左上角 (x1, y1) 坐标 q1 = ((stride * bb + 1) / scale).floor()
        NDArray q1 = bb.mul(stride).add(1).div(scale).floor();

        // 右下角 (x2, y2) 坐标 q2 = ((stride * bb + cellsize) / scale).floor()
        NDArray q2 = bb.mul(stride).add(cellSize).div(scale).floor();

        Shape probShape = probs.getShape(); // [B,H,W]
        long H = probShape.get(1);
        long W = probShape.get(2);

        NDArray linearIndex = maskInds.get(":,0").mul(H * W)
                .add(maskInds.get(":,1").mul(W))
                .add(maskInds.get(":,2"));

        NDArray scores = probs.reshape(-1).gather(linearIndex, 0);

        NDArray regPerm = reg.transpose(1, 0, 2, 3); // [4,B,H,W]
        NDArray regFlat = regPerm.reshape(4, -1);    // [4, total]

        NDArray linearIndexForGather = linearIndex.expandDims(0).repeat(0, 4); // [4, N]
        NDArray regPicked = regFlat.gather(linearIndexForGather, 1).transpose(); // [N,4]

        NDArray x1 = q1.get(":, 0").expandDims(1);
        NDArray y1 = q1.get(":, 1").expandDims(1);
        NDArray x2 = q2.get(":, 0").expandDims(1);
        NDArray y2 = q2.get(":, 1").expandDims(1);

        NDArray boundingBoxes = NDArrays.concat(
                new NDList(x1, y1, x2, y2, scores.expandDims(1), regPicked), 1
        );

        return Arrays.asList(boundingBoxes, imageInds);
    }


}

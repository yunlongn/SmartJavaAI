package cn.smartjavaai.face.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Landmark;
import ai.djl.modality.cv.output.Point;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import cn.smartjavaai.common.utils.LetterBoxUtils;
import cn.smartjavaai.common.utils.NMSUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SCRFD Translator
 */
public class SCRFDFaceTranslator implements Translator<Image, DetectedObjects> {

    private double confThresh;
    private double nmsThresh;
    private int topK;
    private int[] steps;
    private int inputWidth = 640;
    private int inputHeight = 640;

    public SCRFDFaceTranslator(
            double confThresh,
            double nmsThresh,
            int topK,
            int[] steps) {
        this.confThresh = confThresh;
        this.nmsThresh = nmsThresh;
        this.topK = topK;
        this.steps = steps;
    }

    /** {@inheritDoc} */
    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {

        ctx.setAttachment("width", input.getWidth());
        ctx.setAttachment("height", input.getHeight());

        NDManager manager = ctx.getNDManager();
        NDArray array = input.toNDArray(manager, Image.Flag.COLOR);

        //Letter box resize 640x640 with padding (保持比例，补边缘)
        LetterBoxUtils.ResizeResult letterBoxResult = LetterBoxUtils.letterbox(manager, array, inputWidth, inputHeight, 0f, LetterBoxUtils.PaddingPosition.LEFT_TOP);
        ctx.setAttachment("scale", letterBoxResult.r);
        array = letterBoxResult.image;
        array = array.transpose(2, 0, 1).flip(0); // HWC -> CHW RGB -> BGR
        // The network by default takes float32
        if (!array.getDataType().equals(DataType.FLOAT32)) {
            array = array.toType(DataType.FLOAT32, false);
        }
        // 归一化
        array = array.sub(127.5).mul(0.0078125);
        return new NDList(array);
    }

    /** {@inheritDoc} */
    @Override
    public DetectedObjects processOutput(TranslatorContext ctx, NDList list) {

        NDManager manager =
                NDManager.newBaseManager(ctx.getNDManager().getDevice(), "PyTorch");
        int sourceWidth = (int) ctx.getAttachment("width");
        int sourceHeight = (int) ctx.getAttachment("height");
        float detScale = (float) ctx.getAttachment("scale");

        Map<String, NDArray> centerCache = new HashMap<>();
        List<NDArray> scores_list = new ArrayList<>();
        List<NDArray> bboxes_list = new ArrayList<>();
        List<NDArray> kpss_list = new ArrayList<>();
        // 步长长度
        int fmc = steps.length;
        int numAnchors = 2;
        // 多尺度后处理
        for (int idx = 0; idx < steps.length; idx++) {
            int stride = steps[idx];
            NDArray scores, bboxPreds, kpsPreds = null;
            scores = list.get(idx);
            bboxPreds = list.get(idx + fmc).mul(stride);
            kpsPreds = list.get(idx + fmc * 2).mul(stride);
            int height = inputHeight / stride;
            int width = inputWidth / stride;
            String key = height + "_" + width + "_" + stride;
            // anchor centers cache
            NDArray anchorCenters;
            if (centerCache.containsKey(key)) {
                anchorCenters = centerCache.get(key);
            } else {
                NDArray yv = manager.arange((float) height).reshape(height, 1).repeat(1, width);
                NDArray xv = manager.arange((float) width).reshape(1, width).repeat(0, height);
                // stack x, y 到最后一维
                anchorCenters = xv.stack(yv, -1); // shape [height, width, 2]
                // 乘 stride
                anchorCenters = anchorCenters.mul(stride);
                // 拉平成 [-1, 2]，等价于 NumPy 的 reshape((-1, 2))
                long total = anchorCenters.getShape().get(0) * anchorCenters.getShape().get(1);
                // anchorCenters 现在是 [height*width, 2]
                anchorCenters = anchorCenters.reshape(total, 2);
                // 在第一维重复 numAnchors 次，直接拉平成最终形状
                anchorCenters = anchorCenters.repeat(0, numAnchors); // shape [N*numAnchors, 2]
                if (centerCache.size() < 100) {
                    centerCache.put(key, anchorCenters);
                }
            }

            NDArray pos_mask = scores.gte(confThresh); // scores >= thresh
            NDArray pos_inds = pos_mask.nonzero();  // shape: [N, 2]
            pos_inds = pos_inds.get(":, 0"); // 取第一列的索引
//            System.out.println(Arrays.toString(pos_inds.toLongArray()));
            // 计算 bbox
            NDArray bboxes = distance2bbox(anchorCenters, bboxPreds); // [num_anchors, 4]

            // 取出符合阈值的
            NDArray pos_scores = scores.get(pos_inds);
            NDArray pos_bboxes = bboxes.get(pos_inds);

            scores_list.add(pos_scores);
            bboxes_list.add(pos_bboxes);

            NDArray kpss = distance2kps(anchorCenters, kpsPreds); // [num_anchors, num_kps*2]
            kpss = kpss.reshape(kpss.getShape().get(0), -1, 2); // reshape (N, -1, 2)
            NDArray pos_kpss = kpss.get(pos_inds);
            kpss_list.add(pos_kpss);
        }

        // 1. 合并 scores
        NDArray scores = NDArrays.concat(new NDList(scores_list), 0);
        NDArray scoresRavel = scores.reshape(-1);

        // 2. 得到排序索引
        long[] orderLong = scoresRavel.argSort().flip(0).get(":" + topK).toLongArray();

        // 3. 合并 bboxes
        NDArray bboxes = NDArrays.concat(new NDList(bboxes_list), 0).div(detScale);

        NDArray kpss = NDArrays.concat(new NDList(kpss_list), 0).div(detScale);
        // 4. 拼接 [x1,y1,x2,y2,score]
        NDArray preDet = bboxes.concat(scores.reshape(-1,1), 1);
        // 5. 按 order 排序
        preDet = preDet.get(manager.create(orderLong));
        // 6. NMS
        int[] keep = NMSUtils.nms(preDet.get(":,0:4"), preDet.get(":,4"), (float)nmsThresh);
        NDArray det = preDet.get(manager.create(keep));
//        System.out.println(Arrays.toString(det.toFloatArray()));
        if (kpss != null) {
            kpss = kpss.get(manager.create(orderLong));
            kpss = kpss.get(manager.create(keep));
        }
        List<String> retNames = new ArrayList<>();
        List<Double> retProbs = new ArrayList<>();
        List<BoundingBox> retBB = new ArrayList<>();
        long numDet = det.getShape().get(0);  // N
        long numCols = det.getShape().get(1); // 应该是 5: x1,y1,x2,y2,score
        float[] flat = det.toFloatArray(); // 一维
        for (int i = 0; i < numDet; i++) {
            int base = (int) (i * numCols);
            float x1 = flat[base] / sourceWidth;
            float y1 = flat[base + 1] / sourceHeight;
            float x2 = flat[base + 2] / sourceWidth;
            float y2 = flat[base + 3] / sourceHeight;
            float score = flat[base + 4];

            retNames.add("face"); // 类别
            retProbs.add((double) score);

            float width = x2 - x1;
            float height = y2 - y1;
            Landmark rect = new Landmark(x1, y1, width, height, decodeKps(kpss.get(i)));
            retBB.add(rect);
        }
        return new DetectedObjects(retNames, retProbs, retBB);
    }


    public NDArray distance2bbox(NDArray points, NDArray distance) {
        // points: [N, 2], distance: [N, 4]
        NDArray x1 = points.get(":, 0").sub(distance.get(":, 0")); // x - left
        NDArray y1 = points.get(":, 1").sub(distance.get(":, 1")); // y - top
        NDArray x2 = points.get(":, 0").add(distance.get(":, 2")); // x + right
        NDArray y2 = points.get(":, 1").add(distance.get(":, 3")); // y + bottom
        // stack([x1, y1, x2, y2], axis=-1)
        NDList list = new NDList(x1.expandDims(1), y1.expandDims(1), x2.expandDims(1), y2.expandDims(1));
        return NDArrays.concat(list, 1); // axis=1 表示最后一维
    }


    public NDArray distance2kps(NDArray points, NDArray distance) {
        // points: [N, 2], distance: [N, 2*num_kps]
        int numKps = (int) distance.getShape().get(1) / 2;
        List<NDArray> preds = new ArrayList<>();
        for (int i = 0; i < numKps * 2; i += 2) {
            NDArray px = points.get(":, " + (i % 2)).add(distance.get(":, " + i));
            NDArray py = points.get(":, " + ((i % 2) + 1)).add(distance.get(":, " + (i + 1)));
            preds.add(px);
            preds.add(py);
        }
        // stack(preds, axis=-1)
        NDList stackList = new NDList();
        for (NDArray arr : preds) {
            stackList.add(arr.expandDims(1));
        }
        return NDArrays.concat(stackList, 1); // shape [N, num_kps*2]
    }


    public List<Point> decodeKps(NDArray kpss) {
        // 转成一维 float 数组
        float[] flat = kpss.toFloatArray();

        // reshape 成二维 [5][2]
        int numPoints = (int) kpss.getShape().get(0); // 5
        int dim = (int) kpss.getShape().get(1);       // 2
        float[][] kpsArray = new float[numPoints][dim];
        for (int i = 0; i < numPoints; i++) {
            for (int j = 0; j < dim; j++) {
                kpsArray[i][j] = flat[i * dim + j];
            }
        }

        // 转成 Point 数组
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            points.add(new Point(Math.round(kpsArray[i][0]), Math.round(kpsArray[i][1])));
        }
        return points;
    }


}

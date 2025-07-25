package cn.smartjavaai.ocr.model.common.direction.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import cn.smartjavaai.ocr.entity.DirectionInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 方向检测
 *
 * @author Calvin
 * @mail 179209347@qq.com
 * @website www.aias.top
 */
public class PpWordRotateTranslator implements Translator<Image, DirectionInfo> {
    List<String> classes = Arrays.asList("No Rotate", "Rotate");

    private String batchifier;

    private int resizeHeight;

    private int resizeWidth;

    public PpWordRotateTranslator(Map<String, ?> arguments) {
        batchifier =  arguments.containsKey("batchifier")
                ? arguments.get("batchifier").toString()
                : "padding";

        resizeWidth =  arguments.containsKey("resizeWidth")
                ? (Integer) arguments.get("resizeWidth")
                : 192;

        resizeHeight =  arguments.containsKey("resizeHeight")
                ? (Integer) arguments.get("resizeHeight")
                : 48;
    }

    @Override
    public DirectionInfo processOutput(TranslatorContext ctx, NDList list) {
        NDArray prob = list.singletonOrThrow();
        float[] res = prob.toFloatArray();
        int maxIndex = 0;
        if (res[1] > res[0]) {
            maxIndex = 1;
        }

        return new DirectionInfo(classes.get(maxIndex), Double.valueOf(res[maxIndex]));
    }

//    public NDList processInput2(TranslatorContext ctx, Image input){
//        NDArray img = input.toNDArray(ctx.getNDManager());
//        img = NDImageUtils.resize(img, 192, 48);
//        img = NDImageUtils.toTensor(img).sub(0.5F).div(0.5F);
//        img = img.expandDims(0);
//        return new NDList(new NDArray[]{img});
//    }

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDArray img = input.toNDArray(ctx.getNDManager());
        int imgC = 3;
        int imgH = resizeHeight;
        int imgW = resizeWidth;

        NDArray array = ctx.getNDManager().zeros(new Shape(imgC, imgH, imgW));

        int h = input.getHeight();
        int w = input.getWidth();
        int resized_w = 0;

        float ratio = (float) w / (float) h;
        if (Math.ceil(imgH * ratio) > imgW) {
            resized_w = imgW;
        } else {
            resized_w = (int) (Math.ceil(imgH * ratio));
        }

        img = NDImageUtils.resize(img, resized_w, imgH);

        img = NDImageUtils.toTensor(img).sub(0.5F).div(0.5F);
        //    img = img.transpose(2, 0, 1);

        array.set(new NDIndex(":,:,0:" + resized_w), img);

//        array = array.expandDims(0);

        return new NDList(new NDArray[]{array});
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.fromString(batchifier);
    }

}

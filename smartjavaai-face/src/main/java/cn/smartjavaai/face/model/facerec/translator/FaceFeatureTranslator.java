package cn.smartjavaai.face.model.facerec.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

/**
 * facenet人脸特征提取Translator
 * @author dwj
 * @date 2025/3/31
 */
public final class FaceFeatureTranslator implements Translator<Image, float[]> {



    public FaceFeatureTranslator() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDArray array = input.toNDArray(ctx.getNDManager(), Image.Flag.COLOR);
        Pipeline pipeline = new Pipeline();
        if(input.getWidth() != 112 || input.getHeight() != 112){
            pipeline.add(new Resize(112));
        }
        pipeline
                .add(new ToTensor())
                .add(new Normalize(
                        new float[]{0.5F, 0.5F, 0.5F},
                        new float[]{0.5F, 0.5F, 0.5F}));

        return pipeline.transform(new NDList(array));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float[] processOutput(TranslatorContext ctx, NDList list) {
        NDArray embedding = list.singletonOrThrow();
        embedding = embedding.div(embedding.norm()); // L2归一化
        return embedding.toFloatArray();
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
}

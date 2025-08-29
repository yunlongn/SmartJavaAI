package cn.smartjavaai.face.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.*;
import ai.djl.modality.cv.transform.*;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.*;

import java.util.*;

/**
 * YoloV8Translator
 * @author dwj
 */
public class YoloV8FaceTranslator implements Translator<Image, DetectedObjects> {

    private int maxBoxes;

    private YoloOutputType yoloOutputLayerType;
    private float nmsThreshold;

    protected float threshold;
//    private BaseImageTranslator.SynsetLoader synsetLoader;
    protected List<String> classes;
    protected boolean applyRatio;
    protected boolean removePadding;

    protected Pipeline pipeline;
    private Image.Flag flag;
    private Batchifier batchifier;
    protected int width;
    protected int height;


    /**
     * Constructs an ImageTranslator with the provided builder.
     *
     * @param builder the data to build with
     */
    protected YoloV8FaceTranslator(Builder builder) {
        this.yoloOutputLayerType = builder.outputType;
        this.nmsThreshold = builder.nmsThreshold;
        maxBoxes = builder.maxBox;
        this.threshold = builder.threshold;
//        this.synsetLoader = builder.synsetLoader;
        this.applyRatio = builder.applyRatio;
        this.removePadding = builder.removePadding;
        this.flag = builder.flag;
        this.pipeline = builder.pipeline;
        this.batchifier = builder.batchifier;
        this.width = builder.width;
        this.height = builder.height;
        classes = Arrays.asList("face");
    }

    /**
     * Creates a builder to build a {@code YoloV8Translator} with specified arguments.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder to build a {@code YoloV8Translator} with specified arguments.
     *
     * @param arguments arguments to specify builder options
     * @return a new builder
     */
    public static Builder builder(Map<String, ?> arguments) {
        Builder builder = new Builder();
        builder.configPreProcess(arguments);
        builder.configPostProcess(arguments);
        return builder;
    }

    /** {@inheritDoc} */
    protected DetectedObjects processFromBoxOutput(int imageWidth, int imageHeight, NDList list) {
        NDArray rawResult = list.get(0);
        NDArray reshapedResult = rawResult.transpose();
        Shape shape = reshapedResult.getShape();
        float[] buf = reshapedResult.toFloatArray();
        int numberRows = Math.toIntExact(shape.get(0));
        int nClasses = Math.toIntExact(shape.get(1));
        int padding = nClasses - classes.size();
        System.out.println(Arrays.toString(reshapedResult.get(0).toFloatArray()));
//        if (padding != 0 && padding != 4) {
//            throw new IllegalStateException(
//                    "Expected classes: " + (nClasses - 4) + ", got " + classes.size());
//        }

        ArrayList<Landmark> boxes = new ArrayList<>();
        ArrayList<Float> scores = new ArrayList<>();
        ArrayList<Integer> classIds = new ArrayList<>();

        // reverse order search in heap; searches through #maxBoxes for optimization when set
        for (int i = numberRows - 1; i > numberRows - maxBoxes; --i) {
            int index = i * nClasses;
            float maxClassProb = buf[index + 4];
//            int maxIndex = -1;
//            for (int c = 4; c < nClasses; c++) {
//                float classProb = buf[index + c];
//                if (classProb > maxClassProb) {
//                    maxClassProb = classProb;
//                    maxIndex = c;
//                }
//            }
//            maxIndex -= padding;

            if (maxClassProb > threshold) {
                float xPos = buf[index]; // center x
                float yPos = buf[index + 1]; // center y
                float w = buf[index + 2];
                float h = buf[index + 3];
                Rectangle rect =
                        new Rectangle(Math.max(0, xPos - w / 2), Math.max(0, yPos - h / 2), w, h);
//                boxes.add(rect);
                scores.add(maxClassProb);
                classIds.add(0);
                List<Point> keypoints = new ArrayList<>();
                keypoints.add(new Point(buf[index + 5], buf[index + 6]));
                keypoints.add(new Point(buf[index + 8], buf[index + 9]));
                keypoints.add(new Point(buf[index + 11], buf[index + 12]));
                keypoints.add(new Point(buf[index + 14], buf[index + 15]));
                keypoints.add(new Point(buf[index + 17], buf[index + 18]));
                Landmark kps = new Landmark(Math.max(0, xPos - w / 2), Math.max(0, yPos - h / 2), w, h, keypoints);
                boxes.add(kps);
            }
        }

        return nms(imageWidth, imageHeight, boxes, classIds, scores);
    }

    private DetectedObjects processFromDetectOutput() {
        throw new UnsupportedOperationException(
                "detect layer output is not supported yet, check correct YoloV5 export format");
    }

    @Override
    public DetectedObjects processOutput(TranslatorContext ctx, NDList list) throws Exception {
        int imageWidth = (Integer) ctx.getAttachment("width");
        int imageHeight = (Integer) ctx.getAttachment("height");
        switch (yoloOutputLayerType) {
            case DETECT:
                return processFromDetectOutput();
            case AUTO:
                if (list.get(0).getShape().dimension() > 2) {
                    return processFromDetectOutput();
                } else {
                    return processFromBoxOutput(imageWidth, imageHeight, list);
                }
            case BOX:
            default:
                return processFromBoxOutput(imageWidth, imageHeight, list);
        }
    }

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) throws Exception {
        NDArray array = input.toNDArray(ctx.getNDManager(), flag);
        NDList list = pipeline.transform(new NDList(array));
        Shape shape = list.get(0).getShape();
        int processedWidth;
        int processedHeight;
        long[] dim = shape.getShape();
        if (NDImageUtils.isCHW(shape)) {
            processedWidth = (int) dim[dim.length - 1];
            processedHeight = (int) dim[dim.length - 2];
        } else {
            processedWidth = (int) dim[dim.length - 2];
            processedHeight = (int) dim[dim.length - 3];
        }
        ctx.setAttachment("width", input.getWidth());
        ctx.setAttachment("height", input.getHeight());
        ctx.setAttachment("processedWidth", processedWidth);
        ctx.setAttachment("processedHeight", processedHeight);
        return list;
    }

    protected DetectedObjects nms(
            int imageWidth,
            int imageHeight,
            List<Landmark> boxes,
            List<Integer> classIds,
            List<Float> scores) {
        List<String> retClasses = new ArrayList<>();
        List<Double> retProbs = new ArrayList<>();
        List<BoundingBox> retBB = new ArrayList<>();

        for (int classId = 0; classId < classes.size(); classId++) {
            List<Rectangle> r = new ArrayList<>();
            List<Double> s = new ArrayList<>();
            List<Integer> map = new ArrayList<>();
            for (int j = 0; j < classIds.size(); ++j) {
                if (classIds.get(j) == classId) {
                    r.add(boxes.get(j));
                    s.add(scores.get(j).doubleValue());
                    map.add(j);
                }
            }
            if (r.isEmpty()) {
                continue;
            }
            List<Integer> nms = Rectangle.nms(r, s, nmsThreshold);
            for (int index : nms) {
                int pos = map.get(index);
                int id = classIds.get(pos);
                retClasses.add(classes.get(id));
                retProbs.add(scores.get(pos).doubleValue());
//                Rectangle rect = boxes.get(pos);
                Landmark rect = boxes.get(pos);
                List<Point> keypoints = new ArrayList<>();
                if (removePadding) {
                    int padW = (width - imageWidth) / 2;
                    int padH = (height - imageHeight) / 2;
                    rect.getPath().forEach(point -> {
                        keypoints.add(new Point(point.getX() - padW, point.getY() - padH));
                    });
                    rect =
                            new Landmark(
                                    (rect.getX() - padW) / imageWidth,
                                    (rect.getY() - padH) / imageHeight,
                                    rect.getWidth() / imageWidth,
                                    rect.getHeight() / imageHeight,keypoints);
                } else if (applyRatio) {
                    rect.getPath().forEach(point -> {
                        keypoints.add(new Point(point.getX() / width, point.getY() / height));
                    });
                    rect =
                            new Landmark(
                                    rect.getX() / width,
                                    rect.getY() / height,
                                    rect.getWidth() / width,
                                    rect.getHeight() / height,keypoints);
                }
                retBB.add(rect);
            }
        }
        return new DetectedObjects(retClasses, retProbs, retBB);
    }

    public static class Builder {

        private int maxBox = 8400;

        YoloOutputType outputType;
        float nmsThreshold;

        protected float threshold = 0.2F;
        protected boolean applyRatio;
        protected boolean removePadding;

        protected int width = 224;
        protected int height = 224;
        protected Image.Flag flag;
        protected Pipeline pipeline;
        protected Batchifier batchifier;

        public Builder() {
            this.outputType = YoloOutputType.AUTO;
            this.nmsThreshold = 0.4F;
        }

        public Builder optOutputType(YoloOutputType outputType) {
            this.outputType = outputType;
            return this;
        }

        public Builder optNmsThreshold(float nmsThreshold) {
            this.nmsThreshold = nmsThreshold;
            return this;
        }

        /**
         * Builds the translator.
         *
         * @return the new translator
         */
        public YoloV8FaceTranslator build() {
            if (pipeline == null) {
                addTransform(
                        array -> array.transpose(2, 0, 1).toType(DataType.FLOAT32, false).div(255));
            }
//            validate();
            return new YoloV8FaceTranslator(this);
        }

        protected Builder self() {
            return this;
        }

        public Builder addTransform(Transform transform) {
            if (this.pipeline == null) {
                this.pipeline = new Pipeline();
            }

            this.pipeline.add(transform);
            return this.self();
        }

        public Builder optApplyRatio(boolean value) {
            this.applyRatio = value;
            return this.self();
        }

        public Builder optFlag(Image.Flag flag) {
            this.flag = flag;
            return this.self();
        }

        public Builder setPipeline(Pipeline pipeline) {
            this.pipeline = pipeline;
            return this.self();
        }

        public Builder setImageSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this.self();
        }


        public Builder optBatchifier(Batchifier batchifier) {
            this.batchifier = batchifier;
            return this.self();
        }

        public Builder optThreshold(float threshold) {
            this.threshold = threshold;
            return this.self();
        }

        /** {@inheritDoc} */
        protected void configPostProcess(Map<String, ?> arguments) {
            if (ArgumentsUtil.booleanValue(arguments, "optApplyRatio") || ArgumentsUtil.booleanValue(arguments, "applyRatio")) {
                this.optApplyRatio(true);
            }
            this.threshold = ArgumentsUtil.floatValue(arguments, "threshold", 0.2F);
            String centerFit = ArgumentsUtil.stringValue(arguments, "centerFit", "false");
            this.removePadding = "true".equals(centerFit);
            String type = ArgumentsUtil.stringValue(arguments, "outputType", "AUTO");
            this.outputType = YoloOutputType.valueOf(type.toUpperCase(Locale.ENGLISH));
            this.nmsThreshold = ArgumentsUtil.floatValue(arguments, "nmsThreshold", 0.4F);
            maxBox = ArgumentsUtil.intValue(arguments, "maxBox", 8400);
        }

        protected void configPreProcess(Map<String, ?> arguments) {
            if (this.pipeline == null) {
                this.pipeline = new Pipeline();
            }

            this.width = ArgumentsUtil.intValue(arguments, "width", 224);
            this.height = ArgumentsUtil.intValue(arguments, "height", 224);
            if (arguments.containsKey("flag")) {
                this.flag = Image.Flag.valueOf(arguments.get("flag").toString());
            }

            String pad = ArgumentsUtil.stringValue(arguments, "pad", "false");
            if ("true".equals(pad)) {
                this.addTransform(new Pad(0.0));
            } else if (!"false".equals(pad)) {
                double padding = Double.parseDouble(pad);
                this.addTransform(new Pad(padding));
            }

            String resize = ArgumentsUtil.stringValue(arguments, "resize", "false");
            int w;
            int shortEdge;
            if ("true".equals(resize)) {
                this.addTransform(new Resize(this.width, this.height));
            } else if (!"false".equals(resize)) {
                String[] tokens = resize.split("\\s*,\\s*");
                w = (int)Double.parseDouble(tokens[0]);
                if (tokens.length > 1) {
                    shortEdge = (int)Double.parseDouble(tokens[1]);
                } else {
                    shortEdge = w;
                }

                Image.Interpolation interpolation;
                if (tokens.length > 2) {
                    interpolation = Image.Interpolation.valueOf(tokens[2]);
                } else {
                    interpolation = Image.Interpolation.BILINEAR;
                }

                this.addTransform(new Resize(w, shortEdge, interpolation));
            }

            String resizeShort = ArgumentsUtil.stringValue(arguments, "resizeShort", "false");
            if ("true".equals(resizeShort)) {
                w = Math.max(this.width, this.height);
                this.addTransform(new ResizeShort(w));
            } else if (!"false".equals(resizeShort)) {
                String[] tokens = resizeShort.split("\\s*,\\s*");
                shortEdge = (int)Double.parseDouble(tokens[0]);
                int longEdge;
                if (tokens.length > 1) {
                    longEdge = (int)Double.parseDouble(tokens[1]);
                } else {
                    longEdge = -1;
                }

                Image.Interpolation interpolation;
                if (tokens.length > 2) {
                    interpolation = Image.Interpolation.valueOf(tokens[2]);
                } else {
                    interpolation = Image.Interpolation.BILINEAR;
                }

                this.addTransform(new ResizeShort(shortEdge, longEdge, interpolation));
            }

            if (ArgumentsUtil.booleanValue(arguments, "centerCrop", false)) {
                this.addTransform(new CenterCrop(this.width, this.height));
            }

            if (ArgumentsUtil.booleanValue(arguments, "centerFit")) {
                this.addTransform(new CenterFit(this.width, this.height));
            }

            if (ArgumentsUtil.booleanValue(arguments, "toTensor", true)) {
                this.addTransform(new ToTensor());
            }

            String normalize = ArgumentsUtil.stringValue(arguments, "normalize", "false");
            if ("true".equals(normalize)) {
                float[] MEAN = new float[]{0.485F, 0.456F, 0.406F};
                float[] STD = new float[]{0.229F, 0.224F, 0.225F};
                this.addTransform(new Normalize(MEAN, STD));
            } else if (!"false".equals(normalize)) {
                String[] tokens = normalize.split("\\s*,\\s*");
                if (tokens.length != 6) {
                    throw new IllegalArgumentException("Invalid normalize value: " + normalize);
                }

                float[] mean = new float[]{Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])};
                float[] std = new float[]{Float.parseFloat(tokens[3]), Float.parseFloat(tokens[4]), Float.parseFloat(tokens[5])};
                this.addTransform(new Normalize(mean, std));
            }

            String range = (String)arguments.get("range");
            if ("0,1".equals(range)) {
                this.addTransform((a) -> {
                    return a.div(255.0F);
                });
            } else if ("-1,1".equals(range)) {
                this.addTransform((a) -> {
                    return a.div(128.0F).sub(1);
                });
            }

            if (arguments.containsKey("batchifier")) {
                this.batchifier = Batchifier.fromString((String)arguments.get("batchifier"));
            }

        }
    }

    public static enum YoloOutputType {
        BOX,
        DETECT,
        AUTO;

        private YoloOutputType() {
        }
    }



}

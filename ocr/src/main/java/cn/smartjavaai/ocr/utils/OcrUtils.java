package cn.smartjavaai.ocr.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Landmark;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.common.utils.PointUtils;
import cn.smartjavaai.ocr.entity.*;
import cn.smartjavaai.ocr.entity.RotatedBox;
import cn.smartjavaai.ocr.enums.AngleEnum;
import cn.smartjavaai.ocr.enums.PlateType;
import cn.smartjavaai.ocr.opencv.OcrNDArrayUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/**
 * @author dwj
 * @date 2025/4/22
 */
@Slf4j
public class OcrUtils {


    /**
     * 转换为OcrBox
     * @param dt_boxes
     * @return
     */
    public static List<OcrBox> convertToOcrBox(NDList dt_boxes) {
        List<OcrBox> boxList = new ArrayList<>();
        for (NDArray box : dt_boxes) {
            float[] pointsArr = box.toFloatArray();
            OcrBox ocrBox = new OcrBox(
                    new Point(pointsArr[0], pointsArr[1]),
                    new Point(pointsArr[2], pointsArr[3]),
                    new Point(pointsArr[4], pointsArr[5]),
                    new Point(pointsArr[6], pointsArr[7])
            );
            boxList.add(ocrBox);
        }
        return boxList;
    }

    /**
     * 转换为OcrBox
     * @param ndLists
     * @return
     */
    public static List<List<OcrBox>> convertToOcrBox(List<NDList> ndLists) {
        if (ndLists == null || ndLists.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<OcrBox>> boxLists = new ArrayList<>();
        for (NDList dt_boxes : ndLists) {
            boxLists.add(convertToOcrBox(dt_boxes));
        }
        return boxLists;
    }




    /**
     * 图片旋转
     *
     * @param manager
     * @param image
     * @return
     */
    public static Image rotateImg(NDManager manager, Image image) {
        NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
        return ImageFactory.getInstance().fromNDArray(rotated);
    }

    /**
     * 逆时针旋转图片
     *
     * @param image
     * @param times
     * @return
     */
    public static Image rotateImg(Image image, int times) {
        try (NDManager manager = NDManager.newBaseManager()) {
            NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), times);
            return OpenCVImageFactory.getInstance().fromNDArray(rotated);
        }
    }


    /**
     * 逆时针旋转图片
     *
     * @param image
     * @param angleEnum
     * @return
     */
    public static Image rotateImg(Image image, AngleEnum angleEnum) {
        try (NDManager manager = NDManager.newBaseManager()) {
            int times = 0;
            switch (angleEnum) {
                case ANGLE_90:
                    times = 1;
                    break;
                case ANGLE_180:
                    times = 2;
                    break;
                case ANGLE_270:
                    times = 3;
                    break;
            }
            NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), times);
            return OpenCVImageFactory.getInstance().fromNDArray(rotated);
        }
    }


    /**
     * 转换为OcrInfo
     * @param lines
     * @return
     */
    public static OcrInfo convertToOcrInfo(List<ArrayList<RotatedBoxCompX>> lines){
        if(Objects.isNull(lines) || lines.size() == 0){
            return null;
        }
        List<List<OcrItem>> lineList = new ArrayList<List<OcrItem>>();
        String fullText = "";
        for(ArrayList<RotatedBoxCompX> boxList : lines){
            List<OcrItem> line = new ArrayList<OcrItem>();
            for(RotatedBoxCompX box : boxList){
                float[] pointsArr = box.getBox().toFloatArray();
                float[] lt = java.util.Arrays.copyOfRange(pointsArr, 0, 2);
                float[] rt = java.util.Arrays.copyOfRange(pointsArr, 2, 4);
                float[] rb = java.util.Arrays.copyOfRange(pointsArr, 4, 6);
                float[] lb = java.util.Arrays.copyOfRange(pointsArr, 6, 8);
                OcrBox ocrBox = new OcrBox(new Point(lt[0], lt[1]), new Point(rt[0], rt[1]), new Point(rb[0], rb[1]), new Point(lb[0], lb[1]));
                OcrItem ocrItem = new OcrItem(ocrBox, box.getText());
                line.add(ocrItem);
                String text = box.getText();
                if(text.trim().equals(""))
                    continue;
                fullText += text + " ";
            }
            lineList.add(line);
            fullText += '\n';
        }
        return new OcrInfo(lineList, fullText);
    }

    public static OcrInfo convertRotatedBoxesToOcrItems(List<RotatedBox> rotatedBoxes) {
        OcrInfo ocrInfo = new OcrInfo();
        List<OcrItem> ocrItems = new ArrayList<>();
        StringBuilder fullText = new StringBuilder();
        for (RotatedBox rotatedBox : rotatedBoxes) {
            NDArray box = rotatedBox.getBox();
            float[] points = box.toFloatArray();
            Point topLeft = new Point(points[0], points[1]);
            Point topRight = new Point(points[2], points[3]);
            Point bottomRight = new Point(points[4], points[5]);
            Point bottomLeft = new Point(points[6], points[7]);

            OcrBox ocrBox = new OcrBox(topLeft, topRight, bottomRight, bottomLeft);
            String text = rotatedBox.getText();

            OcrItem item = new OcrItem();
            item.setOcrBox(ocrBox);
            item.setText(text);
            ocrItems.add(item);
            fullText.append(text + " ");
        }
        if (fullText.length() > 0) {
            fullText.deleteCharAt(fullText.length() - 1);
        }
        ocrInfo.setOcrItemList(ocrItems);
        ocrInfo.setFullText(fullText.toString());
        return ocrInfo;
    }


    /**
     * 透视变换 + 裁剪
     * @param srcMat
     * @param landMarks
     * @return
     */
    public static Image transformAndCrop(Mat srcMat, List<ai.djl.modality.cv.output.Point> landMarks){
        if (landMarks == null || landMarks.size() != 4) {
            throw new IllegalArgumentException("必须提供4个关键点");
        }

        // 步骤 1：排序为 左上、右上、右下、左下
        List<ai.djl.modality.cv.output.Point> ordered = PointUtils.orderPoints(landMarks);

        ai.djl.modality.cv.output.Point lt = ordered.get(0);
        ai.djl.modality.cv.output.Point rt = ordered.get(1);
        ai.djl.modality.cv.output.Point rb = ordered.get(2);
        ai.djl.modality.cv.output.Point lb = ordered.get(3);

        // 步骤 2：计算目标图像尺寸（宽、高）
        int img_crop_width = (int) Math.max(
                PointUtils.distance(lt, rt),
                PointUtils.distance(rb, lb)
        );
        int img_crop_height = (int) Math.max(
                PointUtils.distance(lt, lb),
                PointUtils.distance(rt, rb)
        );

        // 步骤 3：构造目标坐标点
        List<ai.djl.modality.cv.output.Point> dstPoints = Arrays.asList(
                new ai.djl.modality.cv.output.Point(0, 0),
                new ai.djl.modality.cv.output.Point(img_crop_width, 0),
                new ai.djl.modality.cv.output.Point(img_crop_width, img_crop_height),
                new ai.djl.modality.cv.output.Point(0, img_crop_height)
        );

        // 步骤 4：透视变换
        Mat srcPoint2f = OcrNDArrayUtils.toMat(ordered);
        Mat dstPoint2f = OcrNDArrayUtils.toMat(dstPoints);
        Mat cvMat = OpenCVUtils.perspectiveTransform(srcMat, srcPoint2f, dstPoint2f);

        // 步骤 5：转为 DJL Image + 裁剪
        Image subImg = OpenCVImageFactory.getInstance().fromImage(cvMat);
        subImg = subImg.getSubImage(0, 0, img_crop_width, img_crop_height);

        // 释放资源
        cvMat.release();
        srcPoint2f.release();
        dstPoint2f.release();

        return subImg;
    }



    /**
     * 透视变换+裁剪
     * @param srcMat
     * @param box
     * @return
     */
    public static Image transformAndCrop(Mat srcMat, OcrBox box){
        float[] pointsArr = box.toFloatArray();
        float[] lt = java.util.Arrays.copyOfRange(pointsArr, 0, 2);
        float[] rt = java.util.Arrays.copyOfRange(pointsArr, 2, 4);
        float[] rb = java.util.Arrays.copyOfRange(pointsArr, 4, 6);
        float[] lb = java.util.Arrays.copyOfRange(pointsArr, 6, 8);
        int img_crop_width = (int) Math.max(PointUtils.distance(lt, rt), PointUtils.distance(rb, lb));
        int img_crop_height = (int) Math.max(PointUtils.distance(lt, lb), PointUtils.distance(rt, rb));
        List<ai.djl.modality.cv.output.Point> srcPoints = new ArrayList<>();
        srcPoints.add(new ai.djl.modality.cv.output.Point(lt[0], lt[1]));
        srcPoints.add(new ai.djl.modality.cv.output.Point(rt[0], rt[1]));
        srcPoints.add(new ai.djl.modality.cv.output.Point(rb[0], rb[1]));
        srcPoints.add(new ai.djl.modality.cv.output.Point(lb[0], lb[1]));
        List<ai.djl.modality.cv.output.Point> dstPoints = new ArrayList<>();
        dstPoints.add(new ai.djl.modality.cv.output.Point(0, 0));
        dstPoints.add(new ai.djl.modality.cv.output.Point(img_crop_width, 0));
        dstPoints.add(new ai.djl.modality.cv.output.Point(img_crop_width, img_crop_height));
        dstPoints.add(new ai.djl.modality.cv.output.Point(0, img_crop_height));
        Mat srcPoint2f = OcrNDArrayUtils.toMat(srcPoints);
        Mat dstPoint2f = OcrNDArrayUtils.toMat(dstPoints);
        //透视变换
        Mat cvMat = OpenCVUtils.perspectiveTransform(srcMat, srcPoint2f, dstPoint2f);
        Image subImg = OpenCVImageFactory.getInstance().fromImage(cvMat);
        //ImageUtils.saveImage(subImg, i + ".png", "build/output");
        //变换后裁剪
        subImg = subImg.getSubImage(0, 0, img_crop_width, img_crop_height);
        cvMat.release();
        srcPoint2f.release();
        dstPoint2f.release();
        return subImg;
    }

    /**
     * 绘制文本框
     *
     * @param mat
     * @param boxList
     */
    public static void drawRect(Mat mat, List<OcrBox> boxList) {
        for(OcrBox ocrBox : boxList){
            Imgproc.line(mat, ocrBox.getTopLeft().toCvPoint(), ocrBox.getTopRight().toCvPoint(), new Scalar(0, 255, 0), 1);
            Imgproc.line(mat, ocrBox.getTopRight().toCvPoint(), ocrBox.getBottomRight().toCvPoint(), new Scalar(0, 255, 0),1);
            Imgproc.line(mat, ocrBox.getBottomRight().toCvPoint(), ocrBox.getBottomLeft().toCvPoint(), new Scalar(0, 255, 0),1);
            Imgproc.line(mat, ocrBox.getBottomLeft().toCvPoint(), ocrBox.getTopLeft().toCvPoint(), new Scalar(0, 255, 0), 1);
        }
    }


    /**
     * 绘制文本框及文本
     * @param image
     * @param ocrInfo
     */
    public static void drawRectWithText(BufferedImage image, OcrInfo ocrInfo,  int fontSize) {
        // 将绘制图像转换为Graphics2D
        Graphics2D g = (Graphics2D) image.getGraphics();
        try {
            Font font = new Font("楷体", Font.PLAIN, fontSize);
            g.setFont(font);
            g.setColor(new Color(0, 0, 255));
            // 声明画笔属性 ：粗 细（单位像素）末端无修饰 折线处呈尖角
            BasicStroke bStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(bStroke);
            List<OcrItem> ocrItemList = ocrInfo.getOcrItemList();
            if(CollectionUtils.isNotEmpty(ocrInfo.getLineList())){
                ocrItemList = ocrInfo.flattenLines();
            }
            for(OcrItem item : ocrItemList){
                OcrBox box = item.getOcrBox();
                int[] xPoints = {
                        (int)box.getTopLeft().getX(),
                        (int)box.getTopRight().getX(),
                        (int)box.getBottomRight().getX(),
                        (int)box.getBottomLeft().getX(),
                        (int)box.getTopLeft().getX()
                };
                int[] yPoints = {
                        (int)box.getTopLeft().getY(),
                        (int)box.getTopRight().getY(),
                        (int)box.getBottomRight().getY(),
                        (int)box.getBottomLeft().getY(),
                        (int)box.getTopLeft().getY()
                };
                g.drawPolyline(xPoints, yPoints, 5);
                g.drawString(item.getText(), xPoints[0], yPoints[0]);
            }
        } finally {
            g.dispose();
        }
    }


    /**
     * 绘制文本框及文本
     * @param srcMat
     * @param itemList
     */
    public static void drawRectWithText(Mat srcMat, List<OcrItem> itemList) {
        for(OcrItem item : itemList){
            OcrBox ocrBox = item.getOcrBox();
            Imgproc.line(srcMat, ocrBox.getTopLeft().toCvPoint(), ocrBox.getTopRight().toCvPoint(), new Scalar(0, 255, 0), 1);
            Imgproc.line(srcMat, ocrBox.getTopRight().toCvPoint(), ocrBox.getBottomRight().toCvPoint(), new Scalar(0, 255, 0),1);
            Imgproc.line(srcMat, ocrBox.getBottomRight().toCvPoint(), ocrBox.getBottomLeft().toCvPoint(), new Scalar(0, 255, 0),1);
            Imgproc.line(srcMat, ocrBox.getBottomLeft().toCvPoint(), ocrBox.getTopLeft().toCvPoint(), new Scalar(0, 255, 0), 1);
            // 中文乱码
            Imgproc.putText(srcMat, item.getAngle().getValue(), ocrBox.getTopLeft().toCvPoint(), Imgproc.FONT_HERSHEY_SCRIPT_SIMPLEX, 1.0, new Scalar(0, 255, 0), 1);
        }
    }

    public static List<PlateInfo> convertToPlateInfo(DetectedObjects detectedObjects, Image image) {
        List<PlateInfo> plateInfoList = new ArrayList<>();
        Iterator iterator = detectedObjects.items().iterator();
        int index = 0;
        while(iterator.hasNext()) {
            DetectedObjects.DetectedObject result = (DetectedObjects.DetectedObject)iterator.next();
            BoundingBox box = result.getBoundingBox();
            List<Point> keyPoints = new ArrayList<Point>();
            if(box instanceof Landmark){
                box.getBounds().getPath().forEach(point -> {
                    keyPoints.add(new Point(point.getX(), point.getY()));
                });
            }
            int x = (int)(box.getBounds().getX() * image.getWidth());
            int y = (int)(box.getBounds().getY() * image.getHeight());
            int width = (int)(box.getBounds().getWidth() * image.getWidth());
            int height = (int)(box.getBounds().getHeight() * image.getHeight());
            // 修正边界，防止越界
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x + width > image.getWidth()) width = image.getWidth() - x;
            if (y + height > image.getHeight()) height = image.getHeight() - y;
            PlateInfo plateInfo = new PlateInfo();
            plateInfo.setPlateType(PlateType.fromClassName(detectedObjects.getClassNames().get(index)));
            plateInfo.setScore(detectedObjects.getProbabilities().get(index).floatValue());
            plateInfo.setDetectionRectangle(new DetectionRectangle(x, y, width, height));
            OcrBox ocrBox = new OcrBox(keyPoints.get(0), keyPoints.get(1), keyPoints.get(2), keyPoints.get(3));
            plateInfo.setBox(ocrBox);
            plateInfoList.add(plateInfo);
            index++;
        }
        return plateInfoList;
    }

    /**
     * 绘制车牌信息
     * @param srcMat
     * @param plateInfoList
     */
    public static void drawPlateInfo(Mat srcMat, List<PlateInfo> plateInfoList) {
        for(PlateInfo plateInfo : plateInfoList){
            OcrBox ocrBox = plateInfo.getBox();
            Imgproc.line(srcMat, ocrBox.getTopLeft().toCvPoint(), ocrBox.getTopRight().toCvPoint(), new Scalar(0, 0, 255), 1);
            Imgproc.line(srcMat, ocrBox.getTopRight().toCvPoint(), ocrBox.getBottomRight().toCvPoint(), new Scalar(0, 0, 255),1);
            Imgproc.line(srcMat, ocrBox.getBottomRight().toCvPoint(), ocrBox.getBottomLeft().toCvPoint(), new Scalar(0, 0, 255),1);
            Imgproc.line(srcMat, ocrBox.getBottomLeft().toCvPoint(), ocrBox.getTopLeft().toCvPoint(), new Scalar(0, 0, 255), 1);
            // 中文乱码
            ImageUtils.putTextWithBackground(srcMat, plateInfo.getPlateNumber() + " " + plateInfo.getPlateColor(), ocrBox.getTopLeft().toCvPoint(), new Scalar(255, 255, 255), new Scalar(0, 0, 0), 1);
        }
    }

    /**
     * 在图像上绘制带白色背景、黑色文字的文本
     */
    public static void drawPlateInfo(BufferedImage image, List<PlateInfo> plateInfoList) {
        // 将绘制图像转换为Graphics2D
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        try {
            graphics.setColor(Color.RED);// 边框颜色
            graphics.setStroke(new BasicStroke(2));   // 线宽2像素
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
            int stroke = 2;
            for(PlateInfo plateInfo : plateInfoList){
                DetectionRectangle rectangle = plateInfo.getDetectionRectangle();
                graphics.setColor(Color.RED);// 边框颜色
                //绘制车牌框
                graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
                graphics.setColor(Color.BLACK);// 字体颜色
                ImageUtils.drawText(graphics, plateInfo.getPlateNumber() + " " + plateInfo.getPlateColor(), rectangle.getX(), rectangle.getY(), stroke, 4);
                OcrBox ocrBox = plateInfo.getBox();
                //绘制关键点
                graphics.setColor(Color.BLUE);
                graphics.drawRect((int)ocrBox.getTopLeft().getX(), (int)ocrBox.getTopLeft().getY(), 2, 2);
                graphics.setColor(Color.GREEN);
                graphics.drawRect((int)ocrBox.getTopRight().getX(), (int)ocrBox.getTopRight().getY(), 2, 2);
                graphics.setColor(Color.RED);
                graphics.drawRect((int)ocrBox.getBottomLeft().getX(), (int)ocrBox.getBottomLeft().getY(), 2, 2);
                graphics.setColor(Color.CYAN);
                graphics.drawRect((int)ocrBox.getBottomRight().getX(), (int)ocrBox.getBottomRight().getY(), 2, 2);
            }
        } finally {
            graphics.dispose();
        }
    }


}

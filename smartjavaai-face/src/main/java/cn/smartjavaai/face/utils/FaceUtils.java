package cn.smartjavaai.face.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.FaceModelConfig;
import cn.smartjavaai.face.exception.FaceException;
import com.seeta.sdk.SeetaImageData;
import com.seeta.sdk.SeetaPointF;
import com.seeta.sdk.SeetaRect;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人脸检测相关工具类
 * @author dwj
 * @date 2025/4/9
 */
public class FaceUtils {


    /**
     * 转换为FaceDetectedResult
     * @param detection
     * @param img
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(DetectedObjects detection, Image img){
        if(Objects.isNull(detection) || Objects.isNull(detection.getProbabilities())
                || detection.getProbabilities().isEmpty() || Objects.isNull(detection.items()) || detection.items().isEmpty()){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectedObjects.DetectedObject> detectedObjectList = detection.items();
        List<DetectionRectangle> rectangleList = new ArrayList<DetectionRectangle>();
        Iterator iterator = detectedObjectList.iterator();
        int index = 0;
        while(iterator.hasNext()) {
            DetectedObjects.DetectedObject result = (DetectedObjects.DetectedObject)iterator.next();
            BoundingBox box = result.getBoundingBox();
            List<Point> keyPoints = new ArrayList<Point>();
            box.getBounds().getPath().forEach(point -> {
                keyPoints.add(new Point(point.getX(), point.getY()));
            });
            int x = (int)(box.getBounds().getX() * img.getWidth());
            int y = (int)(box.getBounds().getY() * img.getHeight());
            int width = (int)(box.getBounds().getWidth() * img.getWidth());
            int height = (int)(box.getBounds().getHeight() * img.getHeight());
            // 修正边界，防止越界
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x + width > img.getWidth()) width = img.getWidth() - x;
            if (y + height > img.getHeight()) height = img.getHeight() - y;
            DetectionRectangle rectangle = new DetectionRectangle(x, y, width, height, detection.getProbabilities().get(index).floatValue());
            rectangle.setKeyPoints(keyPoints);
            rectangleList.add(rectangle);
            index++;
        }
        detectionResponse.setRectangleList(rectangleList);
        return detectionResponse;
    }

    /**
     * 转换为FaceDetectedResult
     * @param seetaResult
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(SeetaRect[] seetaResult, FaceModelConfig config,List<SeetaPointF[]> seetaPointFSList){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionRectangle> rectangleList = new ArrayList<DetectionRectangle>();
        for(int i = 0; i < seetaResult.length; i++){
            SeetaRect rect  = seetaResult[i];
            SeetaPointF[] seetaPointFS = seetaPointFSList.get(i);
            //过滤置信度
            /*if(config.getConfidenceThreshold() > 0){
                continue;
            }*/
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height, 0);
            List<Point> keyPoints = Arrays.stream(seetaPointFS)
                    .map(p -> new Point(p.x, p.y))
                    .collect(Collectors.toList());
            rectangle.setKeyPoints(keyPoints);
            rectangleList.add(rectangle);
        }
        detectionResponse.setRectangleList(rectangleList);
        return detectionResponse;
    }

    /**
     * 绘制人脸框
     * @param sourceImage
     * @param detectionResponse
     * @param savePath
     * @throws IOException
     */
    public static void drawBoundingBoxes(BufferedImage sourceImage, DetectionResponse detectionResponse, String savePath) throws IOException {
        if(!ImageUtils.isImageValid(sourceImage)){
            throw new FaceException("图像无效");
        }
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getRectangleList()) || detectionResponse.getRectangleList().isEmpty()){
            throw new FaceException("无目标数据");
        }
        Graphics2D graphics = sourceImage.createGraphics();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        for(DetectionRectangle rectangle : detectionResponse.getRectangleList()){
            graphics.setColor(Color.RED);// 边框颜色
            graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),  rectangle.getHeight());
            drawText(graphics, "face", rectangle.getX(), rectangle.getY(), stroke, 4);
            //绘制人脸关键点
            if(rectangle.getKeyPoints() != null){
                drawLandmarks(graphics, rectangle.getKeyPoints());
            }
        }
        graphics.dispose();
        ImageIO.write(sourceImage, "jpg", new File(savePath));
    }

    /**
     * 绘制人脸框
     * @param sourceImage
     * @param detectionResponse
     * @throws IOException
     */
    public static BufferedImage drawBoundingBoxes(BufferedImage sourceImage, DetectionResponse detectionResponse) throws IOException {
        if(!ImageUtils.isImageValid(sourceImage)){
            throw new FaceException("图像无效");
        }
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getRectangleList()) || detectionResponse.getRectangleList().isEmpty()){
            throw new FaceException("无目标数据");
        }
        Graphics2D graphics = sourceImage.createGraphics();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        for(DetectionRectangle rectangle : detectionResponse.getRectangleList()){
            graphics.setColor(Color.RED);// 边框颜色
            graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),  rectangle.getHeight());
            drawText(graphics, "face", rectangle.getX(), rectangle.getY(), stroke, 4);
            //绘制人脸关键点
            if(rectangle.getKeyPoints() != null){
                drawLandmarks(graphics, rectangle.getKeyPoints());
            }
        }
        graphics.dispose();
        return sourceImage;
    }

    /**
     * 绘制文字
     * @param g
     * @param text
     * @param x
     * @param y
     * @param stroke
     * @param padding
     */
    private static void drawText(Graphics2D g, String text, int x, int y, int stroke, int padding) {
        FontMetrics metrics = g.getFontMetrics();
        x += stroke / 2;
        y += stroke / 2;
        int width = metrics.stringWidth(text) + padding * 2 - stroke / 2;
        int height = metrics.getHeight() + metrics.getDescent();
        int ascent = metrics.getAscent();
        java.awt.Rectangle background = new java.awt.Rectangle(x, y, width, height);
        g.fill(background);
        g.setPaint(Color.WHITE);
        g.drawString(text, x + padding, y + ascent);
    }

    /**
     * 修正检测框
     * @param rectangle
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static DetectionRectangle correctRect(DetectionRectangle rectangle, int imageWidth, int imageHeight) {
        int x = rectangle.getX();
        int y = rectangle.getY();
        int width = rectangle.getWidth();
        int height = rectangle.getHeight();
        // 修正x, y防止越界
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        // 宽高不能超出图片范围
        if (x + width > imageWidth) {
            width = imageWidth - x;
        }
        if (y + height > imageHeight) {
            height = imageHeight - y;
        }
        // 防止最终 width 或 height 为负或为 0
        if (width <= 0 || height <= 0) {
            return null; // 无效区域
        }
        return new DetectionRectangle(x, y, width, height, rectangle.score);
    }

    /**
     * 子图中人脸关键点坐标 - Coordinates of key points in the image
     *
     * @param points
     * @return
     */
    public static double[][] facePoints(List<Point> points) {
        //      图中关键点坐标 - Coordinates of key points in the image
        //      1.  left_eye_x , left_eye_y
        //      2.  right_eye_x , right_eye_y
        //      3.  nose_x , nose_y
        //      4.  left_mouth_x , left_mouth_y
        //      5.  right_mouth_x , right_mouth_y
        double[][] pointsArray = new double[5][2]; // 保存人脸关键点 - Save facial key points
        int i = 0;
        for (Point point : points) {
            pointsArray[i][0] = point.getX();
            pointsArray[i][1] = point.getY();
            i++;
        }
        return pointsArray;
    }

    /**
     * 子图中人脸关键点坐标 - Coordinates of key points in the image
     *
     * @param pointFS
     * @return
     */
    public static double[][] facePoints(SeetaPointF[] pointFS) {
        //      图中关键点坐标 - Coordinates of key points in the image
        //      1.  left_eye_x , left_eye_y
        //      2.  right_eye_x , right_eye_y
        //      3.  nose_x , nose_y
        //      4.  left_mouth_x , left_mouth_y
        //      5.  right_mouth_x , right_mouth_y
        double[][] pointsArray = new double[5][2]; // 保存人脸关键点 - Save facial key points
        int i = 0;
        for (SeetaPointF point : pointFS) {
            pointsArray[i][0] = point.getX();
            pointsArray[i][1] = point.getY();
            i++;
        }
        return pointsArray;
    }

    /**
     * 512x512的目标点 - Target point of 512x512
     * standard 5 landmarks for FFHQ faces with 512 x 512
     *
     * @param manager
     * @return
     */
    public static NDArray faceTemplate512x512(NDManager manager) {
        double[][] coord5point = {
                {192.98138, 239.94708}, // 512x512的目标点 - Target point of 512x512
                {318.90277, 240.1936},
                {256.63416, 314.01935},
                {201.26117, 371.41043},
                {313.08905, 371.15118}
        };
        NDArray points = manager.create(coord5point);
        return points;
    }

    /**
     * bgr转图片
     * @return 图片
     */
    public static BufferedImage toBufferedImage(SeetaImageData seetaImageData) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(seetaImageData.width, seetaImageData.height, type);
        image.getRaster().setDataElements(0, 0, seetaImageData.width, seetaImageData.height, seetaImageData.data);
        return image;
    }

    /**
     * 绘制人脸关键点
     * @param g
     * @param keyPoints
     */
    private static void drawLandmarks(Graphics2D g, List<Point> keyPoints) {
        g.setColor(new Color(246, 96, 0));
        BasicStroke bStroke = new BasicStroke(4.0F, 0, 0);
        g.setStroke(bStroke);
        for (Point point : keyPoints){
            g.drawRect((int)point.getX(), (int)point.getY(), 2, 2);
        }
    }


}

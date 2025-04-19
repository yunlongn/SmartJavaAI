package cn.smartjavaai.face.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.FaceModelConfig;
import cn.smartjavaai.face.exception.FaceException;
import com.seeta.sdk.SeetaRect;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
            int x = (int)(box.getBounds().getX() * (double)img.getWidth());
            int y = (int)(box.getBounds().getY() * (double)img.getHeight());
            int width = (int)(box.getBounds().getWidth() * (double)img.getWidth());
            int height = (int)(box.getBounds().getHeight() * (double)img.getHeight());
            DetectionRectangle rectangle = new DetectionRectangle(x, y, width, height, detection.getProbabilities().get(index).floatValue());
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
    public static DetectionResponse convertToDetectionResponse(SeetaRect[] seetaResult, FaceModelConfig config){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionRectangle> rectangleList = new ArrayList<DetectionRectangle>();
        for(SeetaRect rect : seetaResult){
            //过滤置信度
            /*if(config.getConfidenceThreshold() > 0){
                continue;
            }*/
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height, 0);
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


}

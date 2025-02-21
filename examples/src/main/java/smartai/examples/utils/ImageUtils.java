package smartai.examples.utils;

import cn.smartjavaai.face.FaceDetectedResult;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author dwj
 */
public class ImageUtils {

    /**
     * 绘制人脸框
     * @param sourceImage
     * @param faceDetectedResult
     * @param savePath
     * @throws IOException
     */
    public static void drawBoundingBoxes(BufferedImage sourceImage, FaceDetectedResult faceDetectedResult,String savePath) throws IOException {
        Graphics2D graphics = sourceImage.createGraphics();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        for(cn.smartjavaai.common.entity.Rectangle rectangle : faceDetectedResult.getRectangles()){
            graphics.setColor(Color.RED);// 边框颜色
            graphics.drawRect(rectangle.getPointList().get(0).getX(),
                    rectangle.getPointList().get(0).getY(), rectangle.getWidth(),  rectangle.getHeight());
            drawText(graphics, "face", rectangle.getPointList().get(0).getX(), rectangle.getPointList().get(0).getY(), stroke, 4);
        }
        graphics.dispose();
        ImageIO.write(sourceImage, "jpg", new File(savePath));

    }

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

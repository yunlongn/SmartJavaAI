package cn.smartjavaai.common.utils;

import ai.djl.modality.cv.BufferedImageFactory;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDArray;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
//import java.awt.image.ColorConvertOp;
import java.awt.image.ComponentSampleModel;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 图片处理工具类
 */
public class ImageUtils {
    /**
     * @param image
     * @param bandOffset 用于推断通道顺序
     * @return
     */
    private static boolean equalBandOffsetWith3Byte(BufferedImage image, int[] bandOffset) {
        if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            if (image.getData().getSampleModel() instanceof ComponentSampleModel) {
                ComponentSampleModel sampleModel = (ComponentSampleModel) image.getData().getSampleModel();
                if (Arrays.equals(sampleModel.getBandOffsets(), bandOffset)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 推断图像是否为BGR格式
     *
     * @return
     */
    public static boolean isBGR3Byte(BufferedImage image) {
        return equalBandOffsetWith3Byte(image, new int[]{0, 1, 2});
    }

    /**
     * 对图像解码返回BGR格式矩阵数据
     *
     * @param image
     * @return
     */
    public static byte[] getMatrixBGR(BufferedImage image) {
        byte[] matrixBGR;
        if (isBGR3Byte(image)) {
            matrixBGR = (byte[]) image.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
        } else {
            // ARGB格式图像数据
            int intrgb[] = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            matrixBGR = new byte[image.getWidth() * image.getHeight() * 3];
            // ARGB转BGR格式
            for (int i = 0, j = 0; i < intrgb.length; ++i, j += 3) {
                matrixBGR[j] = (byte) (intrgb[i] & 0xff);
                matrixBGR[j + 1] = (byte) ((intrgb[i] >> 8) & 0xff);
                matrixBGR[j + 2] = (byte) ((intrgb[i] >> 16) & 0xff);
            }
        }
        return matrixBGR;
    }


    public static BufferedImage bgrToBufferedImage(byte[] data, int width, int height) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        // bgr to rgb
        byte b;
        for (int i = 0; i < data.length; i = i + 3) {
            b = data[i];
            data[i] = data[i + 2];
            data[i + 2] = b;
        }
        BufferedImage image = new BufferedImage(width, height, type);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    /**
     * 检查图像是否有效
     * @param image
     * @return
     */
    public static boolean isImageValid(BufferedImage image) {
        // 检查是否为 null 或尺寸异常（如宽高为0）
        return image != null && image.getWidth() > 0 && image.getHeight() > 0;
    }

    /**
     * 画检测框
     *
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public static void drawImageRect(BufferedImage image, int x, int y, int width, int height) {
        // 将绘制图像转换为Graphics2D
        Graphics2D g = (Graphics2D) image.getGraphics();
        try {
            g.setColor(new Color(0, 255, 0));
            // 声明画笔属性 ：粗 细（单位像素）末端无修饰 折线处呈尖角
            BasicStroke bStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(bStroke);
            g.drawRect(x, y, width, height);
        } finally {
            g.dispose();
        }
    }

    /**
     * 画检测框
     *
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public static void drawImageRect(Image image, int x, int y, int width, int height) {
        // 将绘制图像转换为Graphics2D
        BufferedImage bufferedImage = (BufferedImage)image.getWrappedImage();
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        try {
            g.setColor(new Color(0, 255, 0));
            // 声明画笔属性 ：粗 细（单位像素）末端无修饰 折线处呈尖角
            BasicStroke bStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(bStroke);
            g.drawRect(x, y, width, height);
        } finally {
            g.dispose();
        }
    }


    /**
     * 保存BufferedImage图片
     *
     * @param img
     * @param name
     * @param path
     */
    public static void saveImage(BufferedImage img, String name, String path) {
        Mat mat = OpenCVUtils.image2Mat(img);
        Image djlImg = ImageFactory.getInstance().fromImage(mat); // 支持多种图片格式，自动适配
        Path outputDir = Paths.get(path);
        Path imagePath = outputDir.resolve(name);
        // OpenJDK 不能保存 jpg 图片的 alpha channel
        try {
            djlImg.save(Files.newOutputStream(imagePath), "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mat.release();
    }


    /**
     * 保存BufferedImage图片
     *
     * @param img
     * @param path
     */
    public static void saveImage(BufferedImage img, String path) {
        Mat mat = OpenCVUtils.image2Mat(img);
        Image djlImg = ImageFactory.getInstance().fromImage(mat); // 支持多种图片格式，自动适配
        Path outputDir = Paths.get(path);
        // OpenJDK 不能保存 jpg 图片的 alpha channel
        try {
            djlImg.save(Files.newOutputStream(outputDir), "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mat.release();
    }







    /**
     * 保存DJL图片
     *
     * @param img
     * @param name
     * @param path
     */
    public static void saveImage(Image img, String name, String path) {
        Path outputDir = Paths.get(path);
        Path imagePath = outputDir.resolve(name);
        // OpenJDK 不能保存 jpg 图片的 alpha channel
        try {
            img.save(Files.newOutputStream(imagePath), "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片,含检测框
     *
     * @param img
     * @param detection
     * @param name
     * @param path
     * @throws IOException
     */
    public static void saveBoundingBoxImage(
            Image img, DetectedObjects detection, String name, String path) throws IOException {
        // Make image copy with alpha channel because original image was jpg
        img.drawBoundingBoxes(detection);
        Path outputDir = Paths.get(path);
        Files.createDirectories(outputDir);
        Path imagePath = outputDir.resolve(name);
        // OpenJDK can't save jpg with alpha channel
        img.save(Files.newOutputStream(imagePath), "png");
    }




    /**
     * 画检测框(有倾斜角)
     *
     * @param image
     * @param box
     */
    public static void drawImageRect(BufferedImage image, NDArray box) {
        float[] points = box.toFloatArray();
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];

        for (int i = 0; i < 4; i++) {
            xPoints[i] = (int) points[2 * i];
            yPoints[i] = (int) points[2 * i + 1];
        }
        xPoints[4] = xPoints[0];
        yPoints[4] = yPoints[0];

        // 将绘制图像转换为Graphics2D
        Graphics2D g = (Graphics2D) image.getGraphics();
        try {
            g.setColor(new Color(0, 255, 0));
            // 声明画笔属性 ：粗 细（单位像素）末端无修饰 折线处呈尖角
            BasicStroke bStroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(bStroke);
            g.drawPolyline(xPoints, yPoints, 5); // xPoints, yPoints, nPoints
        } finally {
            g.dispose();
        }
    }

    /**
     * 画检测框(有倾斜角)和文本
     *
     * @param image
     * @param box
     * @param text
     */
    public static void drawImageRectWithText(BufferedImage image, NDArray box, String text) {
        float[] points = box.toFloatArray();
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];

        for (int i = 0; i < 4; i++) {
            xPoints[i] = (int) points[2 * i];
            yPoints[i] = (int) points[2 * i + 1];
        }
        xPoints[4] = xPoints[0];
        yPoints[4] = yPoints[0];

        // 将绘制图像转换为Graphics2D
        Graphics2D g = (Graphics2D) image.getGraphics();
        try {
            int fontSize = 32;
            Font font = new Font("楷体", Font.PLAIN, fontSize);
            g.setFont(font);
            g.setColor(new Color(0, 0, 255));
            // 声明画笔属性 ：粗 细（单位像素）末端无修饰 折线处呈尖角
            BasicStroke bStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(bStroke);
            g.drawPolyline(xPoints, yPoints, 5); // xPoints, yPoints, nPoints
            g.drawString(text, xPoints[0], yPoints[0]);
        } finally {
            g.dispose();
        }
    }



    /**
     * 显示文字
     *
     * @param image
     * @param text
     * @param x
     * @param y
     */
    public static void drawImageText(BufferedImage image, String text, int x, int y) {
        Graphics graphics = image.getGraphics();
        int fontSize = 32;
        Font font = new Font("楷体", Font.PLAIN, fontSize);
        try {
            graphics.setFont(font);
            graphics.setColor(new Color(0, 0, 255));
            int strWidth = graphics.getFontMetrics().stringWidth(text);
            graphics.drawString(text, x, y);
        } finally {
            graphics.dispose();
        }
    }


    /**
     * 画检测框(有倾斜角)和文本
     *
     * @param image
     * @param box
     * @param text
     */
    public static void drawImageRectWithText(BufferedImage image, DetectionRectangle box, String text, Color color) {
        // 将绘制图像转换为Graphics2D
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        try {
            graphics.setColor(Color.RED);// 边框颜色
            graphics.setStroke(new BasicStroke(2));   // 线宽2像素
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
            int stroke = 2;
            graphics.setColor(color);// 边框颜色
            graphics.drawRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
            drawText(graphics, text, box.getX(), box.getY(), stroke, 4);
            graphics.dispose();
        } finally {
            graphics.dispose();
        }
    }

    public static void drawText(Graphics2D g, String text, int x, int y, int stroke, int padding) {
        FontMetrics metrics = g.getFontMetrics();
        x += stroke / 2;
        y += stroke / 2;
        int width = metrics.stringWidth(text) + padding * 2 - stroke / 2;
        int height = metrics.getHeight() + metrics.getDescent();
        int ascent = metrics.getAscent();
        y = Math.max(0, y - height);
        java.awt.Rectangle background = new java.awt.Rectangle(x, y, width, height);
        g.fill(background);
        g.setPaint(Color.WHITE);
        g.drawString(text, x + padding, y + ascent);
    }

    /**
     * 计算左上角，右下角坐标 （x0,y0,x1,y1）
     * Get absolute coordinations
     *
     * @param rect
     * @param width
     * @param height
     * @return
     */
    public static int[] rectXYXY(ai.djl.modality.cv.output.Rectangle rect, int width, int height) {
        int left = Math.max((int) (width * rect.getX()), 0);
        int top = Math.max((int) (height * rect.getY()), 0);
        int right = Math.min((int) (width * (rect.getX() + rect.getWidth())), width - 1);
        int bottom = Math.min((int) (height * (rect.getY() + rect.getHeight())), height - 1);
        return new int[] {left, top, right, bottom};
    }

    /**
     * 列出文件夹下的所有图片文件
     * List all image files under the folder
     *
     * @param folderPath
     * @return
     */
    public static List<File> listImageFiles(String folderPath) {
        File folder = new File(folderPath);
        List<File> imageFiles = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return imageFiles;
            }
            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                            name.endsWith(".png") || name.endsWith(".bmp") ||
                            name.endsWith(".gif") || name.endsWith(".tiff") ||
                            name.endsWith(".webp")) {
                        imageFiles.add(file);
                    }
                }
            }
        }
        return imageFiles;
    }

    /**
     * 读取指定目录下所有图片，返回 List<Image>（DJL 格式）
     *
     * @param folderPath 图片文件夹路径
     * @return List<Image>
     * @throws IOException
     */
    public static List<Image> readImagesFromFolder(String folderPath) throws IOException {
        File folder = new File(folderPath);
        List<Image> imageList = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return imageList;
            }
            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                            name.endsWith(".png") || name.endsWith(".bmp") ||
                            name.endsWith(".gif") || name.endsWith(".tiff") ||
                            name.endsWith(".webp")) {

                        Image img = ImageFactory.getInstance().fromInputStream(Files.newInputStream(file.toPath()));
                        imageList.add(img);
                    }
                }
            }
        }
        return imageList;
    }

    /**
     * 判断所有图片尺寸是否一致
     *
     * @param images 图片列表
     */
    public static boolean isAllImageSizeEqual(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return true; // 空集合视为一致
        }
        int width = images.get(0).getWidth();
        int height = images.get(0).getHeight();
        for (Image img : images) {
            if (img.getWidth() != width || img.getHeight() != height) {
                return false;
            }
        }
        return true;
    }

    /**
     * 在图像上绘制带白色背景、黑色文字的文本
     */
    public static void putTextWithBackground(Mat image, String text, org.opencv.core.Point origin, Scalar textColor, Scalar backgroundColor, int padding) {
        // 默认字体
        int font = Imgproc.FONT_HERSHEY_SCRIPT_SIMPLEX;
        // 默认字体缩放大小
        double fontScale = 1.0;
        //线条粗细
        int thickness = 2;
        //获取文字大小
        int[] baseLine = new int[1];
        Size textSize = Imgproc.getTextSize(text, font, fontScale, thickness, baseLine);
        int textWidth = (int) textSize.width;
        int textHeight = (int) textSize.height;

        //计算带padding的背景框
        org.opencv.core.Point bgTopLeft = new org.opencv.core.Point(origin.x - padding, origin.y - textHeight - padding);
        org.opencv.core.Point bgBottomRight = new org.opencv.core.Point(origin.x + textWidth + padding, origin.y + baseLine[0] + padding);

        //绘制背景矩形
        Imgproc.rectangle(image, bgTopLeft, bgBottomRight, backgroundColor, Imgproc.FILLED);

        //绘制文字（黑色）
        Imgproc.putText(image, text, origin, font, fontScale, textColor, thickness);
    }



}

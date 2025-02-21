package cn.smartjavaai.face;

import cn.smartjavaai.common.entity.Rectangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

/**
 * @author dwj
 * @date 2025/2/19
 */
public class Test {

    public static void main(String[] args) throws Exception {
        // 初始化配置
        ModelConfig config = new ModelConfig();
        config.setAlgorithmName("retinaface");
        //config.setAlgorithmName("ultralightfastgenericface");
        //config.setModelPath("/Users/wenjie/Documents/idea_workplace/SmartJavaAI/models/retinaface.pt");
        config.setConfidenceThreshold(0.85f);
        config.setMaxFaceCount(100);
        config.setNmsThresh(0.45f);

        FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm(config);
        File imageFile = new File("/Users/wenjie/Downloads/djl-master/examples/src/test/resources/largest_selfie.jpg");
        FaceDetectedResult result = currentAlgorithm.detect("/Users/wenjie/Downloads/djl-master/examples/src/test/resources/largest_selfie.jpg");
        //FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(imageFile));


        // 1. 加载原始图片
        File input = new File("/Users/wenjie/Downloads/djl-master/examples/src/test/resources/largest_selfie.jpg");
        BufferedImage image = ImageIO.read(input);

        // 2. 创建绘图上下文
        Graphics2D graphics = image.createGraphics();

        // 3. 配置绘制参数
        graphics.setColor(Color.RED);             // 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿

        for(Rectangle rectangle : result.getRectangles()){
            // 4. 绘制矩形（左上角坐标x=50,y=100，宽200，高150）
            graphics.drawRect(rectangle.getPointList().get(0).getX(),
                    rectangle.getPointList().get(0).getY(), rectangle.getWidth(),  rectangle.getHeight());
        }



        // 5. 释放资源并保存
        graphics.dispose();
        ImageIO.write(image, "jpg", new File("output.jpg"));


        System.out.println("111111");
    }

}

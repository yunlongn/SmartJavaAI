package cn.smartjavaai.face.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.enums.EyeStatus;
import cn.smartjavaai.common.enums.GenderType;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.common.enums.LivenessStatus;
import cn.smartjavaai.face.exception.FaceException;
import com.seeta.sdk.*;

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
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
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
            DetectionRectangle rectangle = new DetectionRectangle(x, y, width, height);
            FaceInfo faceInfo = new FaceInfo(keyPoints);
            DetectionInfo detectionInfo = new DetectionInfo(rectangle, detection.getProbabilities().get(index).floatValue(),faceInfo);
            detectionInfoList.add(detectionInfo);
            index++;
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }

    /**
     * 转换为FaceDetectedResult
     * @param seetaResult
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(SeetaRect[] seetaResult, List<SeetaPointF[]> seetaPointFSList){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for(int i = 0; i < seetaResult.length; i++){
            SeetaRect rect  = seetaResult[i];
            SeetaPointF[] seetaPointFS = seetaPointFSList.get(i);
            //过滤置信度
            /*if(config.getConfidenceThreshold() > 0){
                continue;
            }*/
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
            List<Point> keyPoints = Arrays.stream(seetaPointFS)
                    .map(p -> new Point(p.x, p.y))
                    .collect(Collectors.toList());
            FaceInfo faceInfo = new FaceInfo(keyPoints);
            DetectionInfo detectionInfo = new DetectionInfo(rectangle, 0, faceInfo);
            detectionInfoList.add(detectionInfo);
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }


    /**
     * 转换为FaceDetectedResult(人脸特征提取)
     * @param seetaResult
     * @return
     */
    public static DetectionResponse featuresConvertToResponse(SeetaRect[] seetaResult, List<SeetaPointF[]> seetaPointFSList, List<float[]> featureList){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for(int i = 0; i < seetaResult.length; i++){
            SeetaRect rect  = seetaResult[i];
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
            FaceInfo faceInfo = new FaceInfo();
            if(seetaPointFSList != null && seetaPointFSList.size() > 0){
                SeetaPointF[] seetaPointFS = seetaPointFSList.get(i);
                List<Point> keyPoints = Arrays.stream(seetaPointFS)
                        .map(p -> new Point(p.x, p.y))
                        .collect(Collectors.toList());
                faceInfo.setKeyPoints(keyPoints);
            }
            if(featureList != null && featureList.size() > 0){
                faceInfo.setFeature(featureList.get(i));
            }
            detectionInfoList.add(new DetectionInfo(rectangle, 0, faceInfo));
        }
        return new DetectionResponse(detectionInfoList);
    }


    /**
     * 转换为FaceDetectedResult(人脸特征提取)
     * @param rect
     * @param seetaPointFS
     * @param feature
     * @return
     */
    public static DetectionResponse featuresConvertToResponse(SeetaRect rect, SeetaPointF[] seetaPointFS, float[] feature){
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
        FaceInfo faceInfo = new FaceInfo();
        List<Point> keyPoints = Arrays.stream(seetaPointFS)
                .map(p -> new Point(p.x, p.y))
                .collect(Collectors.toList());
        faceInfo.setKeyPoints(keyPoints);
        faceInfo.setFeature(feature);
        detectionInfoList.add(new DetectionInfo(rectangle, 0, faceInfo));
        return new DetectionResponse(detectionInfoList);
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
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getDetectionInfoList()) || detectionResponse.getDetectionInfoList().isEmpty()){
            throw new FaceException("无目标数据");
        }
        Graphics2D graphics = sourceImage.createGraphics();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        for(DetectionInfo detectionInfo : detectionResponse.getDetectionInfoList()){
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            graphics.setColor(Color.RED);// 边框颜色
            graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),  rectangle.getHeight());
            drawText(graphics, "face", rectangle.getX(), rectangle.getY(), stroke, 4);
            //绘制人脸关键点
            if(detectionInfo.getFaceInfo() != null && detectionInfo.getFaceInfo().getKeyPoints() != null &&
                !detectionInfo.getFaceInfo().getKeyPoints().isEmpty()){
                drawLandmarks(graphics, detectionInfo.getFaceInfo().getKeyPoints());
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
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getDetectionInfoList()) || detectionResponse.getDetectionInfoList().isEmpty()){
            throw new FaceException("无目标数据");
        }
        Graphics2D graphics = sourceImage.createGraphics();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        for(DetectionInfo detectionInfo : detectionResponse.getDetectionInfoList()){
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            graphics.setColor(Color.RED);// 边框颜色
            graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),  rectangle.getHeight());
            drawText(graphics, "face", rectangle.getX(), rectangle.getY(), stroke, 4);
            //绘制人脸关键点
            if(detectionInfo.getFaceInfo() != null && detectionInfo.getFaceInfo().getKeyPoints() != null &&
                    !detectionInfo.getFaceInfo().getKeyPoints().isEmpty()){
                drawLandmarks(graphics, detectionInfo.getFaceInfo().getKeyPoints());
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
        return new DetectionRectangle(x, y, width, height);
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


    /**
     * 将DetectionRectangle转换为SeetaRect
     * @param detectionRectangle
     * @return
     */
    public static SeetaRect convertToSeetaRect(DetectionRectangle detectionRectangle){
        SeetaRect seetaRect = new SeetaRect();
        seetaRect.x = detectionRectangle.getX();
        seetaRect.y = detectionRectangle.getY();
        seetaRect.width = detectionRectangle.getWidth();
        seetaRect.height = detectionRectangle.getHeight();
        return seetaRect;
    }


    /**
     * 将PointList转换为SeetaPointF[]
     * @param pointList
     * @return
     */
    public static SeetaPointF[] convertToSeetaPointF(List<Point> pointList){
        return pointList.stream()
                .map(p -> {
                    SeetaPointF sp = new SeetaPointF();
                    sp.x = p.getX();
                    sp.y = p.getY();
                    return sp;
                })
                .toArray(SeetaPointF[]::new);
    }

    /**
     * 将SeetaAntiSpoofing.Status转换为LivenessStatus
     * @param status
     * @return
     */
    public static LivenessStatus convertToLivenessStatus(FaceAntiSpoofing.Status status){
        if(status == null){
            return LivenessStatus.UNKNOWN;
        }
        switch (status) {
            case REAL:
                return LivenessStatus.LIVE;
            case SPOOF:
                return LivenessStatus.NON_LIVE;
            case FUZZY:
                return LivenessStatus.UNKNOWN;
            case DETECTING:
                return LivenessStatus.DETECTING;
            default:
                return LivenessStatus.UNKNOWN;  // 默认返回未知
        }
    }

    /**
     * 转为GenderType
     * @param gender
     * @return
     */
    public static GenderType convertToGenderType(GenderPredictor.GENDER gender){
        if(gender == null){
            return GenderType.UNKNOWN;
        }
        switch (gender) {
            case MALE:
                return GenderType.MALE;
            case FEMALE:
                return GenderType.FEMALE;
            default:
                return GenderType.UNKNOWN;  // 默认返回未知
        }
    }

    /**
     * 转为EyeStatus
     * @param eyeState
     * @return
     */
    public static EyeStatus convertToEyeStatus(EyeStateDetector.EYE_STATE eyeState){
        if(eyeState == null){
            return EyeStatus.UNKNOWN;
        }
        switch (eyeState) {
            case EYE_OPEN:
                return EyeStatus.OPEN;
            case EYE_CLOSE:
                return EyeStatus.CLOSED;
            case EYE_RANDOM:
                return EyeStatus.NON_EYE_REGION;
            default:
                return EyeStatus.UNKNOWN;  // 默认返回未知
        }
    }

    public static DetectionResponse convertToFaceAttributeResponse(SeetaRect[] seetaResult, List<SeetaPointF[]> seetaPointFSList, List<FaceAttribute> faceAttributeList){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for(int i = 0; i < seetaResult.length; i++){
            SeetaRect rect  = seetaResult[i];
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
            FaceInfo faceInfo = new FaceInfo();
            if(seetaPointFSList != null && seetaPointFSList.size() > 0){
                SeetaPointF[] seetaPointFS = seetaPointFSList.get(i);
                List<Point> keyPoints = Arrays.stream(seetaPointFS)
                        .map(p -> new Point(p.x, p.y))
                        .collect(Collectors.toList());
                faceInfo.setKeyPoints(keyPoints);
            }
            if(faceAttributeList != null && faceAttributeList.size() > 0){
                faceInfo.setFaceAttribute(faceAttributeList.get(i));
            }
            detectionInfoList.add(new DetectionInfo(rectangle, 0, faceInfo));
        }
        return new DetectionResponse(detectionInfoList);
    }

    /**
     * 转换为FaceDetectedResult
     * @param seetaResult
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(SeetaRect[] seetaResult, List<SeetaPointF[]> seetaPointFSList, List<LivenessStatus> livenessStatusList){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for(int i = 0; i < seetaResult.length; i++){
            SeetaRect rect  = seetaResult[i];
            SeetaPointF[] seetaPointFS = seetaPointFSList.get(i);
            //过滤置信度
            /*if(config.getConfidenceThreshold() > 0){
                continue;
            }*/
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
            List<Point> keyPoints = Arrays.stream(seetaPointFS)
                    .map(p -> new Point(p.x, p.y))
                    .collect(Collectors.toList());
            FaceInfo faceInfo = new FaceInfo(keyPoints);
            faceInfo.setLivenessStatus(livenessStatusList.get(i));
            DetectionInfo detectionInfo = new DetectionInfo(rectangle, 0, faceInfo);
            detectionInfoList.add(detectionInfo);
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }

    /**
     * 绘制人脸属性
     * @param sourceImage
     * @param detectionResponse
     * @param savePath
     * @throws IOException
     */
    public static void drawBoxesWithFaceAttribute(BufferedImage sourceImage, DetectionResponse detectionResponse, String savePath) throws IOException {
        if(!ImageUtils.isImageValid(sourceImage)){
            throw new FaceException("图像无效");
        }
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getDetectionInfoList()) || detectionResponse.getDetectionInfoList().isEmpty()){
            throw new FaceException("无目标数据");
        }
        Graphics2D graphics = sourceImage.createGraphics();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        for(DetectionInfo detectionInfo : detectionResponse.getDetectionInfoList()){
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            graphics.setColor(Color.RED);// 边框颜色
            graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),  rectangle.getHeight());
            //drawText(graphics, "face", rectangle.getX(), rectangle.getY(), stroke, 4);
            //绘制人脸关键点
            if(detectionInfo.getFaceInfo() != null && detectionInfo.getFaceInfo().getKeyPoints() != null &&
                    !detectionInfo.getFaceInfo().getKeyPoints().isEmpty()){
                drawLandmarks(graphics, detectionInfo.getFaceInfo().getKeyPoints());
            }
            // 判断人脸框是否足够大
            if (rectangle.getHeight() > 60 && detectionInfo.getFaceInfo() != null && detectionInfo.getFaceInfo().getFaceAttribute() != null) {
                StringBuilder attrText = new StringBuilder();
                FaceAttribute faceAttribute = detectionInfo.getFaceInfo().getFaceAttribute();
                if (faceAttribute.getGenderType() != null) {
                    attrText.append(faceAttribute.getGenderType().name()).append(" ");
                }

                if (faceAttribute.getAge() != null) {
                    attrText.append(faceAttribute.getAge()).append("岁").append(" ");
                }

                if (faceAttribute.getWearingMask() != null) {
                    attrText.append(faceAttribute.getWearingMask() ? "戴口罩" : "未戴口罩").append(" ");
                }

                if (faceAttribute.getLeftEyeStatus() != null && faceAttribute.getRightEyeStatus() != null) {
                    attrText.append("眼睛:")
                            .append(faceAttribute.getLeftEyeStatus().name())
                            .append("/")
                            .append(faceAttribute.getRightEyeStatus().name())
                            .append(" ");
                }

                List<String> lines = new ArrayList<>();

                if (faceAttribute.getGenderType() != null) {
                    lines.add("性别: " + faceAttribute.getGenderType().name());
                }
                if (faceAttribute.getAge() != null) {
                    lines.add("年龄: " + faceAttribute.getAge());
                }
                if (faceAttribute.getWearingMask() != null) {
                    lines.add("口罩: " + (faceAttribute.getWearingMask() ? "是" : "否"));
                }
                if (faceAttribute.getLeftEyeStatus() != null && faceAttribute.getRightEyeStatus() != null) {
                    lines.add("眼睛: " + faceAttribute.getLeftEyeStatus().name() + "/" + faceAttribute.getRightEyeStatus().name());
                }
                if (faceAttribute.getHeadPose() != null) {
                    //attrText.append("姿态:").append(faceAttribute.getHeadPose().toString());
                    HeadPose pose = faceAttribute.getHeadPose();
                    String pitch = pose.getPitch() != null ? String.valueOf(pose.getPitch().intValue()) : "-";
                    String yaw = pose.getYaw() != null ? String.valueOf(pose.getYaw().intValue()) : "-";
                    String roll = pose.getRoll() != null ? String.valueOf(pose.getRoll().intValue()) : "-";
                    lines.add("姿态: P=" + pitch + " Y=" + yaw + " R=" + roll);
                }
                if (!lines.isEmpty()) {
                    drawMultilineTextWithBackground(graphics, lines, rectangle.getX(), rectangle.getY());  // 适当偏移
                }

            }
        }
        graphics.dispose();
        ImageIO.write(sourceImage, "jpg", new File(savePath));
    }

    private static void drawMultilineTextWithBackground(Graphics2D g, List<String> lines, int x, int y) {
        Font font = new Font("SansSerif", Font.PLAIN, 14);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int maxWidth = lines.stream().mapToInt(fm::stringWidth).max().orElse(0);

        int padding = 4;
        int boxWidth = maxWidth + padding * 2;
        int boxHeight = lineHeight * lines.size() + padding * 2;

        // 背景矩形
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRoundRect(x, y, boxWidth, boxHeight, 8, 8);

        // 绘制每一行文字
        g.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), x + padding, y + padding + (i + 1) * lineHeight - 4);
        }
    }

    /**
     * 将 Milvus 查询返回的得分转换为 0~1 范围的相似度
     * @param metricType 向量度量方式：IP 或 L2
     * @param score 原始得分（L2 为距离，IP 为相似度）
     * @return 映射后的相似度（0~1）
     */
    public static float convertScoreToSimilarity(String metricType, float score) {
        switch (metricType.toUpperCase()) {
            case "IP":
                // 内积 IP 的范围为 [-1, 1]，归一化为 [0, 1]
                return (score + 1.0f) / 2.0f;
            case "L2":
                // 欧氏距离 L2：距离越小越相似，1 / (1 + 距离) 映射到 (0, 1]
                return 1.0f / (1.0f + score);
            case "COSINE":
                // 余弦相似度 COSINE：本身范围为 [-1, 1]，也需要归一化到 [0, 1]
                return (score + 1.0f) / 2.0f;
            default:
                throw new IllegalArgumentException("Unsupported metricType: " + metricType);
        }
    }



}

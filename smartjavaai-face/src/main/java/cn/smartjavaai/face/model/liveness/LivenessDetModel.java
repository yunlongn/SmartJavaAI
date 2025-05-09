package cn.smartjavaai.face.model.liveness;

import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.common.enums.LivenessStatus;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * 活体检测模型
 * @author dwj
 */
public interface LivenessDetModel {

    /**
     * 加载模型
     * @param config
     */
    void loadModel(LivenessConfig config); // 加载模型


    /**
     * 活体检测(多人脸)
     * @param imagePath 图片路径
     * @return
     */
    default DetectionResponse detect(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(多人脸)
     * @param image BufferedImage
     * @return
     */
    default DetectionResponse detect(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(多人脸)
     * @param imageData 图片字节流
     * @return
     */
    default DetectionResponse detect(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(多人脸)
     * @param imagePath 图片路径
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default List<LivenessStatus> detect(String imagePath, DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(单人脸)
     * @param imagePath 图片路径
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default LivenessStatus detect(String imagePath, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(多人脸)
     * @param imageData 图片数据
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default List<LivenessStatus> detect(byte[] imageData,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(单人脸)
     * @param imageData
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default LivenessStatus detect(byte[] imageData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(多人脸)
     * @param image BufferedImage
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default List<LivenessStatus> detect(BufferedImage image,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(单人脸)
     * @param image BufferedImage
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default LivenessStatus detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(分数最高人脸)
     * @param image
     * @return
     */
    default LivenessStatus detectTopFace(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(分数最高人脸)
     * @param imagePath
     * @return
     */
    default LivenessStatus detectTopFace(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(分数最高人脸)
     * @param imageData
     * @return
     */
    default LivenessStatus detectTopFace(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 视频活体检测(逐帧检测)
     * @param frameImage
     * @param faceDetectionRectangle
     * @return
     */
    default LivenessStatus detectVideoByFrame(BufferedImage frameImage, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 视频活体检测(逐帧检测)
     * @param frameData
     * @param faceDetectionRectangle
     * @return
     */
    default LivenessStatus detectVideoByFrame(byte[] frameData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 视频活体检测(逐帧检测)
     * @param frameImageData
     * @return
     */
    default LivenessStatus detectVideoByFrame(byte[] frameImageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 视频活体检测(逐帧检测)
     * @param frameImageData
     * @return
     */
    default LivenessStatus detectVideoByFrame(BufferedImage frameImageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 视频活体检测
     * @param videoInputStream
     * @return
     */
    default LivenessStatus detectVideo(InputStream videoInputStream){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 视频活体检测
     * @param videoPath
     * @return
     */
    default LivenessStatus detectVideo(String videoPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }







}

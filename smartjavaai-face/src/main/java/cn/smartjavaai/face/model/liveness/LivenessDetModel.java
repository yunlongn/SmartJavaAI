package cn.smartjavaai.face.model.liveness;

import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.face.config.LivenessConfig;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * 活体检测模型
 * @author dwj
 */
public interface LivenessDetModel extends AutoCloseable{

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
    default R<DetectionResponse> detect(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(多人脸)
     * @param image BufferedImage
     * @return
     */
    default R<DetectionResponse> detect(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(多人脸)
     * @param imageData 图片字节流
     * @return
     */
    default R<DetectionResponse> detect(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(多人脸)
     * @param base64Image
     * @return
     */
    default R<DetectionResponse> detectBase64(String base64Image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(多人脸)
     * @param imagePath 图片路径
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default R<List<LivenessResult>> detect(String imagePath, DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(多人脸)
     * @param imageData 图片数据
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default R<List<LivenessResult>> detect(byte[] imageData,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(多人脸)
     * @param image BufferedImage
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default R<List<LivenessResult>> detect(BufferedImage image,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(多人脸)
     * @param base64Image
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default R<List<LivenessResult>> detectBase64(String base64Image,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(单人脸)
     * @param imagePath 图片路径
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detect(String imagePath, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(单人脸)
     * @param imageData
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detect(byte[] imageData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }




    /**
     * 活体检测(单人脸)
     * @param image BufferedImage
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(单人脸)
     * @param base64Image
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detectBase64(String base64Image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(单人脸)
     * @param imagePath 图片路径
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detect(String imagePath, DetectionRectangle faceDetectionRectangle){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(单人脸)
     * @param imageData
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detect(byte[] imageData, DetectionRectangle faceDetectionRectangle){
        throw new UnsupportedOperationException("默认不支持该功能");
    }




    /**
     * 活体检测(单人脸)
     * @param image BufferedImage
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detect(BufferedImage image, DetectionRectangle faceDetectionRectangle){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(单人脸)
     * @param base64Image
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detectBase64(String base64Image, DetectionRectangle faceDetectionRectangle){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(分数最高人脸)
     * @param image
     * @return
     */
    default R<LivenessResult> detectTopFace(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(分数最高人脸)
     * @param imagePath
     * @return
     */
    default R<LivenessResult> detectTopFace(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(分数最高人脸)
     * @param imageData
     * @return
     */
    default R<LivenessResult> detectTopFace(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(分数最高人脸)
     * @param base64Image
     * @return
     */
    default R<LivenessResult> detectTopFaceBase64(String base64Image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }



    /**
     * 视频活体检测(逐帧检测)
     * @param frameImage
     * @param faceDetectionRectangle
     * @return
     */
//    default R<LivenessResult> detectVideoByFrame(BufferedImage frameImage, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
//        throw new UnsupportedOperationException("默认不支持该功能");
//    }

    /**
     * 视频活体检测(逐帧检测)
     * @param frameData
     * @param faceDetectionRectangle
     * @return
     */
//    default R<LivenessResult> detectVideoByFrame(byte[] frameData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
//        throw new UnsupportedOperationException("默认不支持该功能");
//    }

    /**
     * 视频活体检测(逐帧检测)
     * @param frameImageData
     * @return
     */
//    default R<LivenessResult> detectVideoByFrame(byte[] frameImageData){
//        throw new UnsupportedOperationException("默认不支持该功能");
//    }

    /**
     * 视频活体检测(逐帧检测)
     * @param frameImageData
     * @return
     */
//    default R<LivenessResult> detectVideoByFrame(BufferedImage frameImageData){
//        throw new UnsupportedOperationException("默认不支持该功能");
//    }

    /**
     * 视频活体检测
     * @param videoInputStream
     * @return
     */
    default R<LivenessResult> detectVideo(InputStream videoInputStream){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 视频活体检测
     * @param videoPath
     * @return
     */
    default R<LivenessResult> detectVideo(String videoPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }







}

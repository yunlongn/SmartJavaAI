package smartai.examples.face.attribute;

import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.config.FaceAttributeConfig;
import cn.smartjavaai.face.constant.LivenessConstant;
import cn.smartjavaai.face.enums.FaceModelEnum;
import cn.smartjavaai.face.enums.FaceAttributeModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceModelFactory;
import cn.smartjavaai.face.factory.FaceAttributeModelFactory;
import cn.smartjavaai.face.model.attribute.FaceAttributeModel;
import cn.smartjavaai.face.model.facerec.FaceModel;
import cn.smartjavaai.face.utils.FaceUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * 人脸属性检测demo
 * @author dwj
 * @date 2025/5/1
 */
@Slf4j
public class FaceAttributeDetDemo {


    /**
     * 人脸属性检测（多人脸）
     */
    @Test
    public void testFaceAttributeDetect(){
        FaceAttributeConfig config = new FaceAttributeConfig();
        config.setModelEnum(FaceAttributeModelEnum.SEETA_FACE6_MODEL);
        //需替换为实际模型存储路径
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
        FaceAttributeModel faceAttributeModel = FaceAttributeModelFactory.getInstance().getModel(config);
        DetectionResponse detectionResponse = faceAttributeModel.detect("src/main/resources/double_person.png");
        try {
            //绘制并导出人脸属性图片，小人脸仅有人脸框
            BufferedImage image = ImageIO.read(new File(Paths.get("src/main/resources/double_person.png").toAbsolutePath().toString()));
            FaceUtils.drawBoxesWithFaceAttribute(image, detectionResponse,"C:/Users/Administrator/Downloads/double_person_.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("人脸属性检测结果：{}", JSONObject.toJSONString(detectionResponse));
    }

    /**
     * 图片人脸属性检测（分数最高人脸）
     */
    @Test
    public void testFaceAttributeDetect2(){
        FaceAttributeConfig config = new FaceAttributeConfig();
        config.setModelEnum(FaceAttributeModelEnum.SEETA_FACE6_MODEL);
        //需替换为实际模型存储路径
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
        FaceAttributeModel faceAttributeModel = FaceAttributeModelFactory.getInstance().getModel(config);
        FaceAttribute faceAttribute = faceAttributeModel.detectTopFace("src/main/resources/double_person.png");
        log.info("人脸属性检测结果：{}", JSONObject.toJSONString(faceAttribute));
    }

    /**
     * 图片多人脸属性检测（基于已检测出的人脸区域和关键点）
     */
    @Test
    public void testFaceAttributeDetect3(){
        //人脸检测
        //需替换为实际模型存储路径
        String modelPath = "C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models";
        FaceModelConfig faceDetectModelConfig = new FaceModelConfig();
        faceDetectModelConfig.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);
        faceDetectModelConfig.setModelPath(modelPath);
        FaceModel faceDetectModel = FaceModelFactory.getInstance().getModel(faceDetectModelConfig);
        DetectionResponse detectionResponse = faceDetectModel.detect("src/main/resources/double_person.png");
        log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse));
        //检测到人脸
        if(detectionResponse != null && detectionResponse.getDetectionInfoList() != null && detectionResponse.getDetectionInfoList().size() > 0){
            //人脸属性检测
            FaceAttributeConfig config = new FaceAttributeConfig();
            config.setModelEnum(FaceAttributeModelEnum.SEETA_FACE6_MODEL);
            config.setModelPath(modelPath);
            FaceAttributeModel faceAttributeModel = FaceAttributeModelFactory.getInstance().getModel(config);
            List<FaceAttribute> livenessStatusList = faceAttributeModel.detect("src/main/resources/double_person.png",detectionResponse);
            log.info("人脸属性检测结果：{}", JSONObject.toJSONString(livenessStatusList));
        }
    }

    /**
     * 图片单人脸人脸属性检测（基于已检测出的人脸区域和关键点）
     */
    @Test
    public void testFaceAttributeDetect4(){
        try {
            //人脸检测
            //需替换为实际模型存储路径
            String modelPath = "C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models";
            String imagePath = "src/main/resources/double_person.png";
            FaceModelConfig faceDetectModelConfig = new FaceModelConfig();
            faceDetectModelConfig.setModelEnum(FaceModelEnum.SEETA_FACE6_MODEL);
            faceDetectModelConfig.setModelPath(modelPath);
            FaceModel faceDetectModel = FaceModelFactory.getInstance().getModel(faceDetectModelConfig);
            DetectionResponse detectionResponse = faceDetectModel.detect(imagePath);
            log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse));
            //检测到人脸
            if(detectionResponse != null && detectionResponse.getDetectionInfoList() != null && detectionResponse.getDetectionInfoList().size() > 0){
                //人脸属性检测
                FaceAttributeConfig config = new FaceAttributeConfig();
                config.setModelEnum(FaceAttributeModelEnum.SEETA_FACE6_MODEL);
                config.setModelPath(modelPath);
                FaceAttributeModel faceAttributeModel = FaceAttributeModelFactory.getInstance().getModel(config);
                BufferedImage image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
                for (DetectionInfo detectionInfo : detectionResponse.getDetectionInfoList()){
                    FaceInfo faceInfo = detectionInfo.getFaceInfo();
                    FaceAttribute faceAttribute = faceAttributeModel.detect(image, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
                    log.info("人脸属性检测结果：{}", JSONObject.toJSONString(faceAttribute));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}

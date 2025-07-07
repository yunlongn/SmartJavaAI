package cn.smartjavaai.face.model.quality;

import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.PoolUtils;
import cn.smartjavaai.face.config.QualityConfig;
import cn.smartjavaai.face.entity.FaceQualityResult;
import cn.smartjavaai.face.entity.FaceQualitySummary;
import cn.smartjavaai.face.enums.QualityGrade;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.seetaface.ClarityDLResult;
import cn.smartjavaai.face.seetaface.NativeLoader;
import cn.smartjavaai.face.utils.FaceUtils;
import com.seeta.pool.*;
import com.seeta.sdk.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * seetaface6 质量评估模型
 * @author dwj
 * @date 2025/4/30
 */
@Slf4j
public class Seetaface6QualityModel implements FaceQualityModel {

    private QualityConfig config;


    /**
     * 人脸亮度评估器池
     */
    private QualityOfBrightnessPool qualityOfBrightnessPool;

    /**
     * 人脸清晰度评估器池
     */
    private QualityOfClarityPool qualityOfClarityPool;

    /**
     * 人脸清晰度评估器池(深度学习)
     */
    private QualityOfLBNPool qualityOfLBNPool;

    /**
     * 人脸完整度评估器池
     */
    private QualityOfIntegrityPool qualityOfIntegrityPool;

    /**
     * 人脸姿态评估器池
     */
    private QualityOfPosePool qualityOfPosePool;

    /**
     * 人脸姿态评估器池(深度学习)
     */
    private QualityOfPoseExPool qualityOfPoseExPool;

    /**
     * 人脸分辨率评估器池
     */
    private QualityOfResolutionPool qualityOfResolutionPool;


    @Override
    public void loadModel(QualityConfig config) {
        DeviceEnum device = DeviceEnum.CPU;
        if(Objects.nonNull(config.getDevice())){
            device = config.getDevice();
        }
        //加载依赖库
        NativeLoader.loadNativeLibraries(device);
        this.config = config;
        log.debug("Loading seetaFace6 library successfully.");
    }


    @Override
    public R<FaceQualityResult> evaluateBrightness(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints) {
        //参数检查
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(rectangle)){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "rectangle为空");
        }
        if(Objects.isNull(keyPoints) || keyPoints.isEmpty()){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "keyPoints为空");
        }
        QualityOfBrightness qualityOfBrightness = null;
        try {
            if(Objects.isNull(this.qualityOfBrightnessPool)){
                this.qualityOfBrightnessPool = new QualityOfBrightnessPool(new SeetaConfSetting());
            }
            qualityOfBrightness = qualityOfBrightnessPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = FaceUtils.convertToSeetaRect(rectangle);
            SeetaPointF[] pointFS = FaceUtils.convertToSeetaPointF(keyPoints);
            float[] scores = new float[1];
            QualityOfBrightness.QualityLevel level = qualityOfBrightness.check(imageData, seetaRect, pointFS, scores);
            FaceQualityResult result = new FaceQualityResult(scores[0], QualityGrade.valueOf(level.name()));
            return R.ok(result);
        } catch (Exception e) {
            throw new FaceException("亮度评估错误", e);
        } finally {
            if (qualityOfBrightness != null) {
                try {
                    qualityOfBrightnessPool.returnObject(qualityOfBrightness);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<FaceQualityResult> evaluateBrightness(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return evaluateBrightness(image, rectangle, keyPoints);
    }

    @Override
    public R<FaceQualityResult> evaluateBrightness(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return evaluateBrightness(ImageIO.read(new ByteArrayInputStream(imageData)), rectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<FaceQualityResult> evaluateClarity(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints) {
        //参数检查
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(rectangle)){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "rectangle为空");
        }
        if(Objects.isNull(keyPoints) || keyPoints.isEmpty()){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "keyPoints为空");
        }
        QualityOfClarity qualityOfClarity = null;
        try {
            if(Objects.isNull(this.qualityOfClarityPool)){
                this.qualityOfClarityPool = new QualityOfClarityPool(new SeetaConfSetting());
            }
            qualityOfClarity = qualityOfClarityPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = FaceUtils.convertToSeetaRect(rectangle);
            SeetaPointF[] pointFS = FaceUtils.convertToSeetaPointF(keyPoints);
            float[] scores = new float[1];
            QualityOfClarity.QualityLevel level = qualityOfClarity.check(imageData, seetaRect, pointFS, scores);
            FaceQualityResult result = new FaceQualityResult(scores[0], QualityGrade.valueOf(level.name()));
            return R.ok(result);
        } catch (Exception e) {
            throw new FaceException("清晰度评估错误", e);
        } finally {
            if (qualityOfClarity != null) {
                try {
                    qualityOfClarityPool.returnObject(qualityOfClarity);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<FaceQualityResult> evaluateClarity(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return evaluateClarity(image, rectangle, keyPoints);
    }

    @Override
    public R<FaceQualityResult> evaluateClarity(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return evaluateClarity(ImageIO.read(new ByteArrayInputStream(imageData)), rectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<FaceQualityResult> evaluateCompleteness(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints) {
        //参数检查
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(rectangle)){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "rectangle为空");
        }
        if(Objects.isNull(keyPoints) || keyPoints.isEmpty()){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "keyPoints为空");
        }
        QualityOfIntegrity qualityOfIntegrity = null;
        try {
            if(Objects.isNull(this.qualityOfIntegrityPool)){
                this.qualityOfIntegrityPool = new QualityOfIntegrityPool(new SeetaConfSetting());
            }
            qualityOfIntegrity = qualityOfIntegrityPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = FaceUtils.convertToSeetaRect(rectangle);
            SeetaPointF[] pointFS = FaceUtils.convertToSeetaPointF(keyPoints);
            float[] scores = new float[1];
            QualityOfIntegrity.QualityLevel level = qualityOfIntegrity.check(imageData, seetaRect, pointFS, scores);
            FaceQualityResult result = new FaceQualityResult(scores[0], QualityGrade.valueOf(level.name()));
            return R.ok(result);
        } catch (Exception e) {
            throw new FaceException("完整度评估错误", e);
        } finally {
            if (qualityOfIntegrity != null) {
                try {
                    qualityOfIntegrityPool.returnObject(qualityOfIntegrity);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<FaceQualityResult> evaluateCompleteness(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return evaluateCompleteness(image, rectangle, keyPoints);
    }

    @Override
    public R<FaceQualityResult> evaluateCompleteness(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return evaluateCompleteness(ImageIO.read(new ByteArrayInputStream(imageData)), rectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<FaceQualityResult> evaluatePose(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints) {
        //参数检查
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(rectangle)){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "rectangle为空");
        }
        if(Objects.isNull(keyPoints) || keyPoints.isEmpty()){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "keyPoints为空");
        }
        QualityOfPose qualityOfPose = null;
        try {
            if(Objects.isNull(this.qualityOfPosePool)){
                this.qualityOfPosePool = new QualityOfPosePool(new SeetaConfSetting());
            }
            qualityOfPose = qualityOfPosePool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = FaceUtils.convertToSeetaRect(rectangle);
            SeetaPointF[] pointFS = FaceUtils.convertToSeetaPointF(keyPoints);
            float[] scores = new float[1];
            QualityOfPose.QualityLevel level = qualityOfPose.check(imageData, seetaRect, pointFS, scores);
            FaceQualityResult result = new FaceQualityResult(scores[0], QualityGrade.valueOf(level.name()));
            return R.ok(result);
        } catch (Exception e) {
            throw new FaceException("姿态评估错误", e);
        } finally {
            if (qualityOfPose != null) {
                try {
                    qualityOfPosePool.returnObject(qualityOfPose);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<FaceQualityResult> evaluatePose(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return evaluatePose(image, rectangle, keyPoints);
    }

    @Override
    public R<FaceQualityResult> evaluatePose(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return evaluatePose(ImageIO.read(new ByteArrayInputStream(imageData)), rectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<FaceQualityResult> evaluateResolution(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints) {
        //参数检查
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(rectangle)){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "rectangle为空");
        }
        if(Objects.isNull(keyPoints) || keyPoints.isEmpty()){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "keyPoints为空");
        }
        QualityOfResolution qualityOfResolution = null;
        try {
            if(Objects.isNull(this.qualityOfResolutionPool)){
                this.qualityOfResolutionPool = new QualityOfResolutionPool(new SeetaConfSetting());
            }
            qualityOfResolution = qualityOfResolutionPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = FaceUtils.convertToSeetaRect(rectangle);
            SeetaPointF[] pointFS = FaceUtils.convertToSeetaPointF(keyPoints);
            float[] scores = new float[1];
            QualityOfResolution.QualityLevel level = qualityOfResolution.check(imageData, seetaRect, pointFS, scores);
            FaceQualityResult result = new FaceQualityResult(scores[0], QualityGrade.valueOf(level.name()));
            return R.ok(result);
        } catch (Exception e) {
            throw new FaceException("姿态评估错误", e);
        } finally {
            if (qualityOfResolution != null) {
                try {
                    qualityOfResolutionPool.returnObject(qualityOfResolution);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<FaceQualityResult> evaluateResolution(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return evaluateResolution(image, rectangle, keyPoints);
    }

    @Override
    public R<FaceQualityResult> evaluateResolution(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return evaluateResolution(ImageIO.read(new ByteArrayInputStream(imageData)), rectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }


    public R<ClarityDLResult> evaluateClarityWithDL(BufferedImage image, List<Point> keyPoints) {
        //参数检查
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(keyPoints) || keyPoints.isEmpty()){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "keyPoints为空");
        }
        QualityOfLBN qualityOfLBN = null;
        try {
            if(Objects.isNull(this.qualityOfLBNPool)){
                if(Objects.isNull(config)){
                    return R.fail(R.Status.PARAM_ERROR.getCode(), "缺少必要配置（QualityConfig），请在调用前初始化模型配置");
                }
                if(StringUtils.isBlank(config.getModelPath())){
                    return R.fail(R.Status.PARAM_ERROR.getCode(), "QualityConfig中，modelPath为空");
                }
                SeetaConfSetting setting = getClarityMLSetting();
                this.qualityOfLBNPool = new QualityOfLBNPool(setting);
            }
            qualityOfLBN = qualityOfLBNPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaPointF[] pointFS = FaceUtils.convertToSeetaPointF(keyPoints);
            int[] light = new int[1];
            int[] blur = new int[1];
            int[] noise = new int[1];
            qualityOfLBN.Detect(imageData, pointFS, light, blur, noise);
            return R.ok(new ClarityDLResult(light, blur, noise));
        } catch (Exception e) {
            throw new FaceException("亮度评估错误", e);
        } finally {
            if (qualityOfLBN != null) {
                try {
                    qualityOfLBNPool.returnObject(qualityOfLBN);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }


    public R<ClarityDLResult> evaluateClarityWithDL(String imagePath, List<Point> keyPoints) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return evaluateClarityWithDL(image, keyPoints);
    }

    public R<ClarityDLResult> evaluateClarityWithDL(byte[] imageData, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return evaluateClarityWithDL(ImageIO.read(new ByteArrayInputStream(imageData)), keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    public R<FaceQualityResult> evaluatePoseWithDL(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints) {
        //参数检查
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(rectangle)){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "rectangle为空");
        }
        if(Objects.isNull(keyPoints) || keyPoints.isEmpty()){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "keyPoints为空");
        }
        QualityOfPoseEx qualityOfPoseEx = null;
        try {
            if(Objects.isNull(this.qualityOfPoseExPool)){
                if(Objects.isNull(config)){
                    return R.fail(R.Status.PARAM_ERROR.getCode(), "缺少必要配置（QualityConfig），请在调用前初始化模型配置");
                }
                if(StringUtils.isBlank(config.getModelPath())){
                    return R.fail(R.Status.PARAM_ERROR.getCode(), "QualityConfig中，modelPath为空");
                }
                SeetaConfSetting setting = getPoseMLSetting();
                this.qualityOfPoseExPool = new QualityOfPoseExPool(setting);
            }
            qualityOfPoseEx = qualityOfPoseExPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaPointF[] pointFS = FaceUtils.convertToSeetaPointF(keyPoints);
            SeetaRect seetaRect = FaceUtils.convertToSeetaRect(rectangle);
            float[] scores = new float[1];
            QualityOfPoseEx.QualityLevel level = qualityOfPoseEx.check(imageData, seetaRect, pointFS, scores);
            FaceQualityResult result = new FaceQualityResult(scores[0], QualityGrade.valueOf(level.name()));
            return R.ok(result);
        } catch (Exception e) {
            throw new FaceException("亮度评估错误", e);
        } finally {
            if (qualityOfPoseEx != null) {
                try {
                    qualityOfPoseExPool.returnObject(qualityOfPoseEx);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    public R<FaceQualityResult> evaluatePoseWithDL(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return evaluatePoseWithDL(image, rectangle, keyPoints);
    }

    public R<FaceQualityResult> evaluatePoseWithDL(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return evaluatePoseWithDL(ImageIO.read(new ByteArrayInputStream(imageData)), rectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    /**
     * 获取清晰度模型配置(深度学习)
     * @return
     */
    private SeetaConfSetting getClarityMLSetting() throws FileNotFoundException {
        String[] modelPath = {config.getModelPath() + File.separator + "quality_lbn.csta"};
        SeetaDevice device = SeetaDevice.SEETA_DEVICE_AUTO;
        int gpuId = 0;
        if(Objects.nonNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? SeetaDevice.SEETA_DEVICE_CPU : SeetaDevice.SEETA_DEVICE_GPU;
            if(config.getGpuId() >= 0 && device == SeetaDevice.SEETA_DEVICE_GPU){
                gpuId = config.getGpuId();
            }
        }
        SeetaConfSetting setting = new SeetaConfSetting(new SeetaModelSetting(gpuId, modelPath, device));
        return setting;
    }

    /**
     * 获取人脸姿态模型配置(深度学习)
     * @return
     */
    private SeetaConfSetting getPoseMLSetting() throws FileNotFoundException {
        String[] modelPath = {config.getModelPath() + File.separator + "pose_estimation.csta"};
        SeetaDevice device = SeetaDevice.SEETA_DEVICE_AUTO;
        int gpuId = 0;
        if(Objects.nonNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? SeetaDevice.SEETA_DEVICE_CPU : SeetaDevice.SEETA_DEVICE_GPU;
            if(config.getGpuId() >= 0 && device == SeetaDevice.SEETA_DEVICE_GPU){
                gpuId = config.getGpuId();
            }
        }
        SeetaConfSetting setting = new SeetaConfSetting(new SeetaModelSetting(gpuId, modelPath, device));
        return setting;
    }

    @Override
    public R<FaceQualitySummary> evaluateAll(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return evaluateAll(image, rectangle, keyPoints);
    }

    @Override
    public R<FaceQualitySummary> evaluateAll(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints) {
        //参数检查
        if(!ImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        if(Objects.isNull(rectangle)){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "rectangle为空");
        }
        if(Objects.isNull(keyPoints) || keyPoints.isEmpty()){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "keyPoints为空");
        }
        QualityOfBrightness qualityOfBrightness = null;
        QualityOfClarity qualityOfClarity = null;
        QualityOfIntegrity qualityOfIntegrity = null;
        QualityOfPose qualityOfPose = null;
        QualityOfResolution qualityOfResolution = null;
        try {
            if(Objects.isNull(this.qualityOfBrightnessPool)){
                this.qualityOfBrightnessPool = new QualityOfBrightnessPool(new SeetaConfSetting());
            }
            if(Objects.isNull(this.qualityOfClarityPool)){
                this.qualityOfClarityPool = new QualityOfClarityPool(new SeetaConfSetting());
            }
            if(Objects.isNull(this.qualityOfIntegrityPool)){
                this.qualityOfIntegrityPool = new QualityOfIntegrityPool(new SeetaConfSetting());
            }
            if(Objects.isNull(this.qualityOfPosePool)){
                this.qualityOfPosePool = new QualityOfPosePool(new SeetaConfSetting());
            }
            if(Objects.isNull(this.qualityOfResolutionPool)){
                this.qualityOfResolutionPool = new QualityOfResolutionPool(new SeetaConfSetting());
            }
            FaceQualitySummary summary = new FaceQualitySummary();
            qualityOfBrightness = qualityOfBrightnessPool.borrowObject();
            qualityOfClarity = qualityOfClarityPool.borrowObject();
            qualityOfIntegrity = qualityOfIntegrityPool.borrowObject();
            qualityOfPose = qualityOfPosePool.borrowObject();
            qualityOfResolution = qualityOfResolutionPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = FaceUtils.convertToSeetaRect(rectangle);
            SeetaPointF[] pointFS = FaceUtils.convertToSeetaPointF(keyPoints);
            float[] scoresBrightness = new float[1];
            QualityOfBrightness.QualityLevel level = qualityOfBrightness.check(imageData, seetaRect, pointFS, scoresBrightness);
            summary.setBrightness(new FaceQualityResult(scoresBrightness[0], QualityGrade.valueOf(level.name())));
            float[] scoresClarity = new float[1];
            QualityOfClarity.QualityLevel clarityLevel = qualityOfClarity.check(imageData, seetaRect, pointFS, scoresClarity);
            summary.setClarity(new FaceQualityResult(scoresClarity[0], QualityGrade.valueOf(clarityLevel.name())));
            float[] scoresIntegrity = new float[1];
            QualityOfIntegrity.QualityLevel integrityLevel = qualityOfIntegrity.check(imageData, seetaRect, pointFS, scoresIntegrity);
            summary.setCompleteness(new FaceQualityResult(scoresIntegrity[0], QualityGrade.valueOf(integrityLevel.name())));
            float[] scoresPose = new float[1];
            QualityOfPose.QualityLevel poseLevel = qualityOfPose.check(imageData, seetaRect, pointFS, scoresPose);
            summary.setPose(new FaceQualityResult(scoresPose[0], QualityGrade.valueOf(poseLevel.name())));
            float[] scoresResolution = new float[1];
            QualityOfResolution.QualityLevel resolutionLevel = qualityOfResolution.check(imageData, seetaRect, pointFS, scoresResolution);
            summary.setResolution(new FaceQualityResult(scoresResolution[0], QualityGrade.valueOf(resolutionLevel.name())));
            return R.ok(summary);
        } catch (Exception e) {
            throw new FaceException("亮度评估错误", e);
        } finally {
            PoolUtils.returnToPool(qualityOfBrightnessPool, qualityOfBrightness);
            PoolUtils.returnToPool(qualityOfClarityPool, qualityOfClarity);
            PoolUtils.returnToPool(qualityOfIntegrityPool, qualityOfIntegrity);
            PoolUtils.returnToPool(qualityOfPosePool, qualityOfPose);
            PoolUtils.returnToPool(qualityOfResolutionPool, qualityOfResolution);
        }
    }

    @Override
    public R<FaceQualitySummary> evaluateAll(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return evaluateAll(ImageIO.read(new ByteArrayInputStream(imageData)), rectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public void close() throws Exception {
        if(Objects.nonNull(qualityOfBrightnessPool)){
            qualityOfBrightnessPool.close();
        }
        if(Objects.nonNull(qualityOfClarityPool)){
            qualityOfClarityPool.close();
        }
        if(Objects.nonNull(qualityOfLBNPool)){
            qualityOfLBNPool.close();
        }
        if(Objects.nonNull(qualityOfIntegrityPool)){
            qualityOfIntegrityPool.close();
        }
        if(Objects.nonNull(qualityOfPosePool)){
            qualityOfPosePool.close();
        }
        if(Objects.nonNull(qualityOfPoseExPool)){
            qualityOfPoseExPool.close();
        }
        if(Objects.nonNull(qualityOfResolutionPool)){
            qualityOfResolutionPool.close();
        }
    }
}

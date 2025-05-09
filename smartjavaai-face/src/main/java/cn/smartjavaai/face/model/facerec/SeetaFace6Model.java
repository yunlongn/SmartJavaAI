package cn.smartjavaai.face.model.facerec;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceModelConfig;
import cn.smartjavaai.face.dao.FaceDao;
import cn.smartjavaai.face.entity.FaceData;
import cn.smartjavaai.face.entity.FaceResult;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.utils.FaceUtils;
import com.seeta.pool.*;
import com.seeta.sdk.*;
import cn.smartjavaai.face.seetaface.NativeLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SeetaFace6 人脸算法
 * @author dwj
 */
@SuppressWarnings("AliMissingOverrideAnnotation")
@Slf4j
public class SeetaFace6Model extends AbstractFaceModel {


    private FaceModelConfig config;

    private FaceDetectorPool faceDetectorPool;
    private FaceRecognizerPool faceRecognizerPool;
    private FaceLandmarkerPool faceLandmarkerPool;

    private FaceDatabasePool faceDatabasePool;

    /**
     * 默认相似度阈值
     */
    public static final float SEETAFACE_DEFAULT_SIMILARITY_THRESHOLD = 0.62F;


    @Override
    public void loadModel(FaceModelConfig config) {
        this.config = config;
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
        //设置默认相似度阈值
        if(config.getSimilarityThreshold() <= 0){
            config.setSimilarityThreshold(SEETAFACE_DEFAULT_SIMILARITY_THRESHOLD);
        }
        //加载依赖库
        NativeLoader.loadNativeLibraries(config.getDevice());
        log.info("Loading seetaFace6 library successfully.");
        String[] faceDetectorModelPath = {config.getModelPath() + File.separator + "face_detector.csta"};
        String[] faceRecognizerModelPath = {config.getModelPath() + File.separator + "face_recognizer.csta"};
        String[] faceLandmarkerModelPath = {config.getModelPath() + File.separator + "face_landmarker_pts5.csta"};
        SeetaDevice device = SeetaDevice.SEETA_DEVICE_AUTO;
        int gpuId = 0;
        if(Objects.nonNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? SeetaDevice.SEETA_DEVICE_CPU : SeetaDevice.SEETA_DEVICE_GPU;
            if(config.getGpuId() >= 0 && device == SeetaDevice.SEETA_DEVICE_GPU){
                gpuId = config.getGpuId();
            }
        }
        try {
            SeetaModelSetting faceDetectorPoolSetting = new SeetaModelSetting(gpuId, faceDetectorModelPath, device);
            SeetaConfSetting faceDetectorPoolConfSetting = new SeetaConfSetting(faceDetectorPoolSetting);

            SeetaModelSetting faceRecognizerPoolSetting = new SeetaModelSetting(gpuId, faceRecognizerModelPath, device);
            SeetaConfSetting faceRecognizerPoolConfSetting = new SeetaConfSetting(faceRecognizerPoolSetting);

            SeetaModelSetting faceLandmarkerPoolSetting = new SeetaModelSetting(gpuId, faceLandmarkerModelPath, device);
            SeetaConfSetting faceLandmarkerPoolConfSetting = new SeetaConfSetting(faceLandmarkerPoolSetting);

            SeetaModelSetting faceDatabasePoolSetting = new SeetaModelSetting(gpuId, faceRecognizerModelPath, device);
            SeetaConfSetting faceDatabasePoolConfSetting = new SeetaConfSetting(faceDatabasePoolSetting);

            this.faceDetectorPool = new FaceDetectorPool(faceDetectorPoolConfSetting);
            this.faceRecognizerPool = new FaceRecognizerPool(faceRecognizerPoolConfSetting);
            this.faceLandmarkerPool = new FaceLandmarkerPool(faceLandmarkerPoolConfSetting);
            this.faceDatabasePool = new FaceDatabasePool(faceDatabasePoolConfSetting);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("start load faceDb...");
                        loadFaceDb();
                        log.info("Load faceDb success!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (FileNotFoundException e) {
            throw new FaceException(e);
        }

    }

    @Override
    public DetectionResponse detect(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return detect(image);
    }

    @Override
    public DetectionResponse detect(InputStream imageInputStream) {
        if(Objects.isNull(imageInputStream)){
            throw new FaceException("图像输入流无效");
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageInputStream);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }
        return detect(image);
    }

    @Override
    public DetectionResponse detect(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        FaceDetector predictor = null;
        FaceLandmarker faceLandmarker = null;
        try {
            predictor = faceDetectorPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            SeetaRect[] seetaResult = predictor.Detect(imageData);
            List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
            for(SeetaRect seetaRect : seetaResult){
                //提取人脸的5点人脸标识
                SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaRect, pointFS);
                seetaPointFSList.add(pointFS);
            }
            return FaceUtils.convertToDetectionResponse(seetaResult, config, seetaPointFSList);
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    faceDetectorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public DetectionResponse detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public void detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        try {
            //创建保存路径
            Path imageOutputPath = Paths.get(outputPath);
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
            } catch (IOException e) {
                throw new FaceException("无效图片路径", e);
            }
            DetectionResponse result = detect(image);
            if(Objects.isNull(result) || Objects.isNull(result.getDetectionInfoList()) || result.getDetectionInfoList().isEmpty()){
                throw new FaceException("未识别到人脸");
            }
            //绘制人脸框
            FaceUtils.drawBoundingBoxes(image, result, imageOutputPath.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new FaceException(e);
        }
    }

    @Override
    public BufferedImage detectAndDraw(BufferedImage sourceImage) {
        if(!ImageUtils.isImageValid(sourceImage)){
            throw new FaceException("图像无效");
        }
        DetectionResponse detectedObjects = detect(sourceImage);
        if(Objects.isNull(detectedObjects) || Objects.isNull(detectedObjects.getDetectionInfoList()) || detectedObjects.getDetectionInfoList().isEmpty()){
            throw new FaceException("未识别到人脸");
        }
        //绘制人脸框
        try {
            return FaceUtils.drawBoundingBoxes(sourceImage, detectedObjects);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取5点坐标,循序依次为，左眼中心、右眼中心、鼻尖、左嘴角和右嘴角
     * @param imageData
     * @return
     */
    private SeetaPointF[] getMaskPoint(SeetaImageData imageData) {
        FaceDetector faceDetector = null;
        FaceLandmarker faceLandmarker = null;
        try {
            faceDetector = faceDetectorPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            //检测人脸
            SeetaRect[] seetaResult = faceDetector.Detect(imageData);
            if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                throw new FaceException("未检测到人脸");
            }
            //提取第一个人脸的5点人脸标识
            SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
            faceLandmarker.mark(imageData, seetaResult[0], pointFS);
            return pointFS;
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (faceDetector != null) {
                try {
                    faceDetectorPool.returnObject(faceDetector); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    /**
     * 裁剪人脸
     * @param imageData
     * @return
     */
    private SeetaImageData getMaxCropFace(SeetaImageData imageData){
        FaceRecognizer faceRecognizer = null;
        try {
            faceRecognizer = faceRecognizerPool.borrowObject();
            //提取第一个人脸的5点人脸标识
            SeetaPointF[] pointFS = getMaskPoint(imageData);
            //裁剪人脸
            SeetaImageData cropImageData = new SeetaImageData(faceRecognizer.GetCropFaceWidthV2(), faceRecognizer.GetCropFaceHeightV2(), faceRecognizer.GetCropFaceChannelsV2());
            faceRecognizer.CropFaceV2(imageData, pointFS, cropImageData);
            return cropImageData;
        } catch (Exception e) {
            throw new FaceException(e);
        }finally {
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }


    @Override
    public float calculSimilar(float[] feature1, float[] feature2) {
        if(Objects.isNull(feature1) || Objects.isNull(feature2)){
            throw new FaceException("特征向量无效");
        }
        FaceRecognizer faceRecognizer = null;
        try {
            faceRecognizer = faceRecognizerPool.borrowObject();
            return faceRecognizer.CalculateSimilarity(feature1, feature2);
        } catch (Exception e) {
            throw new FaceException(e);
        }finally {
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public float featureComparison(String imagePath1, String imagePath2) {
        if(!FileUtils.isFileExists(imagePath1) || !FileUtils.isFileExists(imagePath2)){
            throw new FaceException("图像文件不存在");
        }
        BufferedImage image1 = null;
        BufferedImage image2 = null;
        try {
            image1 = ImageIO.read(new File(Paths.get(imagePath1).toAbsolutePath().toString()));
            image2 = ImageIO.read(new File(Paths.get(imagePath2).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return featureComparison(image1, image2);
    }

    @Override
    public float featureComparison(InputStream inputStream1, InputStream inputStream2) {
        if(Objects.isNull(inputStream1) || Objects.isNull(inputStream2)){
            throw new FaceException("图像输入流无效");
        }
        BufferedImage image1 = null;
        BufferedImage image2 = null;
        try {
            image1 = ImageIO.read(inputStream1);
            image2 = ImageIO.read(inputStream2);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }
        return featureComparison(image1, image2);
    }


    @Override
    public float featureComparison(BufferedImage image1, BufferedImage image2) {
        if(!ImageUtils.isImageValid(image1) || !ImageUtils.isImageValid(image2)){
            throw new FaceException("图像无效");
        }
        SeetaImageData imageData1 = new SeetaImageData(image1.getWidth(), image1.getHeight(), 3);
        imageData1.data = ImageUtils.getMatrixBGR(image1);

        SeetaImageData imageData2 = new SeetaImageData(image2.getWidth(), image2.getHeight(), 3);
        imageData2.data = ImageUtils.getMatrixBGR(image2);


        FaceRecognizer faceRecognizer = null;
        FaceDatabase faceDatabase = null;
        FaceLandmarker faceLandmarker = null;
        FaceDetector faceDetector = null;
        try {
            faceRecognizer = faceRecognizerPool.borrowObject();
            faceDatabase = faceDatabasePool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            faceDetector = faceDetectorPool.borrowObject();

            //检测人脸
            SeetaRect[] seetaResult = faceDetector.Detect(imageData1);
            if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                throw new FaceException("未检测到人脸");
            }
            //提取第一个人脸的5点人脸标识
            SeetaPointF[] pointFS1 = new SeetaPointF[faceLandmarker.number()];
            faceLandmarker.mark(imageData1, seetaResult[0], pointFS1);

            //裁剪人脸
            SeetaImageData cropImageData1 = new SeetaImageData(faceRecognizer.GetCropFaceWidthV2(), faceRecognizer.GetCropFaceHeightV2(), faceRecognizer.GetCropFaceChannelsV2());
            faceRecognizer.CropFaceV2(imageData1, pointFS1, cropImageData1);

            //图片2：检测人脸
            SeetaRect[] seetaResult2 = faceDetector.Detect(imageData2);
            if(Objects.isNull(seetaResult2) || seetaResult2.length == 0){
                throw new FaceException("未检测到人脸");
            }
            //图片2：提取第一个人脸的5点人脸标识
            SeetaPointF[] pointFS2 = new SeetaPointF[faceLandmarker.number()];
            faceLandmarker.mark(imageData2, seetaResult2[0], pointFS2);

            //图片2：裁剪人脸
            SeetaImageData cropImageData2 = new SeetaImageData(faceRecognizer.GetCropFaceWidthV2(), faceRecognizer.GetCropFaceHeightV2(), faceRecognizer.GetCropFaceChannelsV2());
            faceRecognizer.CropFaceV2(imageData2, pointFS2, cropImageData2);

            return faceDatabase.CompareByCroppedFace(cropImageData1, cropImageData2);
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException(e);
        }finally {
            if (faceDetector != null) {
                try {
                    faceDetectorPool.returnObject(faceDetector); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceDatabase != null) {
                try {
                    faceDatabasePool.returnObject(faceDatabase); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }

    }




    @Override
    public float featureComparison(byte[] imageData1, byte[] imageData2) {
        if(Objects.isNull(imageData1) || Objects.isNull(imageData2)){
            throw new FaceException("图像无效");
        }
        BufferedImage image1 = null;
        BufferedImage image2 = null;
        try {
            image1 = ImageIO.read(new ByteArrayInputStream(imageData1));
            image2 = ImageIO.read(new ByteArrayInputStream(imageData2));
        } catch (IOException e) {
            throw new FaceException("无效图片", e);
        }
        return featureComparison(image1, image2);
    }

    @Override
    public boolean register(String key, String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return register(key, bufferedImage);
    }


    @Override
    public boolean register(String key, BufferedImage image) {
        if(!checkFaceDb()){
            throw new FaceException("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        FaceDatabase faceDatabase = null;
        try {
            faceDatabase = faceDatabasePool.borrowObject();
            SeetaImageData cropImageData = getMaxCropFace(imageData);
            long index = faceDatabase.RegisterByCroppedFace(cropImageData);
            if (index < 0) {
                log.info("register face fail: key={}, index={}", key, index);
                return false;
            }
            //持久化到sqlite数据库
            FaceData face = new FaceData();
            face.setKey(key);
            face.setIndex(index);
            face.setImgData(cropImageData.data);
            try {
                new FaceDao(config.getFaceDbPath()).save(face);
            } catch (SQLException | ClassNotFoundException e) {
                throw new FaceException("保存人脸库失败", e);
            }
            return true;
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException(e);
        }finally {
            if (faceDatabase != null) {
                try {
                    faceDatabasePool.returnObject(faceDatabase); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public boolean register(String key, byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new FaceException(e);
        }
        return register(key, bufferedImage);
    }

    @Override
    public boolean register(String key, InputStream inputStream) {
        if(!checkFaceDb()){
            throw new FaceException("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }
        if(Objects.isNull(inputStream)){
            throw new FaceException("图像输入流无效");
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new FaceException("无效的图片输入流", e);
        }
        return register(key, image);
    }

    /**
     * 注册已裁剪后人脸
     * @param key
     * @param faceData
     * @return
     */
    private boolean registerCroppedFace(String key, FaceData faceData) {
        FaceDatabase faceDatabase = null;
        try {
            faceDatabase = faceDatabasePool.borrowObject();
            SeetaImageData cropImageData = new SeetaImageData(faceData.getWidth(), faceData.getHeight(), faceData.getChannel());
            cropImageData.data = faceData.getImgData();
            long index = faceDatabase.RegisterByCroppedFace(cropImageData);
            if (index < 0) {
                log.info("register face fail: key={}, index={}", key, index);
                return false;
            }
            int rows = 0;
            try {
                rows = new FaceDao(config.getFaceDbPath()).updateIndex(index, faceData);
            } catch (SQLException | ClassNotFoundException e) {
                throw new FaceException(e);
            }
            return rows > 0;
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException(e);
        }finally {
            if (faceDatabase != null) {
                try {
                    faceDatabasePool.returnObject(faceDatabase); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }



    @Override
    public FaceResult search(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return search(bufferedImage);
    }

    @Override
    public FaceResult search(InputStream inputStream) {
        if(Objects.isNull(inputStream)){
            throw new FaceException("图像输入流无效");
        }
        if(!checkFaceDb()){
            throw new FaceException("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }
        return search(image);
    }

    @Override
    public FaceResult search(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        FaceDatabase faceDatabase = null;
        try {
            faceDatabase = faceDatabasePool.borrowObject();
            SeetaPointF[] points = getMaskPoint(imageData);
            long[] index = new long[1];
            float[] similarity = new float[1];
            long result = faceDatabase.QueryTop(imageData, points, 1, index, similarity);
            if(result < 1){
                return null;
            }
            //检查相似度
            if(similarity[0] < config.getSimilarityThreshold()){
                return null;
            }
            return searchFaceDb(index[0], similarity[0]);
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException(e);
        }finally {
            if (faceDatabase != null) {
                try {
                    faceDatabasePool.returnObject(faceDatabase); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public FaceResult search(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new FaceException(e);
        }
        return search(bufferedImage);
    }

    @Override
    public long removeRegister(String... keys) {
        if(keys == null || keys.length == 0){
            throw new FaceException("keys不允许为空");
        }
        if(!checkFaceDb()){
            throw new FaceException("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }

        FaceDatabase faceDatabase = null;
        try {
            List<Long> list = new FaceDao(config.getFaceDbPath()).findIndexList(keys);
            if (list == null) {
                return 0;
            }
            faceDatabase = faceDatabasePool.borrowObject();
            int rows = 0;
            for (long index : list) {
                int row = faceDatabase.Delete(index);
                rows += row;
            }
            new FaceDao(config.getFaceDbPath()).deleteFace(keys);
            return rows;
        } catch (Exception e) {
            throw new FaceException(e);
        }finally {
            if (faceDatabase != null) {
                try {
                    faceDatabasePool.returnObject(faceDatabase); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public long clearFace(){
        if(!checkFaceDb()){
            throw new FaceException("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }

        FaceDatabase faceDatabase = null;
        try {
            faceDatabase = faceDatabasePool.borrowObject();
            faceDatabase.Clear();
            long rows = 0;
            try {
                rows = new FaceDao(config.getFaceDbPath()).deleteAll();
            } catch (SQLException | ClassNotFoundException e) {
                throw new FaceException("删除人脸库失败", e);
            }
            return rows;
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException(e);
        }finally {
            if (faceDatabase != null) {
                try {
                    faceDatabasePool.returnObject(faceDatabase); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    /**
     * 检查是否存在人脸库
     * @return
     */
    private boolean checkFaceDb(){
        if(Objects.nonNull(config) && StringUtils.isNotBlank(config.getFaceDbPath())){
            File file = new File(config.getFaceDbPath());
            return file.exists() && file.isFile();
        }
        return false;
    }

    private FaceResult searchFaceDb(long index,float similar) {
        if(index >= 0){
            String key = null;
            try {
                key = new FaceDao(config.getFaceDbPath()).findKeyByIndex(index);
            } catch (SQLException | ClassNotFoundException e) {
                throw new FaceException("查询人脸库失败", e);
            }
            return new FaceResult(key, similar);
        }
        return null;
    }


    /**
     * 加载人脸库
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private void loadFaceDb() {
        if(!checkFaceDb()){
            log.info("未配置人脸库");
            return;
        }
        //分页查询人脸库
        int pageNo = 0, pageSize = 100;
        while (true) {
            List<FaceData> list = null;
            try {
                list = new FaceDao(config.getFaceDbPath()).findFace(pageNo, pageSize);
            } catch (SQLException | ClassNotFoundException e) {
                throw new FaceException("查询人脸库失败", e);
            }
            if (list == null) {
                break;
            }
            list.forEach(face -> {
                try {
                    registerCroppedFace(face.getKey(), face);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            if (list.size() < pageSize) {
                break;
            }
            pageNo++;
        }
    }


    @Override
    public List<float[]> extractFeatures(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return extractFeatures(image);
    }

    @Override
    public List<float[]> extractFeatures(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return extractFeatures(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public List<float[]> extractFeatures(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        List<float[]> featureList = new ArrayList<float[]>();
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        FaceDetector faceDetector = null;
        FaceLandmarker faceLandmarker = null;
        FaceRecognizer faceRecognizer = null;
        try {
            faceDetector = faceDetectorPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            faceRecognizer = faceRecognizerPool.borrowObject();
            //检测人脸
            SeetaRect[] seetaResult = faceDetector.Detect(imageData);
            if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                throw new FaceException("未检测到人脸");
            }
            for(SeetaRect seetaRect : seetaResult){
                //提取人脸的5点人脸标识
                SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaRect, pointFS);
                //提取特征
                float[] features = new float[faceRecognizer.GetExtractFeatureSize()];
                //CropFaceV2 + ExtractCroppedFace 已包含裁剪+人脸对齐
                boolean isSuccess = faceRecognizer.Extract(imageData, pointFS, features);
                if(!isSuccess){
                    throw new FaceException("人脸特征提取失败");
                }
                featureList.add(features);
            }
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (faceDetector != null) {
                try {
                    faceDetectorPool.returnObject(faceDetector); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
        return featureList;
    }


    @Override
    public float[] extractTopFaceFeature(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        float[] features = null;
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        FaceDetector faceDetector = null;
        FaceLandmarker faceLandmarker = null;
        FaceRecognizer faceRecognizer = null;
        try {
            faceDetector = faceDetectorPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            faceRecognizer = faceRecognizerPool.borrowObject();
            //检测人脸
            SeetaRect[] seetaResult = faceDetector.Detect(imageData);
            if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                throw new FaceException("未检测到人脸");
            }
            //提取人脸的5点人脸标识
            SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
            faceLandmarker.mark(imageData, seetaResult[0], pointFS);
            //提取特征
            features = new float[faceRecognizer.GetExtractFeatureSize()];
            //CropFaceV2 + ExtractCroppedFace 已包含裁剪+人脸对齐
            boolean isSuccess = faceRecognizer.Extract(imageData, pointFS, features);
            if(!isSuccess){
                throw new FaceException("人脸特征提取失败");
            }
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (faceDetector != null) {
                try {
                    faceDetectorPool.returnObject(faceDetector); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
        return features;
    }

    @Override
    public float[] extractTopFaceFeature(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return extractTopFaceFeature(image);
    }

    @Override
    public float[] extractTopFaceFeature(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return extractTopFaceFeature(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }


}

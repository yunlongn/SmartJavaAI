package cn.smartjavaai.face.model;

import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.AbstractFaceModel;
import cn.smartjavaai.face.FaceModelConfig;
import cn.smartjavaai.face.dao.FaceDao;
import cn.smartjavaai.face.entity.FaceData;
import cn.smartjavaai.face.entity.FaceResult;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.utils.FaceUtils;
import com.seetaface.NativeLoader;
import com.seetaface.SeetaFace6JNI;
import com.seetaface.model.RecognizeResult;
import com.seetaface.model.SeetaImageData;
import com.seetaface.model.SeetaRect;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
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

    private static final Object lock = new Object(); // 全局锁


    @Override
    public void loadModel(FaceModelConfig config) {
        this.config = config;
        if (NativeLoader.seetaFace6SDK == null) {
            synchronized (lock) {
                if(StringUtils.isBlank(config.getModelPath())){
                    throw new FaceException("modelPath is null");
                }
                //加载依赖库
                NativeLoader.loadNativeLibraries(config.getModelPath());
                log.info("Loading seetaFace6 library successfully.");
                NativeLoader.seetaFace6SDK = new SeetaFace6JNI();
                //加载模型
                boolean isSuccess = NativeLoader.seetaFace6SDK.initModel(config.getModelPath());
                if(!isSuccess){
                    throw new FaceException("seetaFace6模型初始化失败," + config.getModelPath());
                }
                log.info("Load seetaFace6 model success!");
                new Thread(new Runnable() {
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
            }
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
        synchronized (lock) {
            SeetaRect[] seetaResult = NativeLoader.seetaFace6SDK.detect(imageData);
            return FaceUtils.convertToDetectionResponse(seetaResult, config);
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
            if(Objects.isNull(result) || Objects.isNull(result.getRectangleList()) || result.getRectangleList().isEmpty()){
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
        if(Objects.isNull(detectedObjects) || Objects.isNull(detectedObjects.getRectangleList()) || detectedObjects.getRectangleList().isEmpty()){
            throw new FaceException("未识别到人脸");
        }
        //绘制人脸框
        try {
            return FaceUtils.drawBoundingBoxes(sourceImage, detectedObjects);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float[] featureExtraction(BufferedImage image) {
        if(!ImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        synchronized (lock) {
            return NativeLoader.seetaFace6SDK.extractMaxFace(imageData);
        }

    }

    @Override
    public float[] featureExtraction(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return featureExtraction(image);
    }

    @Override
    public float[] featureExtraction(InputStream inputStream) {
        if(Objects.isNull(inputStream)){
            throw new FaceException("图像输入流无效");
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }
        return featureExtraction(image);
    }

    @Override
    public float[] featureExtraction(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return featureExtraction(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public float calculSimilar(float[] feature1, float[] feature2) {
        if(Objects.isNull(feature1) || Objects.isNull(feature2)){
            throw new FaceException("特征向量无效");
        }
        synchronized (lock) {
            return NativeLoader.seetaFace6SDK.calculateSimilarity(feature1, feature2);
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
        synchronized (lock) {
            //裁剪
            byte[][] cropImg1 = NativeLoader.seetaFace6SDK.crop(imageData1);
            byte[][] cropImg2 = NativeLoader.seetaFace6SDK.crop(imageData2);
            if(cropImg1 == null || cropImg1.length == 0){
                throw new FaceException("未发现人脸");
            }
            if(cropImg2 == null || cropImg2.length == 0){
                throw new FaceException("未发现人脸");
            }
            BufferedImage cropImage1 = ImageUtils.bgrToBufferedImage(cropImg1[0], 256, 256);
            BufferedImage cropImage2 = ImageUtils.bgrToBufferedImage(cropImg2[0], 256, 256);

            SeetaImageData cropImageData1 = new SeetaImageData(cropImage1.getWidth(), cropImage1.getHeight(), 3);
            cropImageData1.data = ImageUtils.getMatrixBGR(cropImage1);
            SeetaImageData cropImageData2 = new SeetaImageData(cropImage2.getWidth(), cropImage2.getHeight(), 3);
            cropImageData2.data = ImageUtils.getMatrixBGR(cropImage2);
            return NativeLoader.seetaFace6SDK.compare(cropImageData1, cropImageData2);
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
        synchronized (lock) {
            byte[][] bytes = NativeLoader.seetaFace6SDK.crop(imageData);
            if (bytes == null || bytes.length == 0) {
                log.info("register face fail: key={}, error=no valid face", key);
                return false;
            }
            long index = NativeLoader.seetaFace6SDK.registerCroppedFace(bytes[0]);
            if (index < 0) {
                log.info("register face fail: key={}, index={}", key, index);
                return false;
            }
            //持久化到sqlite数据库
            FaceData face = new FaceData();
            face.setKey(key);
            face.setIndex(index);
            face.setImgData(bytes[0]);
            try {
                new FaceDao(config.getFaceDbPath()).save(face);
            } catch (SQLException | ClassNotFoundException e) {
                throw new FaceException("保存人脸库失败", e);
            }
            return true;
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
    private boolean register(String key, FaceData faceData) {
        synchronized (lock) {
            long index = NativeLoader.seetaFace6SDK.registerCroppedFace(faceData.getImgData());
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
        synchronized (lock) {
            RecognizeResult recognizeResult = NativeLoader.seetaFace6SDK.query(imageData);
            return searchFaceDb(recognizeResult);
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
        synchronized (lock) {
            try {
                List<Long> list = new FaceDao(config.getFaceDbPath()).findIndexList(keys);
                if (list == null) {
                    return 0;
                }
                long[] array = list.stream().mapToLong(Long::longValue).toArray();
                long rows = NativeLoader.seetaFace6SDK.delete(array);
                new FaceDao(config.getFaceDbPath()).deleteFace(keys);
                return rows;
            } catch (SQLException | ClassNotFoundException e) {
                throw new FaceException(e);
            }
        }

    }

    @Override
    public long clearFace(){
        if(!checkFaceDb()){
            throw new FaceException("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }
        synchronized (lock) {
            long rows = NativeLoader.seetaFace6SDK.delete(new long[]{-1});
            try {
                new FaceDao(config.getFaceDbPath()).deleteAll();
            } catch (SQLException | ClassNotFoundException e) {
                throw new FaceException("删除人脸库失败", e);
            }
            return rows;
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

    private FaceResult searchFaceDb(RecognizeResult recognizeResult) {
        if(recognizeResult != null && recognizeResult.index >= 0){
            String key = null;
            synchronized (lock) {
                try {
                    key = new FaceDao(config.getFaceDbPath()).findKeyByIndex(recognizeResult.index);
                } catch (SQLException | ClassNotFoundException e) {
                    throw new FaceException("查询人脸库失败", e);
                }
                return new FaceResult(key, recognizeResult.similar);
            }
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
                    register(face.getKey(), face);
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
}

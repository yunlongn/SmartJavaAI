package cn.smartjavaai.face.algo;

import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.Rectangle;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.AbstractFaceAlgorithm;
import cn.smartjavaai.face.FaceDetectedResult;
import cn.smartjavaai.face.ModelConfig;
import cn.smartjavaai.face.dao.FaceDao;
import cn.smartjavaai.face.entity.FaceData;
import cn.smartjavaai.face.entity.FaceResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SeetaFace6 人脸算法
 * @author dwj
 */
@Slf4j
public class SeetaFace6Algo extends AbstractFaceAlgorithm {


    private ModelConfig config;


    @Override
    public void loadModel(ModelConfig config) throws Exception {
        this.config = config;
        if (NativeLoader.seetaFace6SDK == null) {
            synchronized (SeetaFace6JNI.class) {
                if(StringUtils.isBlank(config.getModelPath())){
                    throw new Exception("modelPath is null");
                }
                //加载依赖库
                NativeLoader.loadNativeLibraries(config.getModelPath());
                log.info("Loading seetaFace6 library successfully.");
                NativeLoader.seetaFace6SDK = new SeetaFace6JNI();
                //加载模型
                boolean isSuccess = NativeLoader.seetaFace6SDK.initModel(config.getModelPath());
                if(!isSuccess){
                    throw new Exception("seetaFace6模型初始化失败," + config.getModelPath());
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
    public FaceDetectedResult detect(String imagePath) throws Exception {
        // 将图片路径转换为 BufferedImage
        BufferedImage image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        SeetaRect[] seetaResult = NativeLoader.seetaFace6SDK.detect(imageData);
        return convertToFaceDetectedResult(seetaResult);
    }

    @Override
    public FaceDetectedResult detect(InputStream imageInputStream) throws Exception {
        BufferedImage image = ImageIO.read(imageInputStream);
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        SeetaRect[] seetaResult = NativeLoader.seetaFace6SDK.detect(imageData);
        return convertToFaceDetectedResult(seetaResult);
    }

    @Override
    public float[] featureExtraction(String imagePath) throws Exception {
        BufferedImage image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        return NativeLoader.seetaFace6SDK.extractMaxFace(imageData);
    }

    @Override
    public float[] featureExtraction(InputStream inputStream) throws Exception {
        BufferedImage image = ImageIO.read(inputStream);
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        return NativeLoader.seetaFace6SDK.extractMaxFace(imageData);
    }

    @Override
    public float calculSimilar(float[] feature1, float[] feature2) throws Exception {
        return NativeLoader.seetaFace6SDK.calculateSimilarity(feature1, feature2);
    }

    @Override
    public float featureComparison(String imagePath1, String imagePath2) throws Exception {
         return featureComparison(new FileInputStream(Paths.get(imagePath1).toAbsolutePath().toString()),
                 new FileInputStream(Paths.get(imagePath2).toAbsolutePath().toString()));
    }

    @Override
    public float featureComparison(InputStream inputStream1, InputStream inputStream2) throws Exception {
        BufferedImage image1 = ImageIO.read(inputStream1);
        BufferedImage image2 = ImageIO.read(inputStream2);
        SeetaImageData imageData1 = new SeetaImageData(image1.getWidth(), image1.getHeight(), 3);
        imageData1.data = ImageUtils.getMatrixBGR(image1);

        SeetaImageData imageData2 = new SeetaImageData(image2.getWidth(), image2.getHeight(), 3);
        imageData2.data = ImageUtils.getMatrixBGR(image2);
        //裁剪
        byte[][] cropImg1 = NativeLoader.seetaFace6SDK.crop(imageData1);
        byte[][] cropImg2 = NativeLoader.seetaFace6SDK.crop(imageData2);
        if(cropImg1 == null || cropImg1.length == 0){
            throw new Exception("未发现人脸");
        }
        if(cropImg2 == null || cropImg2.length == 0){
            throw new Exception("未发现人脸");
        }

        BufferedImage cropImage1 = ImageUtils.bgrToBufferedImage(cropImg1[0], 256, 256);
        BufferedImage cropImage2 = ImageUtils.bgrToBufferedImage(cropImg2[0], 256, 256);

        SeetaImageData cropImageData1 = new SeetaImageData(cropImage1.getWidth(), cropImage1.getHeight(), 3);
        cropImageData1.data = ImageUtils.getMatrixBGR(cropImage1);
        SeetaImageData cropImageData2 = new SeetaImageData(cropImage2.getWidth(), cropImage2.getHeight(), 3);
        cropImageData2.data = ImageUtils.getMatrixBGR(cropImage2);
        return NativeLoader.seetaFace6SDK.compare(cropImageData1, cropImageData2);
    }


    /**
     * 转换为FaceDetectedResult
     * @param seetaResult
     * @return
     */
    private FaceDetectedResult convertToFaceDetectedResult(SeetaRect[] seetaResult){
        FaceDetectedResult faceDetectedResult = new FaceDetectedResult();
        List<Rectangle> RectangleList = new ArrayList<Rectangle>();
        List<Double> probabilities = new ArrayList<Double>();
        if(seetaResult != null && seetaResult.length > 0){
            for(SeetaRect rect : seetaResult){
                Rectangle rectangle = new Rectangle();
                List<Point> pointList = new ArrayList<>();
                pointList.add(new Point(rect.x,rect.y));
                pointList.add(new Point(rect.x + rect.width,rect.y));
                pointList.add(new Point(rect.x,rect.y + rect.height));
                pointList.add(new Point(rect.x + rect.width,rect.y + rect.height));
                rectangle.setPointList(pointList);
                rectangle.setHeight(rect.height);
                rectangle.setWidth(rect.width);
                RectangleList.add(rectangle);
                probabilities.add(new Double(rect.score));
            }
        }
        faceDetectedResult.setProbabilities(probabilities);
        faceDetectedResult.setRectangles(RectangleList);
        return faceDetectedResult;
    }

    @Override
    public boolean register(String key, String imagePath) throws Exception {
        return register(key, new FileInputStream(Paths.get(imagePath).toAbsolutePath().toString()));
    }


    @Override
    public boolean register(String key, InputStream inputStream) throws Exception {
        if(!checkFaceDb()){
            throw new Exception("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }
        //裁剪人脸
        BufferedImage image = ImageIO.read(inputStream);
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
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
        new FaceDao(config.getFaceDbPath()).save(face);
        return true;
    }

    public boolean register(String key, FaceData faceData) throws Exception {
        long index = NativeLoader.seetaFace6SDK.registerCroppedFace(faceData.getImgData());
        if (index < 0) {
            log.info("register face fail: key={}, index={}", key, index);
            return false;
        }
        int rows = new FaceDao(config.getFaceDbPath()).updateIndex(index, faceData);
        return rows > 0;
    }



    @Override
    public FaceResult search(String imagePath) throws Exception {
        return search(new FileInputStream(Paths.get(imagePath).toAbsolutePath().toString()));
    }

    @Override
    public FaceResult search(InputStream inputStream) throws Exception{
        if(!checkFaceDb()){
            throw new Exception("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }
        BufferedImage image = ImageIO.read(inputStream);
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        RecognizeResult recognizeResult = NativeLoader.seetaFace6SDK.query(imageData);
        return searchFaceDb(recognizeResult);

    }

    @Override
    public long removeRegister(String... keys) throws Exception {
        if(!checkFaceDb()){
            throw new Exception("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }
        List<Long> list = new FaceDao(config.getFaceDbPath()).findIndexList(keys);
        if (list == null) {
            return 0;
        }
        long[] array = list.stream().mapToLong(Long::longValue).toArray();
        long rows = NativeLoader.seetaFace6SDK.delete(array);
        new FaceDao(config.getFaceDbPath()).deleteFace(keys);
        return rows;
    }

    @Override
    public long clearFace() throws Exception{
        if(!checkFaceDb()){
            throw new Exception("未找到人脸库，无法使用此功能（请检查是否配置人脸库路径）");
        }
        long rows = NativeLoader.seetaFace6SDK.delete(new long[]{-1});
        new FaceDao(config.getFaceDbPath()).deleteAll();
        return rows;
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

    private FaceResult searchFaceDb(RecognizeResult recognizeResult) throws SQLException, ClassNotFoundException {
        if(recognizeResult != null && recognizeResult.index >= 0){
            String key = new FaceDao(config.getFaceDbPath()).findKeyByIndex(recognizeResult.index);
            return new FaceResult(key, recognizeResult.similar);
        }
        return null;
    }


    /**
     * 加载人脸库
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private void loadFaceDb() throws SQLException, ClassNotFoundException {
        if(!checkFaceDb()){
            log.info("未配置人脸库");
            return;
        }
        //分页查询人脸库
        int pageNo = 0, pageSize = 100;
        while (true) {
            List<FaceData> list = new FaceDao(config.getFaceDbPath()).findFace(pageNo, pageSize);
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

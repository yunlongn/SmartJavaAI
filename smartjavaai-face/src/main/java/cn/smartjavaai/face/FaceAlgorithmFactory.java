package cn.smartjavaai.face;

import cn.smartjavaai.face.algo.RetinaFace;
import cn.smartjavaai.face.algo.UltraLightFastGenericFace;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人脸算法工厂
 * @author dwj
 */
public class FaceAlgorithmFactory {

    /**
     * 算法注册表
     */
    private static final Map<String, Class<? extends FaceAlgorithm>> registry =
            new ConcurrentHashMap<>();

    /**
     * 注册算法
     * @param name
     * @param clazz
     */
    public static void registerAlgorithm(String name, Class<? extends FaceAlgorithm> clazz) {
        registry.put(name.toLowerCase(), clazz);
    }

    /**
     * 使用ModelConfig创建算法
     * @param config
     * @return
     * @throws Exception
     */
    public static FaceAlgorithm createFaceAlgorithm(ModelConfig config) throws Exception {
        Class<?> clazz = registry.get(config.getAlgorithmName().toLowerCase());
        if(clazz == null){
            throw new IllegalArgumentException("Unsupported algorithm");
        }
        FaceAlgorithm algorithm = (FaceAlgorithm) clazz.newInstance();
        algorithm.loadModel(config);
        return algorithm;
    }

    /**
     * 创建默认算法
     * @return
     * @throws Exception
     */
    public static FaceAlgorithm createFaceAlgorithm() throws Exception {
        // 初始化配置
        ModelConfig config = new ModelConfig();
        config.setAlgorithmName("retinaface");
        config.setConfidenceThreshold(FaceConfig.DEFAULT_CONFIDENCE_THRESHOLD);
        config.setMaxFaceCount(FaceConfig.MAX_FACE_LIMIT);
        config.setNmsThresh(FaceConfig.NMS_THRESHOLD);
        Class<?> clazz = registry.get(config.getAlgorithmName().toLowerCase());
        if(clazz == null){
            throw new IllegalArgumentException("Unsupported algorithm");
        }
        FaceAlgorithm algorithm = (FaceAlgorithm) clazz.newInstance();
        algorithm.loadModel(config);
        return algorithm;
    }

    /**
     * 创建轻量级算法
     * @return
     * @throws Exception
     */
    public static FaceAlgorithm createLightFaceAlgorithm() throws Exception {
        // 初始化配置
        ModelConfig config = new ModelConfig();
        config.setAlgorithmName("ultralightfastgenericface");
        config.setConfidenceThreshold(FaceConfig.DEFAULT_CONFIDENCE_THRESHOLD);
        config.setMaxFaceCount(FaceConfig.MAX_FACE_LIMIT);
        config.setNmsThresh(FaceConfig.NMS_THRESHOLD);
        Class<?> clazz = registry.get(config.getAlgorithmName().toLowerCase());
        if(clazz == null){
            throw new IllegalArgumentException("Unsupported algorithm");
        }
        FaceAlgorithm algorithm = (FaceAlgorithm) clazz.newInstance();
        algorithm.loadModel(config);
        return algorithm;
    }

    // 初始化默认算法
    static {
        registerAlgorithm("retinaface", RetinaFace.class);
        registerAlgorithm("ultralightfastgenericface", UltraLightFastGenericFace.class);
    }

}

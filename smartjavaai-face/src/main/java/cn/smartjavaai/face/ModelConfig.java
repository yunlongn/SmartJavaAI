package cn.smartjavaai.face;

/**
 * 模型配置
 * @author dwj
 */

public class ModelConfig {

    /**
     * 人脸算法名称
     */
    private String algorithmName;

    /**
     * 置信度阈值
     */
    private double confidenceThreshold;

    /**
     * 非极大抑制阈值 作用：消除重叠检测框，保留最优结果
     */
    private double nmsThresh;

    /**
     * 最大检测人脸数量
     */
    private int maxFaceCount;

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public double getNmsThresh() {
        return nmsThresh;
    }

    public void setNmsThresh(double nmsThresh) {
        this.nmsThresh = nmsThresh;
    }

    public int getMaxFaceCount() {
        return maxFaceCount;
    }

    public void setMaxFaceCount(int maxFaceCount) {
        this.maxFaceCount = maxFaceCount;
    }
}

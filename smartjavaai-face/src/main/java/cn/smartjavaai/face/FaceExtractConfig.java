package cn.smartjavaai.face;

import lombok.Data;

/**
 * 人脸特征提取配置
 * @author dwj
 * @date 2025/4/24
 */
@Data
public class FaceExtractConfig {

    /**
     * 是否裁剪人脸
     */
    private boolean cropFace = true;

    /**
     * 是否对齐人脸
     */
    private boolean align = true;

    /**
     * 人脸检测模型配置
     */
    private FaceModelConfig detectModelConfig;

    public FaceExtractConfig() {
    }

    public FaceExtractConfig(boolean cropFace, boolean align, FaceModelConfig detectModelConfig) {
        this.cropFace = cropFace;
        this.align = align;
        this.detectModelConfig = detectModelConfig;
    }


}

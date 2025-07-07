package cn.smartjavaai.face.config;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.enums.ExpressionModelEnum;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dwj
 * @date 2025/7/1
 */
@Data
public class FaceExpressionConfig {

    /**
     * 模型枚举
     */
    private ExpressionModelEnum modelEnum = ExpressionModelEnum.DensNet121;

    /**
     * 模型路径
     */
    private String modelPath;

    /**
     * 设备类型
     */
    private DeviceEnum device;

    /**
     * 人脸检测模型
     */
    private FaceDetModel detectModel;

    /**
     * 是否对齐人脸
     */
    private boolean align = true;

    /**
     * 是否裁剪人脸
     */
    private boolean cropFace = true;



}

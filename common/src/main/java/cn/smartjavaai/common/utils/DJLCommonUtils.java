package cn.smartjavaai.common.utils;

import ai.djl.ndarray.NDArray;
import cn.smartjavaai.common.entity.R;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author dwj
 */
public class DJLCommonUtils {

    /**
     * 检查模型目录中是否存在 "serving.properties" 文件
     *
     * @param modelPath 模型目录路径
     * @return true 表示存在，false 表示不存在
     */
    public static boolean isServingPropertiesExists(Path modelPath) {
        if (modelPath == null || !Files.exists(modelPath)) {
            return false;
        }
        // 确定目录路径
        Path dirPath = Files.isDirectory(modelPath) ? modelPath : modelPath.getParent();
        if (dirPath == null) {
            return false; // 可能是根目录的文件
        }

        // 判断目录下的 serving.properties 是否存在
        Path servingFile = dirPath.resolve("serving.properties");
        return Files.exists(servingFile);
    }

    /**
     * 判断 NDArray 是否为空
     * @param ndArray
     * @return
     */
    public static boolean isNDArrayEmpty(NDArray ndArray){
        return Objects.isNull(ndArray) || ndArray.size() == 0;
    }


}

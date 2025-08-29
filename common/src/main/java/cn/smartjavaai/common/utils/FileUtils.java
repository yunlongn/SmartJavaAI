package cn.smartjavaai.common.utils;

import java.io.File;

/**
 * 文件操作工具类
 * @author dwj
 * @date 2025/4/4
 */
public class FileUtils {

    /**
     * 检查文件是否存在
     * @param filePath
     * @return
     */
    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();  // 确保是文件且存在
    }

    /**
     * 检查目录是否存在
     * @param path
     * @return
     */
    public static boolean isValidDirectory(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }
}

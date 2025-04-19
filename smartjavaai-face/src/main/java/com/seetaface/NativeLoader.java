package com.seetaface;


import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.FaceModelConfig;
import cn.smartjavaai.face.exception.FaceException;
import com.seeta.sdk.SeetaDevice;
import com.seeta.sdk.util.DllItem;
import com.seeta.sdk.util.LoadNativeCore;
import jdk.dynalink.linker.support.Lookup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 依赖库加载器
 * @author dwj
 */
@Slf4j
public class NativeLoader {


    private static Path seetaface6NativePath;
    private static final String[] WIN_LIBS = {"tennis.dll","tennis_haswell.dll","tennis_pentium.dll","tennis_sandy_bridge.dll","SeetaAuthorize.dll","SeetaFaceAntiSpoofingX600.dll","SeetaFaceDetector600.dll","SeetaFaceLandmarker600.dll","SeetaFaceRecognizer610.dll","SeetaFace6JNI.dll"};
    //private static final String[] WIN_LIBS = {"tennis","tennis_haswell","tennis_pentium","tennis_sandy_bridge","SeetaAuthorize","SeetaFaceAntiSpoofingX600","SeetaFaceDetector600","SeetaFaceLandmarker600","SeetaFaceRecognizer610","SeetaFace6JNI"};
    private static final String[] LINUX_CENTOS_LIBS = {"libSeetaAuthorize.so","libtennis.so","libtennis_haswell.so","libtennis_pentium.so","libtennis_sandy_bridge.so","libSeetaFaceDetector600.so","libSeetaAgePredictor600.so","libSeetaEyeStateDetector200.so","libSeetaFaceAntiSpoofingX600.so","libSeetaFaceLandmarker600.so","libSeetaFaceRecognizer610.so","libSeetaGenderPredictor600.so","libSeetaMaskDetector200.so","libSeetaPoseEstimation600.so","libSeetaFaceTracking600.so","libSeetaQualityAssessor300.so"};
    private static final String[] LINUX_UBUNTU_LIBS = {"libSeetaAuthorize.so","libtennis.so","libtennis_haswell.so","libtennis_pentium.so","libtennis_sandy_bridge.so","libSeetaFaceDetector600.so","libSeetaAgePredictor600.so","libSeetaEyeStateDetector200.so","libSeetaFaceAntiSpoofingX600.so","libSeetaFaceLandmarker600.so","libSeetaFaceRecognizer610.so","libSeetaGenderPredictor600.so","libSeetaMaskDetector200.so","libSeetaPoseEstimation600.so","libSeetaFaceTracking600.so","libSeetaQualityAssessor300.so"};

    private static final String SEETAFACE_LIB_DIR = "seetaface6";

    public static SeetaFace6JNI seetaFace6SDK;

    public static final String AMD64 = "amd64";

    public static final String x86_64 = "amd64";

    /**
     * 定义dll 路径和加载顺序的文件
     */
    private static final String PROPERTIES_FILE_NAME = "dll.properties";



    public static void loadNativeLibraries(FaceModelConfig config) {
        try {
            OsInfo osInfo = SystemUtil.getOsInfo();
            //检查当前系统是否支持
            if(!osInfo.isWindows() && !osInfo.isLinux()){
                throw new FaceException("当前系统不支持：" + osInfo.getName());
            }
            //判断硬件架构是否支持GPU
            if(config.getDevice() != null && config.getDevice().equals(DeviceEnum.GPU)){
                //GPU仅支持amd64
                if(!osInfo.getArch().contains("amd64") && !osInfo.getArch().contains("x86_64")){
                    throw new FaceException("seetaface6 GPU模型不支持当前arch：" + osInfo.getArch());
                }
            }
            seetaface6NativePath = Paths.get(Config.getCachePath(), SEETAFACE_LIB_DIR);
            //创建目录
            FileUtil.mkdir(seetaface6NativePath);
            log.info("seetaface6依赖库路径: " + seetaface6NativePath.toAbsolutePath().toString());
            //拷贝依赖库到缓存目录
            List<File> fileList = getLibFiles(osInfo, config.getDevice());
            if(fileList != null && !fileList.isEmpty()){
                // 加载依赖库文件
                fileList.forEach(file -> {
                    System.load(file.getAbsolutePath());
                    log.info(String.format("load %s finish", file.getAbsolutePath()));
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Native library loading failed", e);
        }
    }

    /**
     * 拷贝依赖库到缓存目录
     * @param osInfo
     * @return
     */
    private static List<File> getLibFiles(OsInfo osInfo,DeviceEnum deviceEnum){
        try {
            String device = getDevice(deviceEnum);
            log.info("当前设备：{}", device);
            //获取dll文件列表
            List<DllItem> baseList = new ArrayList<>();
            List<DllItem> jniList = new ArrayList<>();
            InputStream propsInputStream = LoadNativeCore.class.getResourceAsStream(getPropertiesPath());
            Props props = new Props();
            props.load(propsInputStream);
            String prefix = getPrefix();
            props.forEach((keyObj, valuObj) -> {
                String key = (String) keyObj;
                String value = (String) valuObj;
                DllItem dllItem = new DllItem();
                dllItem.setKey(key);
                if (key.contains("base")) {
                    if (value.contains("tennis")) {
                        dllItem.setValue(prefix + "base/" + device + "/" + value);
                    } else {
                        dllItem.setValue(prefix + "base/" + value);
                    }
                    baseList.add(dllItem);
                } else {
                    dllItem.setValue(prefix + value);
                    jniList.add(dllItem);
                }
            });
            //给dll文件排序
            List<String> basePath = getSortedPath(baseList);
            List<String> sdkPath = getSortedPath(jniList);
            List<File> fileList = new ArrayList<>();
            //拷贝文件到临时目录
            for (String baseSo : basePath) {
                fileList.add(extractLibrary(baseSo));
            }
            for (String sdkSo : sdkPath) {
                fileList.add(extractLibrary(sdkSo));
            }
            return fileList;
        } catch (Exception e) {
            throw new FaceException("拷贝依赖库失败",e);
        }
    }

    private static String getDevice(DeviceEnum deviceEnum) {
        String device = "CPU";
        if ("amd64".equals(getArch()) && deviceEnum != null) {
            device = deviceEnum == DeviceEnum.GPU ? "GPU" : "CPU";
        }
        return device;
    }


    /**
     * 返回路径文件前缀
     *
     * @return
     */
    private static String getPrefix() {
        String arch = getArch();
        //aarch64
        String os = SystemUtil.getOsInfo().getName();
        //Windows操作系统
        if (os != null && os.toLowerCase().startsWith("windows")) {
            os = "/windows/";
        } else if (os != null && os.toLowerCase().startsWith("linux")) {//Linux操作系统
            os = "/linux/";
        } else { //其它操作系统
            //安卓 乌班图等等，先不写
            return null;
        }
        // "/seetaface6/windows/amd64"
        return "/" + SEETAFACE_LIB_DIR + os + arch + "/";
    }

    private static String getArch() {
        String arch = SystemUtil.getOsInfo().getArch().toLowerCase();
        if (arch.startsWith("amd64")
                || arch.startsWith("x86_64")
                || arch.startsWith("x86-64")
                || arch.startsWith("x64")) {
            arch = "amd64";
        } else if (arch.contains("aarch")) {
            arch = "aarch64";
        } else if (arch.contains("arm")) {
            arch = "arm";
        }
        return arch;
    }

    /**
     * 获取dll配置文件路径
     *
     * @return String
     */
    private static String getPropertiesPath() {
        return getPrefix() + PROPERTIES_FILE_NAME;
    }



    /**
     * 拷贝依赖库到临时目录
     * @param libPath
     * @return
     * @throws IOException
     */
    private static File extractLibrary(String libPath) throws IOException {
        String resourcePath = libPath;
        try (InputStream in = com.seetaface.NativeLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException(resourcePath);
            Path path = Paths.get(resourcePath);
            String fileName = path.getFileName().toString();
            Path targetPath = seetaface6NativePath.resolve(fileName);
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("copy target path success : {}", targetPath.toAbsolutePath().toString());
            // 设置可执行权限
            if (!SystemUtil.getOsInfo().getName().toLowerCase().contains("win")) {
                targetPath.toFile().setExecutable(true);
            }
            return targetPath.toFile();
        }
    }


    /**
     * 将获得的配置进行排序 并生成路径
     *
     * @param list
     * @return List<String>
     */
    private static List<String> getSortedPath(List<DllItem> list) {
        return list.stream().sorted(Comparator.comparing(dllItem -> {
            int i = dllItem.getKey().lastIndexOf(".") + 1;
            String substring = dllItem.getKey().substring(i);
            return Integer.valueOf(substring);
        })).map(DllItem::getValue).collect(Collectors.toList());
    }

}

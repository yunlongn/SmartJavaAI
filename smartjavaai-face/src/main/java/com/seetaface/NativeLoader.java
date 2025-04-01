package com.seetaface;


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
import java.nio.file.StandardCopyOption;

/**
 * 依赖库加载器
 * @author dwj
 */
@Slf4j
public class NativeLoader {


    private static Path tempNativeDir;
    private static final String[] WIN_LIBS = {"tennis.dll","tennis_haswell.dll","tennis_pentium.dll","tennis_sandy_bridge.dll","SeetaAuthorize.dll","SeetaFaceAntiSpoofingX600.dll","SeetaFaceDetector600.dll","SeetaFaceLandmarker600.dll","SeetaFaceRecognizer610.dll","SeetaFace6JNI.dll"};
    //private static final String[] WIN_LIBS = {"tennis","tennis_haswell","tennis_pentium","tennis_sandy_bridge","SeetaAuthorize","SeetaFaceAntiSpoofingX600","SeetaFaceDetector600","SeetaFaceLandmarker600","SeetaFaceRecognizer610","SeetaFace6JNI"};
    private static final String[] LINUX_CENTOS_LIBS = {"libSeetaAuthorize.so","libtennis.so","libtennis_haswell.so","libtennis_pentium.so","libtennis_sandy_bridge.so","libSeetaFaceDetector600.so","libSeetaAgePredictor600.so","libSeetaEyeStateDetector200.so","libSeetaFaceAntiSpoofingX600.so","libSeetaFaceLandmarker600.so","libSeetaFaceRecognizer610.so","libSeetaGenderPredictor600.so","libSeetaMaskDetector200.so","libSeetaPoseEstimation600.so","libSeetaFaceTracking600.so","libSeetaQualityAssessor300.so"};
    private static final String[] LINUX_UBUNTU_LIBS = {"libSeetaAuthorize.so","libtennis.so","libtennis_haswell.so","libtennis_pentium.so","libtennis_sandy_bridge.so","libSeetaFaceDetector600.so","libSeetaAgePredictor600.so","libSeetaEyeStateDetector200.so","libSeetaFaceAntiSpoofingX600.so","libSeetaFaceLandmarker600.so","libSeetaFaceRecognizer610.so","libSeetaGenderPredictor600.so","libSeetaMaskDetector200.so","libSeetaPoseEstimation600.so","libSeetaFaceTracking600.so","libSeetaQualityAssessor300.so"};

    private static final String TEMP_DIR = "smartjavaai-native-libs";

    public static SeetaFace6JNI seetaFace6SDK;



    public static void loadNativeLibraries(String modelPath) {
        try {
            // 创建临时目录
            tempNativeDir = Files.createTempDirectory(TEMP_DIR);
            log.info("create temp native directory: " + tempNativeDir.toAbsolutePath().toString());

            // 获取当前平台库列表
            String libDir = getLibDir();
            String[] libNames = getPlatformLibs(libDir);

            // 批量提取库文件
            for (String libName : libNames) {
                extractLibrary(libName,libDir);
            }

            String separator = System.getProperty("path.separator");
            String sysLib = System.getProperty("java.library.path");
            if (sysLib.endsWith(separator)) {
                System.setProperty("java.library.path", sysLib + tempNativeDir);
            } else {
                System.setProperty("java.library.path", sysLib + separator + tempNativeDir);
            }


            // 按顺序加载库（确保依赖关系）
            for (String libName : libNames) {
                log.info("Loading library: " + tempNativeDir + File.separator + libName);
                System.load(tempNativeDir + File.separator + libName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Native library loading failed", e);
        }
    }


    private static String[] getPlatformLibs(String libDir) {
        if (libDir.contains("windows")) return WIN_LIBS;
        if (libDir.contains("centos")) return LINUX_CENTOS_LIBS;
        if (libDir.contains("ubuntu")) return LINUX_UBUNTU_LIBS;
        throw new UnsupportedOperationException("Unsupported OS");
    }

    /**
     * 拷贝依赖库到临时目录
     * @param libName
     * @param libDir
     * @throws IOException
     */
    private static void extractLibrary(String libName,String libDir) throws IOException {
        String resourcePath = "/native" + libDir + "/" + libName;
        try (InputStream in = NativeLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException(resourcePath);

            Path targetPath = tempNativeDir.resolve(libName);
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("copy target path success : " + targetPath.toAbsolutePath().toString());

            // 设置可执行权限
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                targetPath.toFile().setExecutable(true);
            }
        }
    }

    /**
     * 获取依赖库目录
     * @return
     */
    private static String getLibDir() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "/windows";
        } /*else if (osName.contains("linux")) {
            String linuxOsName = getLinuxOsName();
            if(StringUtils.isBlank(linuxOsName)){
                throw new UnsupportedOperationException("Unsupported platform");
            };
            if(linuxOsName.contains("ubuntu")){
                return "/linux/ubuntu";
            }else if(linuxOsName.contains("centos")){
                return "/linux/centos";
            }
        }*/
        throw new UnsupportedOperationException("Unsupported platform");
    }



    /**
     * 获取linux系统名称
     * @return
     */
    private static String getLinuxOsName(){
        try (BufferedReader reader = new BufferedReader(new FileReader("/etc/os-release"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ID=")) {
                    String distro = line.substring(3).replace("\"", "").trim();
                    return distro;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read /etc/os-release: " + e.getMessage());
        }
        return null;
    }
}

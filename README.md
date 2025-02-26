# SmartJavaAI：JAVA深度学习算法工具包

**SmartJavaAI**是基于 **DJL（Deep Java Library）** 封装的轻量级深度学习算法库，依托DJL的自动模型管理和跨框架特性，**无需安装Python环境**且无需手动下载模型文件（模型由DJL内部自动从云端加载），该库致力于构建Java生态与AI模型之间的高效桥梁。针对Java开发者面临的两大痛点：

- 🐍 主流AI框架（PyTorch/TensorFlow）的Python生态与Java工程体系割裂

- ⚙️ 直接使用DJL需处理模型加载、预处理、后处理等复杂技术细节

我们实现了：
✅ **开箱即用** - 两行代码完成人脸检测/识别  
✅ **多模型支持** - 集成RetinaFace/Ultra-Light-Fast-Generic-Face-Detector双检测模型（即将支持OCR/目标检测）  
✅ **跨平台兼容** - 完美支持Windows/Linux/macOS系统（x86 & ARM架构）  

## 🌟 核心优势
| 维度        | Python生态           | 原生DJL          | 本工具包         |
|------------|---------------------|-----------------|----------------|
| 开发效率    | 需搭建Python环境     | 需实现完整AI Pipeline | 提供即用API    |
| 学习成本    | 需掌握Python/C++混合编程 | 需深入理解AI框架机制 | Java语法即可调用 |
| 部署复杂度  | 需维护多语言服务      | 需处理底层资源调度 | 单一Jar包集成   |
| 性能表现    | 原生高性能           | 依赖开发者优化经验 | 内置生产级调优  |

## 📌 支持功能

### ✅ 已实现功能

- **人脸检测**  
  支持图片/视频流中的多面孔定位与质量评估

- **人脸特征提取**  
  基于深度学习算法生成512维特征向量

- **人脸特征比对**  
  
- **人证核验**  
  人脸照片与实时人脸画面特征比对

### ⌛ 规划中功能

- **OCR文字识别**  
  即将支持身份证/银行卡/车牌等关键信息提取，适配复杂背景与模糊文本

- **目标检测**  
  计划集成YOLOv9模型，支持车辆检测/安全帽识别/工业质检等场景

- **图像分割**  
  
- **语音识别**  
  基于Transformer的语音转文本引擎，支持中文/英文多语种识别


## 人脸算法模型

- server模型-**RetinaFace 模型**[[GitHub]](https://github.com/deepinsight/insightface/tree/master/detection/retinaface)：一个高效的深度学习人脸检测模型，支持高精度的人脸检测。
- 轻量模型-**Ultra-Light-Fast-Generic-Face-Detector-1MB ** [[GitHub\]](https://github.com/Linzaer/Ultra-Light-Fast-Generic-Face-Detector-1MB)：一个轻量级的人脸检测模型，适用于需要较低延迟和较小模型尺寸的应用场景。

## 环境要求

- Java 版本：**JDK 11或更高版本**
- 操作系统：支持的操作系统（如 Windows、Linux 或 macOS）

## 使用步骤

📌 **运行提示**：首次启动时将自动完成模型下载及依赖项配置，建议保持网络畅通。初始化完成后，后续启动将恢复毫秒级响应速度。

无网络环境下可指定本地模型路径（需提前预下载模型包）

### 1. 安装人脸算法依赖

在 Maven 项目的 `pom.xml` 中添加 SmartJavaAI的人脸算法依赖：

```xml
<dependencies>
     <dependency>
        <groupId>ink.numberone</groupId>
        <artifactId>smartjavaai-face</artifactId>
        <version>1.0.2</version>
     </dependency>
</dependencies>
```

### 2. 人脸检测代码示例

```java
//创建人脸算法
FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm();
//使用图片路径检测
FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
```

### 3. 轻量人脸检测代码示例

```java
//创建人脸算法
FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createLightFaceAlgorithm();
//使用图片路径检测
FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
```

### 4. 使用图片输入流检测

```java
//支持各种输入流方式检测图片
File input = new File("src/main/resources/largest_selfie.jpg");
FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(input));
```

### 5. 人证核验

人证核验步骤：

（1）提取身份证人脸特征，

（2）提取实时人脸特征

（3）特征比对

```java
//创建脸算法
FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm();
//提取身份证人脸特征（图片仅供测试）
float[] featureIdCard = currentAlgorithm.featureExtraction("src/main/resources/kana1.jpg");
//提取身份证人脸特征（从图片流获取）
//File input = new File("src/main/resources/kana1.jpg");
//float[] featureIdCard = currentAlgorithm.featureExtraction(new FileInputStream(input));
logger.info("身份证人脸特征：{}", JSONObject.toJSONString(featureIdCard));
//提取实时人脸特征（图片仅供测试）
float[] realTimeFeature = currentAlgorithm.featureExtraction("src/main/resources/kana2.jpg");
logger.info("实时人脸特征：{}", JSONObject.toJSONString(realTimeFeature));
if(realTimeFeature != null){
    if(currentAlgorithm.calculSimilar(featureIdCard, realTimeFeature) > 0.8){
        logger.info("人脸核验通过");
    }else{
        logger.info("人脸核验不通过");
    }
}
```

### 6. 离线下载模型

​	**SmartJavaAI**如果未指定模型地址，系统将自动下载模型至本地。因此，无论模型是否通过离线方式下载，SmartJavaAI 最终都会在离线环境下运行模型。

- [离线下载模型代码示例](examples/face_offline.md)

## 完整代码

`📁 examples/src/main/java/smartai/examples/face`  
 └── 📄[FaceDemo.java](https://github.com/geekwenjie/SmartJavaAI/blob/master/examples/src/main/java/smartai/examples/face/FaceDemo.java)  <sub>*（基于JDK11构建的完整可执行示例）*</sub>

# SmartJavaAI离线下载模型案例

**SmartJavaAI**如果未指定模型地址，系统将自动下载模型至本地。因此，无论模型是否通过离线方式下载，SmartJavaAI 最终都会在离线环境下运行模型。

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

### 2. 下载模型

|         模型名称          |                           下载地址                           | 文件大小 |    适用场景    |
| :-----------------------: | :----------------------------------------------------------: | :------: | :------------: |
|        retinaface         | [下载](https://resources.djl.ai/test-models/pytorch/retinaface.zip) |  110MB   | 高精度人脸检测 |
| ultralightfastgenericface | [下载](https://resources.djl.ai/test-models/pytorch/ultranet.zip) |  1.7MB   |  高速人脸检测  |
|     featureExtraction     | [下载](https://resources.djl.ai/test-models/pytorch/face_feature.zip) |  104MB   |  人脸特征提取  |

### 3. 人脸检测代码示例（离线下载模型）

```java
// 初始化配置
ModelConfig config = new ModelConfig();
config.setAlgorithmName("retinaface");//人脸算法模型，目前支持：retinaface及ultralightfastgenericface
//config.setAlgorithmName("ultralightfastgenericface");//轻量模型
config.setConfidenceThreshold(FaceConfig.DEFAULT_CONFIDENCE_THRESHOLD);//置信度阈值
config.setMaxFaceCount(FaceConfig.MAX_FACE_LIMIT);//每张特征图保留的最大候选框数量
//nms阈值:控制重叠框的合并程度,取值越低，合并越多重叠框（减少误检但可能漏检）；取值越高，保留更多框（增加检出但可能引入冗余）
config.setNmsThresh(FaceConfig.NMS_THRESHOLD);
//模型下载地址：
//retinaface: https://resources.djl.ai/test-models/pytorch/retinaface.zip
//ultralightfastgenericface: https://resources.djl.ai/test-models/pytorch/ultranet.zip
//改为模型存放路径
config.setModelPath("/Users/xxx/Documents/develop/face_model/retinaface.pt");
//创建人脸算法
FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceAlgorithm(config);
//使用图片路径检测
FaceDetectedResult result = currentAlgorithm.detect("src/main/resources/largest_selfie.jpg");
logger.info("人脸检测结果：{}", JSONObject.toJSONString(result));
//使用图片流检测
File input = new File("src/main/resources/largest_selfie.jpg");
//FaceDetectedResult result = currentAlgorithm.detect(new FileInputStream(input));
//logger.info("人脸检测结果：{}", JSONObject.toJSONString(result));
BufferedImage image = ImageIO.read(input);
//创建保存路径
Path imagePath = Paths.get("output").resolve("retinaface_detected.jpg");
//绘制人脸框
ImageUtils.drawBoundingBoxes(image, result, imagePath.toAbsolutePath().toString());
```

### 3. 人证核验示例（离线下载模型）

人证核验步骤：

（1）提取身份证人脸特征，

（2）提取实时人脸特征

（3）特征比对

```java
// 初始化配置
ModelConfig config = new ModelConfig();
config.setAlgorithmName("featureExtraction");
//模型下载地址：https://resources.djl.ai/test-models/pytorch/face_feature.zip
//改为模型存放路径
config.setModelPath("/Users/xxx/Documents/develop/face_model/face_feature.pt");
//创建脸算法
FaceAlgorithm currentAlgorithm = FaceAlgorithmFactory.createFaceFeatureAlgorithm(config);
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

## 完整代码

`📁 examples/src/main/java/smartai/examples/face`  
 └── 📄[FaceDemo.java](https://github.com/geekwenjie/SmartJavaAI/blob/master/examples/src/main/java/smartai/examples/face/FaceDemo.java)  <sub>*（基于JDK11构建的完整可执行示例）*</sub>

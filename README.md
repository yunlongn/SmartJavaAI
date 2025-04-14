<p align="center">
	<a href="https://gitee.com/dengwenjie/SmartJavaAI"><img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/logo.png?v=2025-04-13T07:48:42.197Z" width="45%"></a>
</p>
<p align="center">
	<strong>🍬JAVA轻量级深度学习算法库，该库致力于构建Java生态与AI模型之间的高效桥梁</strong>
</p>

## 📚简介

`SmartJavaAI`是专为Java 开发者打造的一个功能丰富、开箱即用的 Java 算法工具包，致力于帮助Java开发者高效集成各类智能算法。SmartJavaAI通过对多种主流算法的统一封装，开发者无需深入了解底层实现，即可轻松在 Java 代码中调用人脸识别、目标检测、OCR 等功能。目前已支持部分人脸识别与目标检测算法，底层实现涵盖了 C++、Python 等语言的深度学习模型。后续将持续扩展更多算法，最终将构建一个面向 Java 开发者的通用智能工具库。



## 🚀  核心亮点

针对 Java 开发者在集成智能算法时常见的两大痛点：

- 🐍 主流AI深度学习框架（PyTorch/TensorFlow）的Python生态与Java工程体系割裂

- ⚙️ 现有算法方案分散杂乱，封装不统一，使用门槛高，不易直接服务于 Java 业务开发

我们实现了：

✅ **开箱即用** - 两行代码即可调用算法  

✅ **支持多种深度学习引擎** - Pytorch、Tensorflow、MXNet、ONNX Runtime

✅ **功能丰富** - 当前支持人脸识别与目标检测，未来将陆续支持 OCR、图像分类、NLP 等多个 AI 领域任务，构建全面的智能算法体系。

✅ **跨平台兼容** - 支持Windows/Linux/macOS系统（x86 & ARM架构）  



## 🌟 AI集成方式对比

| 方案                | 技术特点                                                                 | 优点                                                                 | 缺点                                                                 |
|---------------------|--------------------------------------------------------------------------|----------------------------------------------------------------------|----------------------------------------------------------------------|
| **OpenCV**         | 传统图像处理方案                                                        | ✅ 提供java接口<br>✅ 轻量级部署<br>✅ 社区资源丰富                   | ❌ 基于传统算法精度低(60%-75%)<br>❌ 需本地安装环境 |
| &zwnj;**虹软SDK**&zwnj;         | 商业级闭源解决方案                                                      | ✅ 开箱即用<br>✅ 提供完整文档和SDK<br>✅ 支持离线活体检测      | ❌ 免费版需年度授权更新<br>❌ 商业授权费用高<br>❌ 代码不可控 |
| &zwnj;**云API(阿里云)**&zwnj;   | SaaS化云端服务                                                          | ✅ 零部署成本<br>✅ 支持高并发<br>✅ 自带模型迭代        | ❌ 网络延迟风险(200-800ms)<br>❌ 按调用量计费<br>❌ 有数据安全风险 |
| &zwnj;**Python混合调用**&zwnj;  | 跨语言调用方案                                                          | ✅ 可集成PyTorch/TF等框架<br>✅ 支持自定义算法<br>✅ 识别精度高  | ❌ 需维护双语言环境<br>❌ 进程通信性能损耗(30%+)<br>❌ 异常处理复杂度翻倍 |
| &zwnj;**DJL框架**&zwnj;         | 深度学习框架                                                            | ✅ 纯Java实现<br>✅ 支持主流深度学习框架<br>✅ 可加载预训练模型(99%+)    | ❌ 需掌握DL知识<br>❌ 需处理模型加载、预处理、后处理等复杂技术细节       |
| &zwnj;**SmartJavaAI**&zwnj;         | java深度学习工具包                                                            | ✅ 支持主流深度学习框架<br>✅ 提供丰富、开箱即用API<br>  ✅ 上手简单，单一Jar包集成 |   ❌要求JDK版本11及以上  |



## 📌 支持功能

### ✅ 已实现功能

- **人脸检测**  
  人脸检测、人脸识别、人脸比对1:1、人脸比对1:N、人脸库注册、人脸库、人脸库删除
- **目标检测**
    支持通用目标检测，能够识别图像中的多种物体类别，返回物体位置与类别信息

### ⌛ 规划中功能

- **OCR文字识别**  
  即将支持身份证/银行卡/车牌等关键信息提取
- **图像分割**  
- **语音识别**  
  基于Transformer的语音转文本引擎，支持中文/英文多语种识别



## 🛠️包含组件

| 模块                          |     介绍                                                                          |
|-----------------------------|---------------------------------------------------------------------------------- |
| smartjavaai-common          |     基础通用模块，封装了公共功能，供各算法模块共享使用           |
| smartjavaai-face            |     人脸功能模块                                        |
| smartjavaai-objectdetection |     目标检测模块                                                               |
| smartjavaai-seetaface6-lib  |     seetaface6人脸算法JNI接口封装              |

可以根据需求对每个模块单独引入，也可以通过引入`smartjavaai-all`方式引入所有模块。

-------------------------------------------------------------------------------



## 📦 安装

### 1、环境要求

- Java 版本：**JDK 11或更高版本**
- 操作系统：不同模型支持的系统不一样，具体请查看文档

### 2、Maven
在项目的pom.xml的dependencies中加入以下内容（全部功能），也可以根据需求对每个模块单独引入:

```xml
<dependency>
    <groupId>ink.numberone</groupId>
    <artifactId>smartjavaai-all</artifactId>
    <version>1.0.8</version>
</dependency>
```
### 3、人脸检测运行流程
<p align="center">
	<a href="https://gitee.com/dengwenjie/SmartJavaAI"><img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/face.jpg" width="60%"></a>
</p>
（1）人脸模型下载（使用人脸相关功能）

如果在有网环境下使用，不需要下载模型(SeetaFace6模型除外)

|         模型名称          |                           下载地址                           | 文件大小 |                  适用场景                  | 兼容系统            |
| :-----------------------: | :----------------------------------------------------------: | :------: | :----------------------------------------: | ------------------- |
|        retinaface         | [下载](https://resources.djl.ai/test-models/pytorch/retinaface.zip) |  110MB   |               高精度人脸检测               | Windows/Linux/MacOS |
| ultralightfastgenericface | [下载](https://resources.djl.ai/test-models/pytorch/ultranet.zip) |  1.7MB   |                高速人脸检测                | Windows/Linux/MacOS |
|        seetaface6         | [下载](https://pan.baidu.com/s/1hfNacA8ISV2qHrycjOkgqA?pwd=1234) |  288MB   | 人脸检测、人脸比对、人脸库注册、人脸库查询 | Windows             |
|          facenet          | [下载](https://resources.djl.ai/test-models/pytorch/face_feature.zip) |  104MB   |           人脸特征提取、人脸比对           | Windows/Linux/MacOS |

（2）人脸库下载（使用人脸库相关功能：人脸注册、人脸查询）

目前仅SeetaFace6人脸算法支持人脸库注册，查询等功能，所以只有使用SeetaFace6模型时才需要下载`face.db`，`face.db` 是 一个SQLite 数据库，程序启动并使用相关功能时会自动操作该数据库，用于存储人脸特征数据及其对应的唯一标识 Key，支持后续的人脸注册、查询和比对等操作。

下载链接: https://pan.baidu.com/s/1DzE1rDkFnjEXQbIasIdFrA?pwd=1234 提取码: 1234

（3）下载示例代码

https://gitee.com/dengwenjie/SmartJavaAI-Demo

📁 src/main/java/smartai/examples/face

└── 📄[RetinaFaceDemo.java](https://gitee.com/dengwenjie/SmartJavaAI-Demo/blob/master/src/main/java/smartai/examples/face/RetinaFaceDemo.java)  <sub>*（人脸模型：RetinaFace示例代码）*</sub>

└── 📄[LightFaceDemo](https://gitee.com/dengwenjie/SmartJavaAI-Demo/blob/master/src/main/java/smartai/examples/face/LightFaceDemo.java)  <sub>*（人脸模型：UltraLightFastGenericFaceModel示例代码）*</sub>

└── 📄[SeetaFace6Demo.java](https://gitee.com/dengwenjie/SmartJavaAI-Demo/blob/master/src/main/java/smartai/examples/face/SeetaFace6Demo.java)  <sub>*（人脸模型：SeetaFace6示例代码）*</sub>

└── 📄[FaceNetDemo.java](https://gitee.com/dengwenjie/SmartJavaAI-Demo/blob/master/src/main/java/smartai/examples/face/FaceNetDemo.java)  <sub>*（人脸模型：FaceNet示例代码）*</sub>

（4）离线使用方法

程序首次运行时，会自动下载所需的底层依赖库到默认的缓存路径。不同操作系统的默认缓存路径如下：

\{user}需要替换成您当前登录的用户名

|         | 依赖库及缓存目录         |
| ------- | ------------------------ |
| windows | C:/Users/\{user}/.djl.ai |
| linux   | /home/{user}/.djl.ai     |
| macos   | /Users/{user}/.djl.ai    |

对于需要在离线环境中使用的情况，可以在联网环境中运行程序一次，确保所需的依赖库已下载。然后，将上述缓存目录复制到离线环境中相同的路径下，即可实现离线使用。

请注意，SeetaFace6 默认支持离线使用，无需上述操作即可在离线环境中运行。

### 4、目标检测运行流程

<p align="center">
	<a href="https://gitee.com/dengwenjie/SmartJavaAI"><img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/object_detection_detected.png" width="60%"></a>
</p>

（1）下载示例代码

https://gitee.com/dengwenjie/SmartJavaAI-Demo

📁 src/main/java/smartai/examples/objectdetection

└── 📄[ObjectDetection.java](https://gitee.com/dengwenjie/SmartJavaAI-Demo/blob/master/src/main/java/smartai/examples/objectdetection/ObjectDetection.java)  <sub>*（目标检测示例代码）*</sub>




## 🙏 鸣谢

本项目在开发过程中借鉴或使用了以下优秀开源项目，特此致谢：

- **[Seetaface6JNI](https://gitee.com/cnsugar/seetaface6JNI)**
- **[Deep Java Library](https://docs.djl.ai)**



## 联系方式

如您在使用过程中有任何问题或建议，欢迎添加微信，与我们交流并加入用户交流群

- **微信**: deng775747758 （请备注：SmartJavaAI）
- **Email**: 775747758@qq.com

🚀 **如果这个项目对你有帮助，别忘了点个 Star ⭐！你的支持是我持续优化升级的动力！** ❤️




## 更新日志


## [v1.0.8] - 2025-04-13
-  新增目标检测功能
-  模型调用接口统一封装
-  修复若干已知问题
-  支持自定义选择使用 GPU 或 CPU 运算
-  人脸识别模块新增多种接口，功能更加完善
## [v1.0.6] - 2025-04-01
-  修复人脸识别算法facenet-pytorch实现方式
-  优化Seetaface6算法，兼容jdk高版本




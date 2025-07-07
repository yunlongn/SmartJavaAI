<p align="center">
	<a href="https://gitee.com/dengwenjie/SmartJavaAI"><img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/logo.png?v=2025-04-13T07:48:42.197Z" width="45%"></a>
</p>
<p align="center">
	<strong>🍬Java轻量级、免费、离线AI工具箱，致力于帮助Java开发者零门槛使用AI算法模型</strong><br>
	<em>像Hutool一样简单易用的Java AI工具箱</em>
</p>
<p align="center">
	👉 <a href="http://smartjavaai.cn/">http://smartjavaai.cn/</a> 👈
</p>
<p align="center">
	<a target="_blank" href="https://central.sonatype.com/artifact/ink.numberone/smartjavaai-all">
		<img src="https://img.shields.io/maven-central/v/ink.numberone/smartjavaai-all.svg?label=Maven%20Central" />
	</a>
	<a target="_blank" href="https://license.coscl.org.cn/MulanPSL2">
		<img src="https://img.shields.io/:license-MulanPSL2-blue.svg" />
	</a>
	<a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk11-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8+-green.svg" />
	</a>
	<a target="_blank" href='https://gitee.com/dengwenjie/SmartJavaAI/stargazers'>
		<img src='https://gitee.com/dengwenjie/SmartJavaAI/badge/star.svg?theme=gvp' alt='star'/>
	</a>
    <a target="_blank" href='https://github.com/geekwenjie/SmartJavaAI'>
		<img src="https://img.shields.io/github/stars/geekwenjie/SmartJavaAI.svg?style=social" alt="github star"/>
	</a>
    <a target="_blank" href='https://gitcode.com/geekwenjie/SmartJavaAI'>
		<img src="https://gitcode.com/geekwenjie/SmartJavaAI/star/badge.svg" alt="gitcode star"/>
	</a>
</p>

-------------------------------------------------------------------------------

[**开发文档**](http://doc.smartjavaai.cn)

-------------------------------------------------------------------------------

## 📚简介

SmartJavaAI是专为JAVA 开发者打造的一个功能丰富、开箱即用的 JAVA  AI算法工具包，致力于帮助JAVA开发者零门槛使用各种AI算法模型，开发者无需深入了解底层实现，即可轻松在 Java 代码中调用人脸识别、目标检测、OCR 等功能。底层实现涵盖了 C++、Python 等语言的深度学习模型。后续将持续扩展更多算法，目标是构建一个“像 Hutool 一样简单易用”的 JAVA AI 通用工具箱

## 🚀  能力展示

<div align="center">
  <table>      
    <tr>
      <td>
        <div align="left">
          <p>人脸检测</p>   
         - 5点人脸关键点定位 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/face5point.png" width = "500px"/>
        </div>
      </td>
    </tr>          
    <tr>
      <td>
        <div align="left">
          <p>人脸比对1：1</p>
          - 人脸对齐 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/face1-1.jpg" width = "500px"/>
        </div>
      </td>
    </tr>  
    <tr>
      <td>
        <div align="left">
          <p>人证核验</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/idcard.png" width = "500px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>人脸比对1：N</p>  
          - 人脸对齐 <br>
          - 人脸注册 <br>
          - 人脸库查询<br>
          - 人脸库删除<br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/face1-n.png" width = "500px"/>
        </div>
      </td>
    </tr>  
    <tr>
      <td>
        <div align="left">
          <p>人脸属性检测</p>  
          - 性别检测 <br>
          - 年龄检测 <br>
          - 口罩检测<br>
          - 眼睛状态检测<br>
          - 脸部姿态检测<br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/face_attribute.jpg" width = "500px"/>
        </div>
      </td>
    </tr> 
    <tr>
      <td>
        <div align="left">
          <p>活体检测</p>  
          - 图片和视频活体检测 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/liveness2.jpg" width = "500px"/>
        </div>
      </td>
    </tr> 
<tr>
      <td>
        <div align="left">
          <p>人脸表情识别</p>  
          - 7种表情检测 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/emotion.jpg" width = "500px"/>
        </div>
      </td>
    </tr> 
   <tr>
      <td>
        <div align="left">
          <p>目标检测</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/object_detection_detected.png" width = "500px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>OCR文字识别</p>
          - 支持任意角度文字识别 <br>
          - 支持印刷体识别 <br>
          - 支持手写字识别<br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/ocr/general_ocr_002_recognized.png" width = "500px"/>
        </div>
      </td>
    </tr> 
    <tr>
      <td>
        <div align="left">
          <p>机器翻译</p>
          - 200多种语言互相翻译
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/translate/translate.png" width = "500px"/>
        </div>
      </td>
    </tr>
  </table>
</div>



## 🚀  核心亮点

针对 Java 开发者在集成智能算法时常见的两大痛点：

- 🐍 主流AI深度学习框架（PyTorch/TensorFlow）的Python生态与Java工程体系割裂

- ⚙️ 现有算法方案分散杂乱，封装不统一，使用门槛高，不易直接服务于 Java 业务开发

我们实现了：

✅ **开箱即用** - 两行代码即可调用算法  

✅ **支持多种深度学习引擎** - Pytorch、Tensorflow、MXNet、ONNX Runtime

✅ **功能丰富** - 当前支持人脸识别与目标检测，未来将陆续支持 OCR、图像分类、NLP 等多个 AI 领域任务，构建全面的智能算法体系。

✅ **跨平台兼容** - 支持Windows/Linux/macOS系统（x86 & ARM架构）  



## 📌 支持功能


### ✅ 已实现功能

- **人脸识别**  
  - 支持模型：
  - 人脸检测、人脸识别、人脸比对1:1、人脸比对1:N(支持向量数据库milvus/sqlite)、人脸库注册、人脸库删除
  - 5点人脸关键点定位
  - 人脸属性检测（性别、年龄、口罩、眼睛状态、脸部姿态）
  - 人脸活体检测：图片、视频活体检测
- **目标检测**
  - 支持多种主流模型：兼容 YOLOv3、YOLOv5、YOLOv8、YOLOv11、YOLOv12、SSD 等目标检测算法
  - 支持自定义模型加载：可无缝加载并部署用户自行训练的目标检测模型
- **OCR文字识别**
  - 支持PaddleOCR 3.0模型：集成最新PP-OCRv5模型
  - 支持任意角度识别，方向校准
  - 支持通用文字识别，通用手写字识别
- **机器翻译**
  - 集成NLLB-200模型：支持200+语言互相翻译

### ⌛ 规划中功能

- 图像分类（Image classification）

- 万物分割 （Segment Anything）

- 实例分割（Instance Segmentation）

- 语义分割（Semantic Segmentation）

- 姿态识别（Pose Estimation）

- 动作识别（Action Recognition）

- 使用 BigGAN 的图像生成

- 图像增强

- 基于 BERT 的问答

- 语音识别（Speech Recognition）


## 🌟 AI集成方式对比

| 方案                | 技术特点                                                                 | 优点                                                                 | 缺点                                                                 |
|---------------------|--------------------------------------------------------------------------|----------------------------------------------------------------------|----------------------------------------------------------------------|
| **OpenCV**         | 传统图像处理方案                                                        | ✅ 提供java接口<br>✅ 轻量级部署<br>✅ 社区资源丰富                   | ❌ 基于传统算法精度低(60%-75%)<br>❌ 需本地安装环境 |
| &zwnj;**虹软SDK**&zwnj;         | 商业级闭源解决方案                                                      | ✅ 开箱即用<br>✅ 提供完整文档和SDK<br>✅ 支持离线活体检测      | ❌ 免费版需年度授权更新<br>❌ 商业授权费用高<br>❌ 代码不可控 |
| &zwnj;**云API(阿里云)**&zwnj;   | SaaS化云端服务                                                          | ✅ 零部署成本<br>✅ 支持高并发<br>✅ 自带模型迭代        | ❌ 网络延迟风险(200-800ms)<br>❌ 按调用量计费<br>❌ 有数据安全风险 |
| &zwnj;**Python混合调用**&zwnj;  | 跨语言调用方案                                                          | ✅ 可集成PyTorch/TF等框架<br>✅ 支持自定义算法<br>✅ 识别精度高  | ❌ 需维护双语言环境<br>❌ 进程通信性能损耗(30%+)<br>❌ 异常处理复杂度翻倍 |
| &zwnj;**DJL框架**&zwnj;         | 深度学习框架                                                            | ✅ 纯Java实现<br>✅ 支持主流深度学习框架<br>✅ 可加载预训练模型(99%+)    | ❌ 需掌握DL知识<br>❌ 需处理模型加载、预处理、后处理等复杂技术细节       |
| &zwnj;**SmartJavaAI**&zwnj;         | java深度学习工具包                                                            | ✅ 支持主流深度学习框架<br>✅ 提供丰富、开箱即用API<br>  ✅ 上手简单，单一Jar包集成 |   ❌要求JDK版本11及以上  |



## 🛠️包含组件

| 模块                          | 介绍                        |
|-----------------------------|---------------------------|
| smartjavaai-common          | 基础通用模块，封装了公共功能，供各算法模块共享使用 |
| smartjavaai-face            | 人脸功能模块                    |
| smartjavaai-objectdetection | 目标检测模块                    |
| smartjavaai-ocr             | OCR文字识别模块                 |
| smartjavaai-translate       | 机器翻译模块                    |

可以根据需求对每个模块单独引入，也可以通过引入`smartjavaai-all`方式引入所有模块。

-------------------------------------------------------------------------------



## 📦 安装


### 1、环境要求

- Java 版本：**JDK 8或更高版本**
- 操作系统：不同模型支持的系统不一样，具体请查看文档

### 2、Maven
在项目的pom.xml的dependencies中加入以下内容（全部功能），也可以根据需求对每个模块单独引入:

```xml
<dependency>
    <groupId>cn.smartjavaai</groupId>
    <artifactId>smartjavaai-all</artifactId>
    <version>1.0.19</version>
</dependency>
```
### 3、完整示例代码

[示例代码](https://gitee.com/dengwenjie/SmartJavaAI/tree/master/examples)

### 4、文档地址

[开发文档](http://doc.smartjavaai.cn)

### 5、模型简介及下载

[模型下载](https://pan.baidu.com/s/1dlZxWEMULnaietMDUJh38g?pwd=1234)

## 人脸模块

#### 人脸检测模型(FaceDetection、FaceLandmarkExtraction)

支持功能：
- 人脸检测
- 5点人脸关键点定位

| 模型名称        | 模型简介                   | 模型开源网站                                                                                 |
| ----------- |------------------------|----------------------------------------------------------------------------------------|
| RetinaFace | 高精度人脸检测模型              | [Github](https://github.com/biubug6/Pytorch_Retinaface)                                   |
| UltraLightFastGenericFace | 针对边缘计算设备设计的轻量人脸检测模型 | [Github](https://github.com/Linzaer/Ultra-Light-Fast-Generic-Face-Detector-1MB) |
| SeetaFace6  | 中科视拓最新开放的开源免费的全栈人脸识别工具包       | [Github](https://github.com/seetafaceengine/SeetaFace6)     |

---

#### 人脸识别模型(FaceRecognition)

支持功能：
- 人脸512维特征提取
- 人脸对齐(人脸矫正)
- 人脸特征比对（内积[IP]、欧氏距离[L2]、余弦相似度[COSINE]）

| 模型名称                | 模型简介                                                    | 模型开源网站                                                                                 |
|---------------------|---------------------------------------------------------|----------------------------------------------------------------------------------------|
| InsightFace_IR-SE50 | （高精度）这是对 ArcFace（论文）和 InsightFace（GitHub）的 PyTorch 重新实现 | [Github](https://github.com/TreB1eN/InsightFace_Pytorch)  |
| InsightFace_Mobilefacenet  | （轻量级）这是对 ArcFace（论文）和 InsightFace（GitHub）的 PyTorch 重新实现 | [Github](https://github.com/TreB1eN/InsightFace_Pytorch)  |
| FaceNet             | 基于 PyTorch 的 Inception ResNet（V1）模型仓库                   | [Github](https://github.com/timesler/facenet-pytorch)  |
| ElasticFace         | 基于 CVPRW2022 论文《ElasticFace: Elastic Margin Loss for Deep Face Recognition》实现的人脸识别模型| [Github](https://github.com/fdbtrs/ElasticFace) |
| SeetaFace6          | 中科视拓最新开放的开源免费的全栈人脸识别工具包  | [Github](https://github.com/seetafaceengine/SeetaFace6)     |


#### 静态活体检测(RGB)模型（Silent face-anti-spoofing、FaceLivenessDetection）

支持功能：
- 检测图片中的人脸是否为来自认证设备端的近距离裸拍活体人脸对象(裸拍活体正面人脸是指真人未经重度PS、风格化、人工合成等后处理的含正面人脸)

| 模型名称       | 模型简介                    | 模型开源网站                                                               |
|------------|-------------------------|----------------------------------------------------------------------|
| MiniVision | 小视科技的静默活体检测             | [Github](https://github.com/minivision-ai/Silent-Face-Anti-Spoofing) |
| IIC_FL(cv_manual_face-liveness_flrgb)   | 阿里通义工作室人脸活体检测模型-RGB   | [魔塔](https://www.modelscope.cn/models/iic/cv_manual_face-liveness_flrgb/feedback)                 |
| SeetaFace6 | 中科视拓最新开放的开源免费的全栈人脸识别工具包 | [Github](https://github.com/seetafaceengine/SeetaFace6)              |


#### 人脸表情识别模型(FacialExpressionRecognition、fer)

支持功能：
- 支持识别7种表情：neutral（中性）、happy（高兴）、sad（悲伤）、surprise（惊讶）、fear（恐惧）、disgust（厌恶）、anger（愤怒）

| 模型名称       | 模型简介                     | 模型开源网站                                                               |
|------------|--------------------------|----------------------------------------------------------------------|
| DensNet121 | FaceLib的densnet121表情识别模型 | [Github](https://github.com/sajjjadayobi/FaceLib/) |
| FrEmotion   | FaceRecognition-LivenessDetection-Javascript      | [Github](https://github.com/Faceplugin-ltd/FaceRecognition-LivenessDetection-Javascript)                 |


#### 人脸属性识别模型(GenderDetection、AgeDetection、EyeClosenessDetection、FacePoseEstimation)

支持功能：
- 性别检测
- 年龄检测
- 闭眼检测
- 人脸姿态检测
- 戴口罩检测

| 模型名称       | 模型简介                     | 模型开源网站                                                               |
|------------|--------------------------|----------------------------------------------------------------------|
| SeetaFace6 | 中科视拓最新开放的开源免费的全栈人脸识别工具包 | [Github](https://github.com/seetafaceengine/SeetaFace6)              |


#### 人脸质量评估模型(FaceQualityAssessment)

支持功能：
- 亮度评估
- 清晰度评估
- 完整度评估
- 姿态评估
- 遮挡评估

| 模型名称       | 模型简介                     | 模型开源网站                                                               |
|------------|--------------------------|----------------------------------------------------------------------|
| SeetaFace6 | 中科视拓最新开放的开源免费的全栈人脸识别工具包 | [Github](https://github.com/seetafaceengine/SeetaFace6)              |


---

## 目标检测模型

支持功能：
- 自训练模型推理
- yolov3~yolov12 系列

#### SSD 系列

| 模型名称 | 骨干网络 | 输入尺寸 | <div style="width: 60pt">训练数据集</div> | 精度（mAP） | <div style="width: 50pt">推理速度</div> | <div style="width: 150pt">适用场景</div>|
| :------------------ | ------------- | ----------- |--------------------------------------| -------------|-------------------------------------| -------------|
|SSD_300_RESNET50 | ResNet‑50 | 300×300 | COCO                                 | 中等 | 快                                   | 精度需求一般|
|SSD_512_RESNET50_V1_VOC | ResNet‑50 | 512×512 | Pascal VOC                           | 稍高 | 中等                                  | 精度优先、可接受略低速度的场景|
|SSD_512_VGG16_ATROUS_COCO | VGG‑16 | 512×512 | COCO                                 | 较高 | 中等                                  | 通用场景；对小目标有一定提升|
|SSD_300_VGG16_ATROUS_VOC | VGG‑16 | 300×300 | Pascal VOC                           | 中等偏上 | 快                                   | VOC 数据集同类任务；资源受限时使用|
|SSD_512_MOBILENET1_VOC | MobileNet‑1.0 | 512×512 | Pascal VOC                           | 中等 | 快                                   | 嵌入式/移动端设备；算力和内存都很有限|

#### YOLO 系列

|模型名称 | 版本 | 大小（Backbone） | <div style="width: 60pt">数据集</div> | <div style="width: 50pt">精度</div> | <div style="width: 50pt">速度</div> | <div style="width: 150pt">适用场景</div> |
| :--------- | -------| ----------- |----------------------------------------|-----------------------------------|-------|--------------------------------------|
| YOLO12N | v12 | 极轻量 | COCO                                   | 高                                | 极快 | YOLO 系列最新版本，精度与速度进一步优化，适合高实时性要求场景 |
|YOLO11N | v11  | 极轻量 | COCO                                   | 中等偏上                              | 极快 | 与 v8n 类似，版本更新点在兼容性与 API              |
|YOLOV8N | v8  | 极轻量 | COCO                                   | 中等偏上                              | 极快 | 对实时性要求极高的应用                          |
|YOLOV5S | v5  | 小型 | COCO                                   | 较高                                | 非常快 | 常见通用场景，算力资源有限时优选                     |
|YOLOV5S_ONNXRUNTIME | v5  | 小型 | COCO                                   | 较高                                | 加速（需 ONNX 支持） | Windows/Linux 通用加速部署                 |
|YOLO (MXNet / 通用模型) | v3  | DarkNet‑53 | COCO                                   | 较高                                | 快 | 需要 MXNet 生态或复现老项目时使用                 |

#### YOLOv3 变体系列

|模型名称 | 骨干网络 |  <div style="width: 60pt">数据集</div> | 输入尺寸 | <div style="width: 50pt">精度</div> | <div style="width: 50pt">速度</div> | <div style="width: 200pt">适用场景</div> |
| :--------- | -------| ----------- |------------| ---------|-------|--------------------------------------|
|YOLO3_DARKNET_VOC_416 | DarkNet‑53 | VOC | 416×416 | 高 | 中等 | VOC 任务复现；精度优先                        |
|YOLO3_DARKNET_COCO_320 | DarkNet‑53 | COCO | 320×320 | 中等 | 快 | COCO 小模型测试；资源受限                      |
|YOLO3_DARKNET_COCO_416 | DarkNet‑53 | COCO | 416×416 | 高 | 中等 | 通用 COCO 部署；精度优先                      |
|YOLO3_DARKNET_COCO_608 | DarkNet‑53 | COCO | 608×608 | 很高 | 慢| 批量离线推理；精度要求极高                        |
|YOLO3_MOBILENET_VOC_320 | MobileNet‑V1 | VOC | 320×320 | 中等 | 非常快| 嵌入式设备；VOC 小目标任务                      |
|YOLO3_MOBILENET_VOC_416 | MobileNet‑V1 | VOC | 416×416 | 高 | 快| 移动端 VOC 部署                           |
|YOLO3_MOBILENET_COCO_320 | MobileNet‑V1 | COCO | 320×320 | 中等 | 非常快 | 嵌入式设备；COCO 小目标任务                     |
|YOLO3_MOBILENET_COCO_416 | MobileNet‑V1 | COCO | 416×416 | 高 | 快 | 移动端 COCO 部署                          |
|YOLO3_MOBILENET_COCO_608 | MobileNet‑V1 | COCO | 608×608 | 很高 | 中等 | 对精度要求较高的移动端任务                        |

---

## OCR 模型

支持功能：
- 支持简体中文、繁体中文、英文、日文四种主要语言
- 手写、竖版、拼音、生僻字
- 方向矫正

#### 文本检测模型

| 模型名称                | 模型简介 | 模型开源网站                                                                                                       |
| ------------| ------------------- |--------------------------------------------------------------------------------------------------------------|
| PP-OCRv5_server_det | 飞桨PaddleOCR 3.0         | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |

#### 文本识别模型
| 模型名称                | 模型简介 | 模型开源网站                                                                                                       |
| ------------| ------------------- |--------------------------------------------------------------------------------------------------------------|
| PP-OCRv5_server_rec | 飞桨PaddleOCR 3.0           | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |

#### 文本方向分类模型(cls)

| 模型名称                | 模型简介 | 模型开源网站     |
| ------------| ------------------- |------------|
| ch_ppocr_mobile_v2.0_cls   | 原始分类器模型，对检测到的文本行文字角度分类      | [Github](https://paddlepaddle.github.io/PaddleOCR/latest/en/version2.x/ppocr/model_list.html?h=models+list#13-multilingual-detection-model) |

---

## 🌍 机器翻译模型

支持功能
- 200多语言互相翻译

| 模型名称                           | 模型简介                     | 模型官网                                          |
| ------------------------------ | ------------------------ |-----------------------------------------------|
| NLLB-200                       | Meta AI 开发的一个先进的单一多语言机器翻译模型 | [Github](https://github.com/facebookresearch/fairseq/tree/nllb) |

---


## 🙏 鸣谢

本项目在开发过程中借鉴或使用了以下优秀开源项目，特此致谢：

- **[Deep Java Library](https://docs.djl.ai)**
- **[AIAS](https://gitee.com/mymagicpower/AIAS)**

## 联系方式

如您在使用过程中有任何问题或建议，欢迎添加微信，与我们交流并加入用户交流群

- **微信**: deng775747758 （请备注：SmartJavaAI）
- **Email**: 775747758@qq.com


🚀 **如果这个项目对你有帮助，别忘了点个 Star ⭐！你的支持是我持续优化升级的动力！** ❤️


## 近期更新日志

## [v1.0.19] - 2025-07-06
- 人脸模块：新增小视科技（MiniVision）活体检测模型
- 人脸模块：新增阿里通义工作室活体检测模型
- 人脸模块：新增 2 个表情识别模型
- 人脸模块：新增 InsightFace 和 ElasticFace 人脸识别模型
- 人脸模块：新增 Seetaface6 质量评估模型
- 目标检测模块：支持更多自定义模型参数配置
- 人脸模块：支持 Base64 编码图片输入
- 通用功能：实现 AutoCloseable 接口，支持资源自动释放
- OCR 模块：修复加方向矫正后无法连续识别的问题
- 人脸模块：修复人脸更新后的缓存异常问题
- 其他：优化部分功能与细节体验

## [v1.0.17] - 2025-06-18
- 新增机器翻译模块：支持 200+ 种语言之间的相互翻译
- 人脸识别模块：修复批量删除人脸数据时的异常问题
- 人脸识别模块：修复人脸检索 Top大于 1 时报异常问题

## [v1.0.16] - 2025-06-09
- 人脸模块：人脸查询支持 Milvus 和 SQLite
- 人脸模块：FaceNet人脸模型也支持人脸注册，查询等功能
- 人脸模块：Seetaface6 自动下载人脸库
- 人脸模块：Seetaface6解决依赖库重复下载问题
- 人脸模块：支持手动加载人脸库
- 人脸模块：人脸识别相关功能支持更多参数


## [v1.0.15] - 2025-05-17
- 新增OCR文字识别模块：支持最新 PP-OCRv5
- OCR文本识别：支持文字方向检测与自动校正


<p align="center">
	<a href="https://gitee.com/dengwenjie/SmartJavaAI"><img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/logo.png?v=2025-04-13T07:48:42.197Z" width="45%"></a>
</p>
<p align="center">
	<strong>🍬Java轻量级、免费、离线AI工具箱，致力于帮助Java开发者零门槛使用AI算法模型</strong><br>
	<em>像Hutool一样简单易用的Java AI工具箱</em>
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
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/liveness.jpg" width = "500px"/>
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
  - 人脸检测、人脸识别、人脸比对1:1、人脸比对1:N、人脸库注册、人脸库、人脸库删除
  - 5点人脸关键点定位
  - 人脸属性检测（性别、年龄、口罩、眼睛状态、脸部姿态）
  - 人脸活体检测：图片、视频活体检测
- **目标检测**
  - 支持多种主流模型：兼容 YOLOv3、YOLOv5、YOLOv8、YOLOv11、YOLOv12、SSD 等目标检测算法
  - 支持自定义模型加载：可无缝加载并部署用户自行训练的目标检测模型

### ⌛ 规划中功能

- 文字识别（OCR）

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

| 模块                          |     介绍                                                                          |
|-----------------------------|---------------------------------------------------------------------------------- |
| smartjavaai-common          |     基础通用模块，封装了公共功能，供各算法模块共享使用           |
| smartjavaai-face            |     人脸功能模块                                        |
| smartjavaai-objectdetection |     目标检测模块                                                               |

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
    <version>1.0.13</version>
</dependency>
```
### 3、完整示例代码

[示例代码](https://gitee.com/dengwenjie/SmartJavaAI/tree/master/examples)

### 4、文档地址

[开发文档](http://doc.smartjavaai.cn)


## 🙏 鸣谢

本项目在开发过程中借鉴或使用了以下优秀开源项目，特此致谢：

- **[Seetaface6JNI](https://gitee.com/cnsugar/seetaface6JNI)**
- **[Deep Java Library](https://docs.djl.ai)**
- **[AIAS](https://gitee.com/mymagicpower/AIAS)**



## 联系方式

如您在使用过程中有任何问题或建议，欢迎添加微信，与我们交流并加入用户交流群

- **微信**: deng775747758 （请备注：SmartJavaAI）
- **Email**: 775747758@qq.com


🚀 **如果这个项目对你有帮助，别忘了点个 Star ⭐！你的支持是我持续优化升级的动力！** ❤️


## 近期更新日志

## [v1.0.13] - 2025-05-17
- 支持 JDK8 环境运行
- 引入离线依赖，支持完全离线使用
- 优化 FaceNet 人脸比对性能，提升比对速度
- 支持带 Alpha 通道的 4 通道图片检测
- 目标检测：新增 YOLOv12 官方模型支持
- 目标检测：支持加载自训练模型进行推理

## [v1.0.12] - 2025-05-09
-  新增图片与视频活体检测
-  新增人脸属性识别（性别、年龄、口罩、姿态、眼睛状态）
-  优化检测返回与包结构
-  新增 dependencyManagement 统一依赖版本管理

## [v1.0.11] - 2025-04-28
-  FaceNet 特征提取新增人脸对齐
-  人脸检测新5点人脸关键点定位
-  特征提取接口支持多人脸和最佳人脸提取
-  修复人脸框边界精度问题
-  更新 Maven 发布的 groupId
## [v1.0.10] - 2025-04-19
-  兼容 SeetaFace6 在 Linux 系统下的运行
-  新增全局缓存路径设置功能
-  优化若干功能细节，提升稳定性与性能
## [v1.0.8] - 2025-04-13
-  新增目标检测功能
-  模型调用接口统一封装
-  修复若干已知问题
-  支持自定义选择使用 GPU 或 CPU 运算
-  人脸识别模块新增多种接口，功能更加完善




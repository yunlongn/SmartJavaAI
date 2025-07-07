# 人脸识别示例

本项目提供了一系列关于人脸识别相关功能的 Java 示例代码，适用于图像处理、人脸检测、活体检测等场景。所有示例基于 SmartJavaAI 的 SDK 实现。

## 📁 项目结构

```
src/main/java/smartai/examples/face/
├── attribute/          # 人脸属性检测模块
│   └── FaceAttributeDetDemo.java     # 检测性别、年龄等人脸属性
├── expression/         # 表情识别模块
│   └── ExpressionRecDemo.java        # 识别中性、高兴、悲伤等7种表情
├── facedet/            # 人脸检测模块
│   └── FaceDetDemo.java              # 检测图片或视频中的人脸并绘制人脸框
├── facerec/            # 人脸识别模块（1:1, 1:N）
│   └── FaceRecDemo.java              # 提取人脸特征、比对、注册与搜索人脸库
├── liveness/           # 活体检测模块
│   └── LivenessDetDemo.java          # 判断是否为真人（静态图或摄像头视频流）
├── quality/            # 人脸质量评估模块
│   └── FaceQualityDetDemo.java       # 评估亮度、清晰度、完整性、姿态、分辨率
└── ViewerFrame.java    # 图像显示窗口工具类（用于在 GUI 中展示图像）
```


---

## 🧩 功能模块说明

### 1. 人脸属性检测 ([FaceAttributeDetDemo.java](file:///Users/xxx/Documents/idea_workplace/SmartJavaAI/examples/face-example/src/main/java/smartai/examples/face/attribute/FaceAttributeDetDemo.java))
- **功能**：识别性别、年龄、眼镜佩戴状态、种族等属性。
- **使用模型**：SeetaFace6 等。

---

### 2. 表情识别 ([ExpressionRecDemo.java](file:///Users/xxx/Documents/idea_workplace/SmartJavaAI/examples/face-example/src/main/java/smartai/examples/face/expression/ExpressionRecDemo.java))
- **功能**：识别 7 种面部表情：中性、高兴、悲伤、惊讶、恐惧、厌恶、愤怒。
- **支持模式**：单人、多人、摄像头实时检测。

---

### 3. 人脸检测 ([FaceDetDemo.java](file:///Users/xxx/Documents/idea_workplace/SmartJavaAI/examples/face-example/src/main/java/smartai/examples/face/facedet/FaceDetDemo.java))
- **功能**：识别图像或视频中的人脸区域，并返回人脸边界框。
- **支持模型**：RetinaFace、SeetaFace6。
---

### 4. 人脸识别 ([FaceRecDemo.java](file:///Users/xxx/Documents/idea_workplace/SmartJavaAI/examples/face-example/src/main/java/smartai/examples/face/facerec/FaceRecDemo.java))
- **功能**：提取人脸特征、进行人脸比对（1:1）、人脸搜索（1:N）、人脸注册管理。
- **支持数据库**：SQLite、Milvus 向量数据库。

---

### 5. 活体检测 ([LivenessDetDemo.java](file:///Users/xxx/Documents/idea_workplace/SmartJavaAI/examples/face-example/src/main/java/smartai/examples/face/liveness/LivenessDetDemo.java))
- **功能**：判断输入图像中人脸是否为真实人脸（非照片、视频伪造）。
- **支持模型**：IIC-FL、MiniVision（双模型融合）。

---

### 6. 人脸质量评估 ([FaceQualityDetDemo.java](file:///Users/xxx/Documents/idea_workplace/SmartJavaAI/examples/face-example/src/main/java/smartai/examples/face/quality/FaceQualityDetDemo.java))
- **功能**：评估人脸图像的质量指标，包括：
    - 亮度 (Brightness)
    - 完整度 (Completeness)
    - 清晰度 (Clarity)
    - 姿态 (Pose)
    - 分辨率 (Resolution)

---

### 7. 工具类 ([ViewerFrame.java](file:///Users/xxx/Documents/idea_workplace/SmartJavaAI/examples/face-example/src/main/java/smartai/examples/face/ViewerFrame.java))
- **功能**：GUI 显示组件，用于展示图像处理结果（如人脸框、表情、活体状态等）。
- **用途**：支持摄像头实时检测时的结果可视化。

---

## ⚙️ 配置要求

- **运行环境**：
    - JDK 1.8 或更高版本
    - IntelliJ IDEA 推荐作为开发 IDE
- **依赖库**：
    - OpenCV、DJL、SmartJavaAI SDK
- **模型路径**：
    - 所有模型需下载并配置正确的路径（参考各 demo 注释中的链接）

---

## 🚀 快速开始

1. 克隆项目到本地：

2. 导入项目至 IntelliJ IDEA。

3. 根据需要修改模型路径（见各 demo 中注释）。

4. 运行对应的 JUnit 测试类方法即可体验各项功能。

---

## 📄 文档

有关完整使用说明，请查阅 SmartJavaAI 官方文档：
[http://doc.smartjavaai.cn](http://doc.smartjavaai.cn)

---

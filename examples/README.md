# SmartJavaAI 示例项目说明

本项目包含多个基于 SmartJavaAI 平台的人脸识别、活体检测及目标检测的示例代码

## 项目结构

```
src/main/java/smartai/examples/
├── face/                    人脸相关示例
│   ├── attribute/           人脸属性检测模块
│   │   └── FaceAttributeDetDemo.java         示例：检测性别、年龄等人脸属性
│   ├── facerec/             人脸识别模块（1:1、1:N）
│   │   ├── FaceNetDemo.java                 示例：使用 FaceNet 算法做人脸识别
│   │   ├── GpuFaceDemo.java                 示例：使用 GPU 加速的人脸识别
│   │   ├── LightFaceDemo.java               示例：轻量级人脸识别模型（适用于嵌入式场景）
│   │   ├── RetinaFaceDemo.java              示例：使用 RetinaFace 进行人脸检测
│   │   └── SeetaFace6Demo.java              示例：集成 SeetaFace6 的人脸识别
│   └── liveness/            活体检测模块
│       ├── LivenessDetDemo.java             示例：基于图像进行活体检测
├── objectdetection/         目标检测模块
│   └── ObjectDetection.java                 示例：使用目标检测模型识别图像中的目标
└── ocr/                     OCR文字识别模块
    ├── OcrDetectionDemo.java                示例：OCR通用文字检测示例
    ├── OcrDirectionDetDemo.java             示例：OCR方向检测示例
    └── OcrRecognizeDemo.java                示例：OCR通用文字识别示例
```

## 快速开始

1. 克隆本项目
2. 导入 IDE（推荐 IntelliJ IDEA）
3. 运行对应 demo 文件即可测试功能（确保模型文件已准备好）

## 模型说明

- 本示例项目配合 `smartjavaai` 平台使用，模型加载及使用方式已封装好。
- 支持 CPU 和 GPU 两种运行模式。
- 所有模型均可通过 Maven 或本地加载方式接入。

## 联系与支持

如需帮助或有建议欢迎通过 Issue 反馈或联系作者。

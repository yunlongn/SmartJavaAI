# OCR文字识别示例


## 📁 项目结构

```

src
├── main
│   ├── java
│   │   └── smartai/examples/ocr
│   │       ├── OcrDetectionDemo.java     # 文本检测示例
│   │       ├── OcrDirectionDetDemo.java  # 文本方向检测示例
│   │       └── OcrRecognizeDemo.java     # 文本识别示例
│   └── resources
│       ├── logback.xml                   # 日志配置文件
└── test


```


---

## 🧩 功能说明

### 1. 文本检测 - [OcrDetectionDemo]

- **功能**：检测图像中的文本区域，仅返回文本框位置，不识别文字内容。


### 2. 文本方向检测 - [OcrDirectionDetDemo]

- **功能**：在文本检测基础上，判断文本整体方向（0°, 90°, 180°, 270°）。

### 3. 文本识别 - [OcrRecognizeDemo]

- **功能**：对检测到的文本区域进行文字识别，支持简体中文、繁体中文、英文、日文等。
- **流程**：
    - 文本检测 → 文本识别（或加上方向矫正）

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

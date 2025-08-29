package cn.smartjavaai.objectdetection.stream;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Mask;
import ai.djl.modality.cv.output.Rectangle;
import cn.hutool.core.lang.UUID;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.enums.VideoSourceType;
import cn.smartjavaai.common.utils.FrameConverterUtil;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.objectdetection.model.DetectorModel;
import cn.smartjavaai.vision.utils.DetectorUtils;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.*;
import java.util.concurrent.*;

/**
 * 视频流目标检测器
 * @author dwj
 */
@Slf4j
public class StreamDetector implements AutoCloseable{

    static {
        OpenCV.loadLocally();
    }


    private DetectorModel detectorModel;
    private String streamUrl;
    private ExecutorService grabberExecutor;   // 专门抓帧的线程
    private ExecutorService processorExecutor; // 专门处理帧的线程
    private int frameDetectionInterval = 1;
    private int repeatGap = 5; // 秒
    private volatile boolean isRunning;
    private FrameGrabber grabber;
    private StreamDetectionListener listener;
    private OpenCVFrameConverter.ToOrgOpenCvCoreMat converterToMat;
    private VideoSourceType sourceType = VideoSourceType.STREAM; // 默认流
    private int cameraIndex = 0; // 默认第一个摄像头

    private Map<String, Long> lastDetectTime = new ConcurrentHashMap<>();
    private BlockingQueue<Frame> frameQueue = new LinkedBlockingQueue<>(100);

    public static Builder builder() { return new Builder(); }

    private StreamDetector(Builder builder) {
        this.detectorModel = builder.detectorModel;
        this.streamUrl = builder.streamUrl;
        this.frameDetectionInterval = builder.frameDetectionInterval;
        this.listener = builder.listener;
        this.sourceType = builder.sourceType;
        this.cameraIndex = builder.cameraIndex;
        this.converterToMat = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();


    }

    private void initializeGrabber() throws FrameGrabber.Exception {
        if (sourceType == VideoSourceType.CAMERA) {
            // 使用用户传入的摄像头索引
            grabber = new OpenCVFrameGrabber(cameraIndex);
        } else {
            grabber = new FFmpegFrameGrabber(streamUrl);
            if (sourceType == VideoSourceType.STREAM) {
                grabber.setOption("rtsp_transport", "tcp");
                grabber.setOption("buffer_size", "1024000");
                grabber.setOption("stimeout", "20000000");
                grabber.setOption("max_delay", "500000");
            }
        }
        grabber.start();
    }

    public void startDetection() {
        if (isRunning) return;
        isRunning = true;

        // 初始化抓帧线程池
        if (grabberExecutor == null) grabberExecutor = Executors.newSingleThreadExecutor();
        // 初始化帧处理线程池
        if (processorExecutor == null) processorExecutor = Executors.newSingleThreadExecutor();

        // 初始化抓帧线程
        grabberExecutor.submit(() -> {
            try {
                initializeGrabber();
                processFrames();
            } catch (Exception e) {
                log.error("视频流处理异常", e);
            } finally {
                release();
            }
        });
        log.info("视频流处理已启动");
        // 初始化队列处理线程：解决回调比较耗时，导致线程池爆满
        startFrameProcessor();
    }

    /**
     * 负责抓取视频帧到队列
     */
    private void processFrames() {
        int frameCount = 0;
        while (isRunning) {
            try {
                Frame frame = grabber.grab();
                if (frame == null || frame.image == null) continue;

                frameCount++;
                if (frameCount % frameDetectionInterval != 0) continue;

                Frame currentFrame = frame.clone();
                frameQueue.offer(currentFrame); // 队列满则丢弃，可改为 put 阻塞
            } catch (Exception e) {
                log.error("抓取视频帧异常", e);
                if (e instanceof FFmpegFrameGrabber.Exception) reconnect();
            }
        }
    }

    /**
     * 负责处理视频帧
     */
    private void startFrameProcessor() {
        processorExecutor.submit(() -> {
            log.info("帧处理线程已启动");
            while (isRunning || !frameQueue.isEmpty()) {
                try {
                    Frame frame = frameQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (frame != null) processFrame(frame);
                } catch (InterruptedException e) {
                   e.printStackTrace();
                } catch (Exception e) {
                    log.error("帧处理异常", e);
                }
            }
        });
    }

    private void processFrame(Frame frame) {
        Mat mat = null;
        try {
            mat = converterToMat.convert(frame);
            if (mat == null) return;

            Image image = ImageFactory.getInstance().fromImage(mat);
            DetectedObjects detectedObjects = detectorModel.detect(image);
//            log.debug("检测结果：{}", detectedObjects.toString());
            DetectionResponse detectionResponse = DetectorUtils.convertToDetectionResponse(detectedObjects, image);
            List<DetectionInfo> filtered = filterRepeatedObjects(detectionResponse);
            if (!filtered.isEmpty() && listener != null) {
                listener.onObjectDetected(filtered, image); // 同帧多物体一次回调
            }
        } catch (Throwable e) {
            e.printStackTrace();
            log.error("单帧处理异常", e);
        }  finally {
            if (mat != null) mat.release();
        }
    }

    private List<DetectionInfo> filterRepeatedObjects(DetectionResponse response) {
        List<DetectionInfo> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (DetectionInfo info : response.getDetectionInfoList()) {
            String name = info.getObjectDetInfo().getClassName();
            Long last = lastDetectTime.get(name);
            if (last == null || (now - last) > repeatGap * 1000) {
                lastDetectTime.put(name, now);
                result.add(info);
            }
        }
        return result;
    }

    private void reconnect() {
        log.info("尝试重新连接视频流");
        try {
            release();
            Thread.sleep(5000);
            initializeGrabber();
        } catch (Exception e) {
            log.error("重新连接RTSP流失败", e);
        }
    }

    public void stopDetection() { isRunning = false; }

    private void release() {
        if (grabber != null) {
            try { grabber.stop(); grabber.release(); }
            catch (FrameGrabber.Exception e) { log.error("释放Grabber失败", e); }
        }
    }

    @Override
    public void close() {
        stopDetection();
        if (grabberExecutor != null) grabberExecutor.shutdownNow();
        if (processorExecutor != null) processorExecutor.shutdownNow();
        release();
    }

    public static class Builder {
        private DetectorModel detectorModel;
        private String streamUrl;
        private ExecutorService executorService;
        private int frameDetectionInterval = 1;
        private StreamDetectionListener listener;
        private VideoSourceType sourceType = VideoSourceType.STREAM; // 默认流
        private int cameraIndex = 0; // 默认第一个摄像头

        public Builder detectorModel(DetectorModel m) { this.detectorModel = m; return this; }
        public Builder streamUrl(String url) { this.streamUrl = url; return this; }
        public Builder executorService(ExecutorService es) { this.executorService = es; return this; }
        public Builder listener(StreamDetectionListener listener) { this.listener = listener; return this; }
        public Builder sourceType(VideoSourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }
        public Builder cameraIndex(int cameraIndex) {
            this.cameraIndex = cameraIndex;
            return this;
        }
        public Builder frameDetectionInterval(int interval) {
            if (interval < 1) throw new IllegalArgumentException("frameDetectionInterval >= 1");
            this.frameDetectionInterval = interval;
            return this;
        }

        public StreamDetector build() {
            if (detectorModel == null) {
                throw new DetectionException("detectorModel 不能为空");
            }

            if (sourceType == null) {
                throw new DetectionException("sourceType 不能为空");
            }

            // 根据 sourceType 校验不同的参数
            switch (sourceType) {
                case STREAM:
                case FILE:
                    if (StringUtils.isBlank(streamUrl)) {
                        throw new DetectionException("streamUrl 不能为空");
                    }
                    break;
                case CAMERA:
                    if (cameraIndex < 0) {
                        throw new DetectionException("cameraIndex 必须 >= 0");
                    }
                    break;
                default:
                    throw new DetectionException("不支持的视频源类型: " + sourceType);
            }

            if (executorService == null) {
                executorService = Executors.newFixedThreadPool(2); // 至少2个线程
            }

            return new StreamDetector(this);
        }

    }

}

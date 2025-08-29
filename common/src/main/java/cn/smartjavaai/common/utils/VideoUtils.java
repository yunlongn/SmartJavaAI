package cn.smartjavaai.common.utils;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * 视频工具类
 * @author dwj
 * @date 2025/7/17
 */
public class VideoUtils {

    /**
     * 视频旋转
     * @param inputPath 输入视频路径
     * @param outputPath 输出视频路径
     * @param angle 旋转角度
     * @param format 视频格式
     * @param videoCodec 视频编码器
     * @throws FFmpegFrameRecorder.Exception
     * @throws FFmpegFrameGrabber.Exception
     */
    public static void rotateVideo(String inputPath, String outputPath, int angle, String format, int videoCodec) throws FFmpegFrameRecorder.Exception, FFmpegFrameGrabber.Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
        grabber.start();
        int inputWidth = grabber.getImageWidth();
        int inputHeight = grabber.getImageHeight();
        int outputWidth = inputWidth;
        int outputHeight = inputHeight;

        if (angle == 90 || angle == 270) {
            outputWidth = inputHeight;
            outputHeight = inputWidth;
        }
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath,
                outputWidth, outputHeight, grabber.getAudioChannels());
        recorder.setVideoCodec(videoCodec);
        recorder.setFormat(format);
        recorder.start();
        Frame frame;
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        while ((frame = grabber.grab()) != null) {
            if (frame.image != null) {
                Mat mat = converter.convert(frame);
                Mat rotated = new Mat();
                switch (angle) {
                    case 90:
                        opencv_core.transpose(mat, rotated);
                        opencv_core.flip(rotated, rotated, 1);
                        break;
                    case 180:
                        opencv_core.flip(mat, rotated, -1);
                        break;
                    case 270:
                        opencv_core.transpose(mat, rotated);
                        opencv_core.flip(rotated, rotated, 0);
                        break;
                    default:
                        rotated = mat.clone();
                        break;
                }
                frame = converter.convert(rotated);
                recorder.record(frame);
            }
        }
        recorder.stop();
        recorder.release();
        grabber.stop();
        grabber.release();
    }

}

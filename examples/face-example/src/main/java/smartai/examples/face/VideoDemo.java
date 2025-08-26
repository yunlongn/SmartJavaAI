package smartai.examples.face;

import cn.smartjavaai.common.utils.VideoUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

/**
 * 视频预处理
 * @author dwj
 * @date 2025/7/17
 */
public class VideoDemo {

    public static void main(String[] args) {
        try {
            VideoUtils.rotateVideo("/Users/wenjie/Downloads/girl.mp4", "/Users/wenjie/Downloads/girl_rotate.mp4", 180,"mp4", avcodec.AV_CODEC_ID_H264);
        } catch (FFmpegFrameRecorder.Exception e) {
            throw new RuntimeException(e);
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

}

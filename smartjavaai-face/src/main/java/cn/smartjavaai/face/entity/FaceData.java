package cn.smartjavaai.face.entity;

import lombok.Data;

/**
 * 人脸数据
 * @author dwj
 */
@Data
public class FaceData {

    private String key;
    private long index;
    private byte[] imgData;
    private int width = 256;
    private int height = 256;
    private int channel = 3;

}

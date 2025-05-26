package cn.smartjavaai.ocr.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * OCR信息
 * @author dwj
 * @date 2025/5/20
 */
@Data
public class OcrInfo {

    private List<List<OcrItem>> lineList;

    private String fullText;


    public OcrInfo(List<List<OcrItem>> lineList, String fullText) {
        this.lineList = lineList;
        this.fullText = fullText;
    }
    public OcrInfo() {
    }
}

package cn.smartjavaai.ocr.entity;

import lombok.Data;

/**
 * 身份证正面信息
 */
@Data
public class IdCardFrontInfo {

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别（男/女）
     */
    private String gender;

    /**
     * 民族（如：汉、满、回等，不带“族”字）
     */
    private String ethnicity;

    /**
     * 公民身份号码
     */
    private String idNumber;

    /**
     * 出生日期，格式：yyyy-MM-dd
     */
    private String birthday;

    /**
     * 住址
     */
    private String address;
}


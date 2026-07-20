package cn.smartjavaai.ocr.entity;

import lombok.Data;

/**
 * 身份证反面信息
 */
@Data
public class IdCardBackInfo {

    /**
     * 签发机关
     */
    private String issuingAuthority;

    /**
     * 有效期开始日期，格式：yyyy-MM-dd
     */
    private String validFrom;

    /**
     * 有效期结束日期，格式：yyyy-MM-dd 或 "长期"
     */
    private String validTo;
}


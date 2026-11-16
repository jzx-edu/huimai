package com.huimai.module.video.dto;

import lombok.Data;

/**
 * 内容来源视图。
 */
@Data
public class ContentSourceVO {

    private Long id;
    private String sourceType;
    private String reference;
    private String titleAr;
    private String note;
}

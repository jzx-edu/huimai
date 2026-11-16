package com.huimai.module.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容来源（经典 / 文献 / 官方资料等权威出处）。
 */
@Data
@TableName("content_source")
public class ContentSource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long videoId;
    private String sourceType;
    private String reference;
    private String titleAr;
    private String note;
    private LocalDateTime createdAt;
}

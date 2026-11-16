package com.huimai.module.report.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报。
 */
@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被举报视频（与 commentId 至少有一个） */
    private Long videoId;

    /** 被举报评论（与 videoId 至少有一个） */
    private Long commentId;

    /** 举报者 */
    private Long userId;

    private String reason;
    private String description;

    /** 0 待处理 1 已处理 2 已驳回 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

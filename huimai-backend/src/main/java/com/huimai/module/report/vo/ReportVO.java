package com.huimai.module.report.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报视图（后台处置用）。
 */
@Data
public class ReportVO {

    private Long id;
    private Long videoId;
    private Long commentId;
    private Long userId;
    private String userName;
    private String reason;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
}

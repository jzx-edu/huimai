package com.huimai.module.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 举报创建请求。
 */
@Data
public class ReportCreateDTO {

    private Long videoId;

    private Long commentId;

    @NotBlank(message = "举报理由不能为空")
    private String reason;

    @Size(max = 512, message = "补充说明过长")
    private String description;
}

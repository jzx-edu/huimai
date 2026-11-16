package com.huimai.module.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 私信发送请求。
 */
@Data
public class MessageSendDTO {

    @NotNull(message = "接收者不能为空")
    private Long receiverId;

    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容过长")
    private String content;
}

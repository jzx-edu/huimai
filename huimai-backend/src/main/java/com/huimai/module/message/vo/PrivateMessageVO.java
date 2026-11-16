package com.huimai.module.message.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息视图。
 */
@Data
public class PrivateMessageVO {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    /** 0 未读 1 已读 */
    private Integer isRead;
    private LocalDateTime createdAt;
}

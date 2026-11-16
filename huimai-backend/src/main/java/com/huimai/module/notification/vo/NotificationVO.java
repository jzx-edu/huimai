package com.huimai.module.notification.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知视图。
 */
@Data
public class NotificationVO {

    private Long id;
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime createdAt;

    /** 触发者信息 */
    private Long fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
}

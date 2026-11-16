package com.huimai.module.message.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信会话视图（按对方用户聚合）。
 */
@Data
public class ConversationVO {

    /** 对方用户 ID */
    private Long userId;
    /** 对方昵称 */
    private String nickname;
    /** 对方头像 */
    private String avatar;
    /** 最后一条消息内容 */
    private String lastContent;
    /** 最后一条消息时间 */
    private LocalDateTime lastTime;
    /** 对方发给我的未读数 */
    private Integer unreadCount;
}

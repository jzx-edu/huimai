package com.huimai.module.notification.service;

import com.huimai.module.notification.vo.NotificationVO;

import java.util.List;

/**
 * 通知服务。
 */
public interface NotificationService {

    /** 产生一条通知 */
    void create(Long fromUserId, Long toUserId, String type,
                String title, String content, Long relatedId);

    /** 通知列表（时间倒序） */
    List<NotificationVO> list(Long userId);

    /** 未读数 */
    long unreadCount(Long userId);

    /** 标记单条已读 */
    void markRead(Long userId, Long notificationId);

    /** 全部标记已读 */
    void markAllRead(Long userId);
}

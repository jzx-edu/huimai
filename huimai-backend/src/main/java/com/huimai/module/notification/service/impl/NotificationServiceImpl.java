package com.huimai.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.huimai.module.notification.entity.Notification;
import com.huimai.module.notification.mapper.NotificationMapper;
import com.huimai.module.notification.service.NotificationService;
import com.huimai.module.notification.vo.NotificationVO;
import com.huimai.module.user.entity.User;
import com.huimai.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知服务实现。
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserService userService;

    @Override
    public void create(Long fromUserId, Long toUserId, String type,
                       String title, String content, Long relatedId) {
        // 自己对自己的操作不产生通知
        if (toUserId.equals(fromUserId)) {
            return;
        }
        Notification n = new Notification();
        n.setUserId(toUserId);
        n.setFromUserId(fromUserId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    @Override
    public List<NotificationVO> list(Long userId) {
        List<Notification> list = notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
                .orderByDesc(Notification::getId));
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> fromIds = list.stream()
                .map(Notification::getFromUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = fromIds.isEmpty()
                ? new HashMap<>()
                : userService.listByIds(fromIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<NotificationVO> result = new ArrayList<>();
        for (Notification n : list) {
            NotificationVO vo = new NotificationVO();
            vo.setId(n.getId());
            vo.setType(n.getType());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setRelatedId(n.getRelatedId());
            vo.setIsRead(n.getIsRead());
            vo.setCreatedAt(n.getCreatedAt());
            vo.setFromUserId(n.getFromUserId());
            if (n.getFromUserId() != null) {
                User u = userMap.get(n.getFromUserId());
                if (u != null) {
                    vo.setFromUserName(u.getNickname());
                    vo.setFromUserAvatar(u.getAvatar());
                }
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public long unreadCount(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }

    @Override
    public void markAllRead(Long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }
}

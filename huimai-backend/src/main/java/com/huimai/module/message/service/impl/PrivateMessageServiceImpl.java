package com.huimai.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.huimai.common.BusinessException;
import com.huimai.module.message.dto.MessageSendDTO;
import com.huimai.module.message.entity.PrivateMessage;
import com.huimai.module.message.mapper.PrivateMessageMapper;
import com.huimai.module.message.service.PrivateMessageService;
import com.huimai.module.message.vo.ConversationVO;
import com.huimai.module.message.vo.PrivateMessageVO;
import com.huimai.module.notification.service.NotificationService;
import com.huimai.module.user.entity.User;
import com.huimai.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 私信服务实现。
 */
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final PrivateMessageMapper messageMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrivateMessageVO send(Long senderId, MessageSendDTO dto) {
        Long receiverId = dto.getReceiverId();
        if (receiverId.equals(senderId)) {
            throw new BusinessException("不能给自己发私信");
        }
        User receiver = userService.getById(receiverId);
        if (receiver == null) {
            throw new BusinessException("接收用户不存在");
        }

        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(dto.getContent().trim());
        message.setIsRead(0);
        messageMapper.insert(message);
        notificationService.create(senderId, receiverId, "message",
                "给你发了私信", message.getContent(), senderId);
        return toVO(message);
    }

    @Override
    public List<ConversationVO> conversations(Long userId) {
        // 取当前用户相关的全部消息，按时间正序，便于后面“后者覆盖”得到最后一条
        List<PrivateMessage> all = messageMapper.selectList(new LambdaQueryWrapper<PrivateMessage>()
                .eq(PrivateMessage::getSenderId, userId)
                .or().eq(PrivateMessage::getReceiverId, userId)
                .orderByAsc(PrivateMessage::getCreatedAt)
                .orderByAsc(PrivateMessage::getId));

        Map<Long, ConversationVO> map = new LinkedHashMap<>();
        Map<Long, Integer> unread = new HashMap<>();
        for (PrivateMessage m : all) {
            Long peer = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            ConversationVO vo = map.computeIfAbsent(peer, k -> new ConversationVO());
            vo.setUserId(peer);
            vo.setLastContent(m.getContent());
            vo.setLastTime(m.getCreatedAt());
            // 未读：对方发给我且尚未读
            if (m.getReceiverId().equals(userId) && m.getIsRead() == 0) {
                unread.merge(peer, 1, Integer::sum);
            }
        }

        if (!map.isEmpty()) {
            Map<Long, User> users = userService.listByIds(map.keySet()).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            for (ConversationVO vo : map.values()) {
                User u = users.get(vo.getUserId());
                if (u != null) {
                    vo.setNickname(u.getNickname());
                    vo.setAvatar(u.getAvatar());
                }
                vo.setUnreadCount(unread.getOrDefault(vo.getUserId(), 0));
            }
        }

        List<ConversationVO> result = new ArrayList<>(map.values());
        result.sort((a, b) -> b.getLastTime().compareTo(a.getLastTime()));
        return result;
    }

    @Override
    public List<PrivateMessageVO> chat(Long currentUserId, Long peerUserId) {
        List<PrivateMessage> list = messageMapper.selectList(new LambdaQueryWrapper<PrivateMessage>()
                .nested(w -> w.eq(PrivateMessage::getSenderId, currentUserId)
                        .eq(PrivateMessage::getReceiverId, peerUserId))
                .or(w -> w.eq(PrivateMessage::getSenderId, peerUserId)
                        .eq(PrivateMessage::getReceiverId, currentUserId))
                .orderByAsc(PrivateMessage::getCreatedAt)
                .orderByAsc(PrivateMessage::getId));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long currentUserId, Long peerUserId) {
        messageMapper.update(null, new LambdaUpdateWrapper<PrivateMessage>()
                .eq(PrivateMessage::getSenderId, peerUserId)
                .eq(PrivateMessage::getReceiverId, currentUserId)
                .eq(PrivateMessage::getIsRead, 0)
                .set(PrivateMessage::getIsRead, 1));
    }

    @Override
    public long unreadCount(Long userId) {
        return messageMapper.selectCount(new LambdaQueryWrapper<PrivateMessage>()
                .eq(PrivateMessage::getReceiverId, userId)
                .eq(PrivateMessage::getIsRead, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId, Long userId) {
        PrivateMessage m = messageMapper.selectById(messageId);
        if (m == null) {
            throw new BusinessException("消息不存在");
        }
        if (!m.getSenderId().equals(userId)) {
            throw new BusinessException("只能删除自己发送的消息");
        }
        // private_message 无逻辑删除字段，物理删除
        messageMapper.deleteById(messageId);
    }

    private PrivateMessageVO toVO(PrivateMessage m) {
        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setId(m.getId());
        vo.setSenderId(m.getSenderId());
        vo.setReceiverId(m.getReceiverId());
        vo.setContent(m.getContent());
        vo.setIsRead(m.getIsRead());
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }
}

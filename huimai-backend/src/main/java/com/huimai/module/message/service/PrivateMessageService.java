package com.huimai.module.message.service;

import com.huimai.module.message.dto.MessageSendDTO;
import com.huimai.module.message.vo.ConversationVO;
import com.huimai.module.message.vo.PrivateMessageVO;

import java.util.List;

/**
 * 私信服务。
 */
public interface PrivateMessageService {

    /** 发送私信 */
    PrivateMessageVO send(Long senderId, MessageSendDTO dto);

    /** 会话列表 */
    List<ConversationVO> conversations(Long userId);

    /** 与某用户的历史消息（时间正序） */
    List<PrivateMessageVO> chat(Long currentUserId, Long peerUserId);

    /** 标记与某用户的消息为已读 */
    void markRead(Long currentUserId, Long peerUserId);

    /** 总未读数 */
    long unreadCount(Long userId);

    /** 删除本人发送的一条消息 */
    void deleteMessage(Long messageId, Long userId);
}

package com.huimai.module.message.controller;

import com.huimai.common.Result;
import com.huimai.module.message.dto.MessageSendDTO;
import com.huimai.module.message.service.PrivateMessageService;
import com.huimai.module.message.vo.ConversationVO;
import com.huimai.module.message.vo.PrivateMessageVO;
import com.huimai.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 私信接口。
 */
@Tag(name = "私信接口")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final PrivateMessageService messageService;

    @Operation(summary = "发送私信")
    @PostMapping("/send")
    public Result<PrivateMessageVO> send(@Valid @RequestBody MessageSendDTO dto) {
        return Result.ok(messageService.send(SecurityUtils.getCurrentUserId(), dto));
    }

    @Operation(summary = "会话列表")
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        return Result.ok(messageService.conversations(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "与某用户的历史消息")
    @GetMapping("/chat/{userId}")
    public Result<List<PrivateMessageVO>> chat(@PathVariable Long userId) {
        return Result.ok(messageService.chat(SecurityUtils.getCurrentUserId(), userId));
    }

    @Operation(summary = "标记与某用户的消息已读")
    @PutMapping("/read/{userId}")
    public Result<Void> markRead(@PathVariable Long userId) {
        messageService.markRead(SecurityUtils.getCurrentUserId(), userId);
        return Result.ok();
    }

    @Operation(summary = "未读消息总数")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(messageService.unreadCount(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "删除一条消息")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        messageService.deleteMessage(id, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }
}

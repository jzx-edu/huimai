package com.huimai.module.notification.controller;

import com.huimai.common.Result;
import com.huimai.module.notification.service.NotificationService;
import com.huimai.module.notification.vo.NotificationVO;
import com.huimai.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通知接口（需登录）。
 */
@Tag(name = "通知接口")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "通知列表")
    @GetMapping("/list")
    public Result<List<NotificationVO>> list() {
        return Result.ok(notificationService.list(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "未读通知数")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(notificationService.unreadCount(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "标记单条已读")
    @PutMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(SecurityUtils.getCurrentUserId(), id);
        return Result.ok();
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead(SecurityUtils.getCurrentUserId());
        return Result.ok();
    }
}

package com.huimai.module.interaction.controller;

import com.huimai.common.Result;
import com.huimai.module.interaction.service.InteractionService;
import com.huimai.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 互动接口（需登录）。
 */
@Tag(name = "互动接口")
@RestController
@RequestMapping("/interaction")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like/{videoId}")
    public Result<Map<String, Boolean>> like(@PathVariable Long videoId) {
        boolean liked = interactionService.toggleLike(videoId, SecurityUtils.getCurrentUserId());
        return Result.ok(payload("liked", liked));
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/favorite/{videoId}")
    public Result<Map<String, Boolean>> favorite(@PathVariable Long videoId) {
        boolean favorited = interactionService.toggleFavorite(videoId, SecurityUtils.getCurrentUserId());
        return Result.ok(payload("favorited", favorited));
    }

    @Operation(summary = "关注/取消关注")
    @PostMapping("/follow/{userId}")
    public Result<Map<String, Boolean>> follow(@PathVariable Long userId) {
        boolean followed = interactionService.toggleFollow(userId, SecurityUtils.getCurrentUserId());
        return Result.ok(payload("followed", followed));
    }

    private Map<String, Boolean> payload(String key, boolean value) {
        Map<String, Boolean> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}

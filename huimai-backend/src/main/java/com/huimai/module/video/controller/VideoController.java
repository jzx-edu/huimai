package com.huimai.module.video.controller;

import com.huimai.common.PageResult;
import com.huimai.common.Result;
import com.huimai.module.video.dto.VideoVO;
import com.huimai.module.video.service.VideoService;
import com.huimai.security.LoginUser;
import com.huimai.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 视频接口。
 */
@Tag(name = "视频接口")
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @Operation(summary = "发布视频")
    @PostMapping("/publish")
    public Result<VideoVO> publish(@RequestParam("file") MultipartFile file,
                                   @RequestParam("title") String title,
                                   @RequestParam(value = "description", required = false) String description,
                                   @RequestParam(value = "categoryId", required = false) Long categoryId,
                                   @RequestParam(value = "isKnowledge", required = false) Integer isKnowledge,
                                   @RequestParam(value = "tags", required = false) List<String> tags,
                                   @RequestParam(value = "sources", required = false) List<String> sources) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(videoService.publish(file, title, description, categoryId,
                isKnowledge, tags, sources, userId));
    }

    @Operation(summary = "竖屏信息流")
    @GetMapping("/feed")
    public Result<PageResult<VideoVO>> feed(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(videoService.feed(page, size, optionalUserId()));
    }

    @Operation(summary = "视频详情")
    @GetMapping("/{id}")
    public Result<VideoVO> detail(@PathVariable Long id) {
        return Result.ok(videoService.detail(id, optionalUserId()));
    }

    @Operation(summary = "上报播放")
    @PostMapping("/{id}/play")
    public Result<Void> play(@PathVariable Long id) {
        videoService.incrementPlay(id, optionalUserId());
        return Result.ok();
    }

    @Operation(summary = "删除视频")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        videoService.delete(id, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @Operation(summary = "分类视频")
    @GetMapping("/category/{categoryId}")
    public Result<PageResult<VideoVO>> byCategory(@PathVariable Long categoryId,
                                                   @RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size) {
        return Result.ok(videoService.byCategory(categoryId, page, size, optionalUserId()));
    }

    @Operation(summary = "用户作品")
    @GetMapping("/user/{userId}")
    public Result<PageResult<VideoVO>> userVideos(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size) {
        return Result.ok(videoService.userVideos(userId, page, size, optionalUserId()));
    }

    @Operation(summary = "搜索")
    @GetMapping("/search")
    public Result<PageResult<VideoVO>> search(@RequestParam String keyword,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size) {
        return Result.ok(videoService.search(keyword, page, size, optionalUserId()));
    }

    /** 匿名访问时返回 null，登录时返回用户 id。 */
    private Long optionalUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}

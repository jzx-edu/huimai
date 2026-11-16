package com.huimai.module.interaction.controller;

import com.huimai.common.PageResult;
import com.huimai.common.Result;
import com.huimai.module.interaction.dto.CommentCreateDTO;
import com.huimai.module.interaction.dto.CommentVO;
import com.huimai.module.interaction.service.CommentService;
import com.huimai.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 评论接口。
 */
@Tag(name = "评论接口")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "发表评论")
    @PostMapping("/{videoId}")
    public Result<CommentVO> add(@PathVariable Long videoId,
                                 @Valid @RequestBody CommentCreateDTO dto) {
        return Result.ok(commentService.add(videoId, SecurityUtils.getCurrentUserId(),
                dto.getContent(), dto.getParentId()));
    }

    @Operation(summary = "评论列表")
    @GetMapping("/list/{videoId}")
    public Result<PageResult<CommentVO>> list(@PathVariable Long videoId,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "20") long size) {
        return Result.ok(commentService.list(videoId, page, size, SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "点赞/取消点赞评论")
    @PostMapping("/like/{id}")
    public Result<Map<String, Boolean>> like(@PathVariable Long id) {
        boolean liked = commentService.likeComment(id, SecurityUtils.getCurrentUserId());
        Map<String, Boolean> payload = new HashMap<>();
        payload.put("liked", liked);
        return Result.ok(payload);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }
}

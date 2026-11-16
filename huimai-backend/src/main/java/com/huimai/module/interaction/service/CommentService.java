package com.huimai.module.interaction.service;

import com.huimai.common.PageResult;
import com.huimai.module.interaction.dto.CommentVO;

/**
 * 评论服务。
 */
public interface CommentService {

    CommentVO add(Long videoId, Long userId, String content, Long parentId);

    PageResult<CommentVO> list(Long videoId, long page, long size, Long currentUserId);

    /** 点赞/取消点赞评论，返回当前是否点赞 */
    boolean likeComment(Long commentId, Long userId);

    /** 删除本人评论（含其下回复） */
    void delete(Long commentId, Long userId);
}

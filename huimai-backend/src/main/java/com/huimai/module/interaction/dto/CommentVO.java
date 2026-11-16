package com.huimai.module.interaction.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论视图。
 */
@Data
public class CommentVO {

    private Long id;
    private Long videoId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long parentId;
    private String content;
    private Long likeCount;
    private Boolean liked;
    private Long replyCount;
    private LocalDateTime createdAt;

    /** 二级回复（一级评论下挂） */
    private List<CommentVO> replies;
}

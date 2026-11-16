package com.huimai.module.interaction.service;

/**
 * 互动服务：点赞、收藏、关注。
 */
public interface InteractionService {

    /**
     * 切换点赞，返回操作后的状态（true=已点赞）。
     */
    boolean toggleLike(Long videoId, Long userId);

    /**
     * 切换收藏，返回操作后的状态（true=已收藏）。
     */
    boolean toggleFavorite(Long videoId, Long userId);

    /**
     * 切换关注，返回操作后的状态（true=已关注）。
     */
    boolean toggleFollow(Long targetUserId, Long followerId);
}

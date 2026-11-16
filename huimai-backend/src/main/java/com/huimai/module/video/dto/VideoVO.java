package com.huimai.module.video.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频视图（信息流 / 详情）。
 */
@Data
public class VideoVO {

    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private String coverUrl;
    private Long duration;
    private Integer width;
    private Integer height;
    private Integer isKnowledge;
    private Integer status;

    private Long playCount;
    private Long likeCount;
    private Long commentCount;
    private Long favoriteCount;
    private Long shareCount;

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private Long categoryId;
    private String categoryName;

    /** 当前用户是否已点赞 / 收藏 */
    private Boolean liked;
    private Boolean favorited;

    private List<ContentSourceVO> sources;

    private LocalDateTime createdAt;
}

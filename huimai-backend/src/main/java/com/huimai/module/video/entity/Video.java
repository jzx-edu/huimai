package com.huimai.module.video.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huimai.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video")
public class Video extends BaseEntity {

    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private String videoUrl;
    private String coverUrl;
    private Long duration;
    private Integer width;
    private Integer height;
    private Long fileSize;
    private Integer isKnowledge;
    private Integer status;
    private Long playCount;
    private Long likeCount;
    private Long commentCount;
    private Long favoriteCount;
    private Long shareCount;
}

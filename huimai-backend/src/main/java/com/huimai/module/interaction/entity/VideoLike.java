package com.huimai.module.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频点赞。
 */
@Data
@TableName("video_like")
public class VideoLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long videoId;
    private Long userId;
    private LocalDateTime createdAt;
}

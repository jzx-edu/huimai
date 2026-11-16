package com.huimai.module.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 播放日志。
 */
@Data
@TableName("play_log")
public class PlayLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long videoId;
    private Long userId;
    private LocalDateTime createdAt;
}

package com.huimai.module.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 视频-标签关联。
 */
@Data
@TableName("video_tag")
public class VideoTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long videoId;
    private Long tagId;
}

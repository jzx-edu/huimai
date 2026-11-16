package com.huimai.module.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huimai.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends BaseEntity {

    private Long videoId;
    private Long userId;
    private Long parentId;
    private String content;
    private Long likeCount;
    private Long replyCount;
    private Integer status;
}

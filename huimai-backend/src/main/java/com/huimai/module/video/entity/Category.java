package com.huimai.module.video.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huimai.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频分类（家庭友好的通用频道）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {

    private String name;
    private String nameAr;
    private String code;
    private String icon;
    private Integer sort;
    private Integer status;
}

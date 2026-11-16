package com.huimai.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huimai.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    /** 商品名称 */
    private String name;

    /** 商品分类 */
    private String category;

    /** 价格（元） */
    private BigDecimal price;

    /** 封面图地址（可空，为空时用渐变+图标占位） */
    private String coverImage;

    /** 占位图标（Vant 图标名） */
    private String icon;

    /** 占位渐变样式 */
    private String gradient;

    /** 商品描述 */
    private String description;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer soldCount;

    /** 1上架 0下架 */
    private Integer status;
}

package com.huimai.module.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品展示对象。
 */
@Data
public class ProductVO {

    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private String coverImage;
    private String icon;
    private String gradient;
    private String description;
    private Integer stock;
    private Integer soldCount;
    private LocalDateTime createdAt;
}

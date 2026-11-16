package com.huimai.module.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单展示对象（含商品快照信息）。
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createdAt;

    // 商品快照信息
    private String productName;
    private String productCoverImage;
    private String productIcon;
    private String productGradient;
    private BigDecimal productPrice;
}

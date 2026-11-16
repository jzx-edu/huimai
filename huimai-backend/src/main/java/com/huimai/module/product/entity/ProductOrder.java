package com.huimai.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huimai.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品订单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_order")
public class ProductOrder extends BaseEntity {

    /** 订单号 */
    private String orderNo;

    /** 下单用户 */
    private Long userId;

    /** 商品 */
    private Long productId;

    /** 购买数量 */
    private Integer quantity;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 0待支付 1已支付 2已取消 */
    private Integer status;
}

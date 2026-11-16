package com.huimai.module.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 下单请求。
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "请选择商品")
    private Long productId;

    @NotNull(message = "请填写购买数量")
    @Min(value = 1, message = "购买数量至少为 1")
    @Max(value = 99, message = "单次购买数量不能超过 99")
    private Integer quantity;
}

package com.huimai.module.product.controller;

import com.huimai.common.Result;
import com.huimai.module.product.dto.OrderCreateDTO;
import com.huimai.module.product.service.ProductService;
import com.huimai.module.product.vo.OrderVO;
import com.huimai.module.product.vo.ProductVO;
import com.huimai.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品接口（需登录）。
 */
@Tag(name = "商品接口")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "商品列表（可按分类、关键词过滤）")
    @GetMapping
    public Result<List<ProductVO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.ok(productService.list(category, keyword));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }

    @Operation(summary = "下单")
    @PostMapping("/order")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok(productService.createOrder(dto, SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "我的订单")
    @GetMapping("/orders")
    public Result<List<OrderVO>> myOrders() {
        return Result.ok(productService.myOrders(SecurityUtils.getCurrentUserId()));
    }
}

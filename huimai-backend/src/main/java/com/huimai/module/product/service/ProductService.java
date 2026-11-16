package com.huimai.module.product.service;

import com.huimai.module.product.dto.OrderCreateDTO;
import com.huimai.module.product.vo.OrderVO;
import com.huimai.module.product.vo.ProductVO;

import java.util.List;

/**
 * 商品服务。
 */
public interface ProductService {

    /** 商品列表（可按分类、关键词过滤，仅返回上架商品） */
    List<ProductVO> list(String category, String keyword);

    /** 商品详情 */
    ProductVO detail(Long id);

    /** 下单（校验库存、扣减库存、增加销量） */
    OrderVO createOrder(OrderCreateDTO dto, Long userId);

    /** 我的订单（按时间倒序） */
    List<OrderVO> myOrders(Long userId);
}

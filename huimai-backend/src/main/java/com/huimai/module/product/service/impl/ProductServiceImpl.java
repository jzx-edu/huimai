package com.huimai.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.huimai.common.BusinessException;
import com.huimai.module.product.dto.OrderCreateDTO;
import com.huimai.module.product.entity.Product;
import com.huimai.module.product.entity.ProductOrder;
import com.huimai.module.product.mapper.ProductMapper;
import com.huimai.module.product.mapper.ProductOrderMapper;
import com.huimai.module.product.service.ProductService;
import com.huimai.module.product.vo.OrderVO;
import com.huimai.module.product.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 商品服务实现。
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductOrderMapper productOrderMapper;

    @Override
    public List<ProductVO> list(String category, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreatedAt)
                .orderByDesc(Product::getId);
        if (StringUtils.hasText(category) && !"全部".equals(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword.trim());
        }
        return productMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVO detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        return toVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderCreateDTO dto, Long userId) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        int quantity = dto.getQuantity();
        // 条件扣减库存并增加销量，库存不足时影响行数为 0，避免超卖
        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, product.getId())
                .ge(Product::getStock, quantity)
                .setSql("stock = stock - " + quantity)
                .setSql("sold_count = sold_count + " + quantity);
        int rows = productMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("库存不足，请减少购买数量");
        }
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        ProductOrder order = new ProductOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        productOrderMapper.insert(order);
        return toOrderVO(order, product);
    }

    @Override
    public List<OrderVO> myOrders(Long userId) {
        List<ProductOrder> orders = productOrderMapper.selectList(
                new LambdaQueryWrapper<ProductOrder>()
                        .eq(ProductOrder::getUserId, userId)
                        .orderByDesc(ProductOrder::getCreatedAt)
                        .orderByDesc(ProductOrder::getId));
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> productIds = orders.stream()
                .map(ProductOrder::getProductId).distinct().collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        return orders.stream()
                .map(o -> toOrderVO(o, productMap.get(o.getProductId())))
                .collect(Collectors.toList());
    }

    private String generateOrderNo() {
        return "HM" + System.currentTimeMillis()
                + String.format("%03d", new Random().nextInt(1000));
    }

    private ProductVO toVO(Product p) {
        ProductVO vo = new ProductVO();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setCategory(p.getCategory());
        vo.setPrice(p.getPrice());
        vo.setCoverImage(p.getCoverImage());
        vo.setIcon(p.getIcon());
        vo.setGradient(p.getGradient());
        vo.setDescription(p.getDescription());
        vo.setStock(p.getStock());
        vo.setSoldCount(p.getSoldCount());
        vo.setCreatedAt(p.getCreatedAt());
        return vo;
    }

    private OrderVO toOrderVO(ProductOrder o, Product p) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setUserId(o.getUserId());
        vo.setProductId(o.getProductId());
        vo.setQuantity(o.getQuantity());
        vo.setTotalAmount(o.getTotalAmount());
        vo.setStatus(o.getStatus());
        vo.setCreatedAt(o.getCreatedAt());
        if (p != null) {
            vo.setProductName(p.getName());
            vo.setProductCoverImage(p.getCoverImage());
            vo.setProductIcon(p.getIcon());
            vo.setProductGradient(p.getGradient());
            vo.setProductPrice(p.getPrice());
        }
        return vo;
    }
}

package com.restaurant.management.product.domain.repository;

import com.restaurant.management.product.domain.model.Product;

import java.util.Optional;

/**
 * 商品仓储接口（领域层定义）
 */
public interface ProductRepository {
    
    /**
     * 保存商品
     */
    Product save(Product product);
    
    /**
     * 根据ID查询
     */
    Optional<Product> findById(Long id);
    
    /**
     * 根据商品名称查询
     */
    Optional<Product> findByProductName(String productName);
}


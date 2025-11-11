package com.restaurant.management.product.domain.service;

import com.restaurant.management.product.domain.model.Product;
import com.restaurant.management.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 商品领域服务
 * 封装商品相关的业务逻辑，包括持久化操作
 */
@Service
@RequiredArgsConstructor
public class ProductDomainService {
    
    private final ProductRepository productRepository;
    
    /**
     * 创建商品
     * 封装了商品创建的完整业务逻辑
     */
    public Product createProduct(String productName, String description, 
                                 BigDecimal price, Long categoryId) {
        // 创建商品聚合（领域模型）
        Product product = Product.create(productName, description, price, categoryId);
        
        // 持久化商品（由领域服务决定如何保存）
        return productRepository.save(product);
    }
    
    /**
     * 创建商品（带初始库存）
     * 封装了商品创建的完整业务逻辑，并发布领域事件
     */
    public Product createProductWithInventory(String productName, String description, 
                                              BigDecimal price, Long categoryId, 
                                              Integer initialQuantity) {
        // 创建商品聚合（领域模型）
        Product product = Product.create(productName, description, price, categoryId, initialQuantity);
        
        // 持久化商品（由领域服务决定如何保存）
        product = productRepository.save(product);
        
        // 保存后发布领域事件（此时ID已生成）
        product.publishProductCreatedEvent(initialQuantity);
        
        return product;
    }
    
    /**
     * 更新商品价格
     */
    public Product updatePrice(Long productId, BigDecimal newPrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 调用聚合根的业务方法
        product.updatePrice(newPrice);
        
        // 持久化状态变更
        return productRepository.save(product);
    }
    
    /**
     * 下架商品
     */
    public Product deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        product.deactivate();
        return productRepository.save(product);
    }
    
    /**
     * 上架商品
     */
    public Product activateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        product.activate();
        return productRepository.save(product);
    }
    
    /**
     * 根据ID查询商品
     */
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }
}


package com.restaurant.management.product.application;

import com.restaurant.management.common.domain.DomainEventPublisher;
import com.restaurant.management.product.application.command.CreateProductCommand;
import com.restaurant.management.product.domain.model.Product;
import com.restaurant.management.product.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品应用服务
 * 职责：
 * 1. 协调领域服务（不直接调用Repository）
 * 2. 处理事务边界
 * 3. DTO与领域对象的转换
 * 4. 发布领域事件（最终一致性场景）
 * 
 * 规则：
 * - 强一致性：应用层编排，直接调用领域服务
 * - 最终一致性：使用领域事件，由事件处理器异步处理
 */
@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    
    private final ProductDomainService productDomainService;
    private final DomainEventPublisher domainEventPublisher;
    
    /**
     * 创建商品
     * 使用领域事件实现最终一致性：
     * 1. 创建商品聚合
     * 2. 发布领域事件（ProductCreatedEvent）
     * 3. 库存上下文监听事件，异步创建库存
     * 
     * 注意：这是最终一致性，不需要强一致性
     */
    @Transactional
    public Product createProduct(CreateProductCommand command) {
        Product product = productDomainService.createProductWithInventory(
                command.getProductName(),
                command.getDescription(),
                command.getPrice(),
                command.getInitialQuantity()
        );
        
        domainEventPublisher.publishAll(product.getDomainEvents());
        product.clearDomainEvents();
        return product;
    }
    
    /**
     * 根据ID查询商品
     */
    public Product getProduct(String productId) {
        return productDomainService.getProduct(productId);
    }
}


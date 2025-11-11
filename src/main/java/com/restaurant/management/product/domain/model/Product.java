package com.restaurant.management.product.domain.model;

import com.restaurant.management.common.domain.AggregateRoot;
import com.restaurant.management.common.domain.BaseEntity;
import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.product.domain.event.ProductCreatedEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品聚合根
 */
@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends BaseEntity implements AggregateRoot {
    
    @Transient  // 不持久化到数据库
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "price", nullable = false)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    /**
     * 创建商品
     * @param productName 商品名称
     * @param description 商品描述
     * @param price 价格
     * @param categoryId 分类ID
     * @param initialQuantity 初始库存数量（用于发布事件）
     */
    public static Product create(String productName, String description, 
                                 BigDecimal price, Long categoryId, Integer initialQuantity) {
        Product product = new Product();
        product.productName = productName;
        product.description = description;
        product.price = price;
        product.categoryId = categoryId;
        product.status = ProductStatus.ACTIVE;
        
        // 发布领域事件（如果提供了初始库存数量）
        if (initialQuantity != null && initialQuantity > 0) {
            // 注意：此时product.getId()可能为null，需要在保存后设置
            // 所以事件会在保存后发布
        }
        
        return product;
    }
    
    /**
     * 创建商品（不包含初始库存）
     */
    public static Product create(String productName, String description, 
                                 BigDecimal price, Long categoryId) {
        return create(productName, description, price, categoryId, null);
    }
    
    /**
     * 发布商品创建事件
     * 在商品保存后调用，此时ID已生成
     */
    public void publishProductCreatedEvent(Integer initialQuantity) {
        if (initialQuantity != null && initialQuantity > 0) {
            addDomainEvent(new ProductCreatedEvent(this.getId(), this.productName, initialQuantity));
        }
    }
    
    @Override
    public List<DomainEvent> getDomainEvents() {
        return domainEvents;
    }
    
    @Override
    public void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    @Override
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    /**
     * 更新价格
     */
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("商品价格必须大于0");
        }
        this.price = newPrice;
    }
    
    /**
     * 下架商品
     */
    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }
    
    /**
     * 上架商品
     */
    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }
}


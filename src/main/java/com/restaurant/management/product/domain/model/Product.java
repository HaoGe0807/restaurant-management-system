package com.restaurant.management.product.domain.model;

import com.restaurant.management.common.domain.AggregateRoot;
import com.restaurant.management.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 商品聚合根
 */
@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends BaseEntity implements AggregateRoot {
    
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
     */
    public static Product create(String productName, String description, 
                                 BigDecimal price, Long categoryId) {
        Product product = new Product();
        product.productName = productName;
        product.description = description;
        product.price = price;
        product.categoryId = categoryId;
        product.status = ProductStatus.ACTIVE;
        return product;
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


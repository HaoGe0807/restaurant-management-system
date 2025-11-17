package com.restaurant.management.product.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.AggregateRoot;
import com.restaurant.management.common.domain.BaseEntity;
import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.product.domain.event.ProductCreatedEvent;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 商品聚合根
 */
@Getter
@Setter
@TableName("products")
public class Product extends BaseEntity implements AggregateRoot {
    
    /**
     * 存储该商品聚合根产生的所有领域事件
     */
    @TableField(exist = false)
    private List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 商品的唯一标识符
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品的详细描述信息。
     */
    private String description;
    
    /**
     * 商品的售价。
     */
    private BigDecimal price;
    
    /**
     * 表示商品的当前状态，如上架或下架。
     */
    private ProductStatus status;
    
    /**
     * 创建商品
     * @param productName 商品名称
     * @param description 商品描述
     * @param price 价格
     */
    public static Product create(String productName, String description,
                                 BigDecimal price) {
        Product product = new Product();
        product.productId = generateProductId();
        product.productName = productName;
        product.description = description;
        product.price = price;
        product.status = ProductStatus.ACTIVE;

        return product;
    }

    /**
     * 发布商品创建事件
     * 在商品保存后调用，此时ID已生成
     */
    public void publishProductCreatedEvent(int initialQuantity) {
        addDomainEvent(new ProductCreatedEvent(this.productId, this.productName, initialQuantity));
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

    private static String generateProductId() {
        return "PRO" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}


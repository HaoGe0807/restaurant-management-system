package com.company.ecommerce.inventory.domain.model;

import com.company.ecommerce.common.domain.AggregateRoot;
import com.company.ecommerce.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 库存聚合根
 */
@Entity
@Table(name = "inventories")
@Getter
@Setter
public class Inventory extends BaseEntity implements AggregateRoot {
    
    @Column(name = "product_id", unique = true, nullable = false)
    private Long productId;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;
    
    /**
     * 创建库存
     */
    public static Inventory create(Long productId, Integer initialQuantity) {
        Inventory inventory = new Inventory();
        inventory.productId = productId;
        inventory.availableQuantity = initialQuantity;
        inventory.reservedQuantity = 0;
        return inventory;
    }
    
    /**
     * 预留库存
     */
    public void reserve(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("预留数量必须大于0");
        }
        if (availableQuantity < quantity) {
            throw new IllegalStateException("可用库存不足");
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }
    
    /**
     * 释放预留库存
     */
    public void releaseReserved(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("释放数量必须大于0");
        }
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("预留库存不足");
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }
    
    /**
     * 扣减库存（确认预留）
     */
    public void deduct(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("扣减数量必须大于0");
        }
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("预留库存不足");
        }
        reservedQuantity -= quantity;
    }
    
    /**
     * 增加库存
     */
    public void increase(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("增加数量必须大于0");
        }
        availableQuantity += quantity;
    }
    
    /**
     * 获取总库存
     */
    public Integer getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }
}


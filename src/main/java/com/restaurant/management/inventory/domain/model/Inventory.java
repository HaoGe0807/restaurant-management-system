package com.restaurant.management.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.AggregateRoot;
import com.restaurant.management.common.domain.BaseEntity;
import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.inventory.domain.event.InventoryChangedEvent;
import com.restaurant.management.inventory.domain.event.InventoryReservedEvent;
import com.restaurant.management.inventory.domain.event.InventoryReleasedEvent;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 库存聚合根
 * 增强版库存管理，支持多仓库、成本管理、预留机制等
 */
@Getter
@Setter
@TableName("inventories")
public class Inventory extends BaseEntity implements AggregateRoot {
    
    /**
     * 存储该聚合根产生的所有领域事件（仅存在于内存）
     */
    @TableField(exist = false)
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    @TableField("sku_id")
    private String skuId;
    
    @TableField("warehouse_id")
    private String warehouseId;
    
    private Integer availableQuantity;
    
    private Integer reservedQuantity;
    
    private Integer occupiedQuantity;
    
    @TableField("unit_cost")
    private BigDecimal unitCost;
    
    @TableField("safety_stock")
    private Integer safetyStock;
    
    @TableField("max_stock")
    private Integer maxStock;
    
    private InventoryStatus status;
    
    /**
     * 创建库存
     */
    public static Inventory create(String skuId, String warehouseId, int initialQuantity) {
        Inventory inventory = new Inventory();
        inventory.skuId = skuId;
        inventory.warehouseId = warehouseId;
        inventory.availableQuantity = initialQuantity;
        inventory.reservedQuantity = 0;
        inventory.occupiedQuantity = 0;
        inventory.unitCost = BigDecimal.ZERO;
        inventory.safetyStock = 0;
        inventory.maxStock = Integer.MAX_VALUE;
        inventory.status = InventoryStatus.NORMAL;
        
        // 发布库存创建事件
        inventory.addDomainEvent(new InventoryChangedEvent(
            skuId, warehouseId, 0, initialQuantity, "初始化库存"
        ));
        
        return inventory;
    }
    
    /**
     * 创建库存（带成本）
     */
    public static Inventory create(String skuId, String warehouseId, int initialQuantity, BigDecimal unitCost) {
        Inventory inventory = create(skuId, warehouseId, initialQuantity);
        inventory.unitCost = unitCost;
        return inventory;
    }
    
    /**
     * 预留库存
     */
    public void reserve(Integer quantity, String orderId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("预留数量必须大于0");
        }
        if (status == InventoryStatus.FROZEN) {
            throw new IllegalStateException("库存已冻结，无法预留");
        }
        if (availableQuantity < quantity) {
            throw new IllegalStateException(
                String.format("可用库存不足，需要: %d, 可用: %d", quantity, availableQuantity));
        }
        
        availableQuantity -= quantity;
        reservedQuantity += quantity;
        
        // 发布库存预留事件
        addDomainEvent(new InventoryReservedEvent(skuId, warehouseId, quantity, orderId));
    }
    
    /**
     * 释放预留库存
     */
    public void releaseReserved(Integer quantity, String orderId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("释放数量必须大于0");
        }
        if (reservedQuantity < quantity) {
            throw new IllegalStateException(
                String.format("预留库存不足，需要释放: %d, 预留: %d", quantity, reservedQuantity));
        }
        
        reservedQuantity -= quantity;
        availableQuantity += quantity;
        
        // 发布库存释放事件
        addDomainEvent(new InventoryReleasedEvent(skuId, warehouseId, quantity, orderId));
    }
    
    /**
     * 确认预留（预留转占用）
     */
    public void confirmReserved(Integer quantity, String orderId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("确认数量必须大于0");
        }
        if (reservedQuantity < quantity) {
            throw new IllegalStateException(
                String.format("预留库存不足，需要确认: %d, 预留: %d", quantity, reservedQuantity));
        }
        
        reservedQuantity -= quantity;
        occupiedQuantity += quantity;
        
        // 发布库存确认事件
        addDomainEvent(new InventoryChangedEvent(
            skuId, warehouseId, -quantity, getTotalQuantity(), 
            String.format("确认预留库存，订单: %s", orderId)
        ));
    }
    
    /**
     * 扣减库存（直接扣减可用库存）
     */
    public void deduct(Integer quantity, String reason) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("扣减数量必须大于0");
        }
        if (status == InventoryStatus.FROZEN) {
            throw new IllegalStateException("库存已冻结，无法扣减");
        }
        if (availableQuantity < quantity) {
            throw new IllegalStateException(
                String.format("可用库存不足，需要扣减: %d, 可用: %d", quantity, availableQuantity));
        }
        
        availableQuantity -= quantity;
        
        // 发布库存变更事件
        addDomainEvent(new InventoryChangedEvent(
            skuId, warehouseId, -quantity, getTotalQuantity(), reason
        ));
    }
    
    /**
     * 增加库存
     */
    public void increase(Integer quantity, BigDecimal cost, String reason) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("增加数量必须大于0");
        }
        
        // 计算加权平均成本
        if (cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalCost = unitCost.multiply(BigDecimal.valueOf(getTotalQuantity()))
                .add(cost.multiply(BigDecimal.valueOf(quantity)));
            int newTotalQuantity = getTotalQuantity() + quantity;
            if (newTotalQuantity > 0) {
                unitCost = totalCost.divide(BigDecimal.valueOf(newTotalQuantity), 4, BigDecimal.ROUND_HALF_UP);
            }
        }
        
        availableQuantity += quantity;
        
        // 发布库存变更事件
        addDomainEvent(new InventoryChangedEvent(
            skuId, warehouseId, quantity, getTotalQuantity(), reason
        ));
    }
    
    /**
     * 调整库存（盘点调整）
     */
    public void adjust(Integer newQuantity, String reason) {
        int oldQuantity = getTotalQuantity();
        int changeQuantity = newQuantity - oldQuantity;
        
        // 重新分配库存
        availableQuantity = newQuantity - reservedQuantity - occupiedQuantity;
        if (availableQuantity < 0) {
            // 如果调整后可用库存为负，需要调整预留和占用库存
            int deficit = Math.abs(availableQuantity);
            if (reservedQuantity >= deficit) {
                reservedQuantity -= deficit;
                availableQuantity = 0;
            } else {
                occupiedQuantity -= (deficit - reservedQuantity);
                reservedQuantity = 0;
                availableQuantity = 0;
            }
        }
        
        // 发布库存调整事件
        addDomainEvent(new InventoryChangedEvent(
            skuId, warehouseId, changeQuantity, getTotalQuantity(), 
            String.format("库存调整: %s", reason)
        ));
    }
    
    /**
     * 冻结库存
     */
    public void freeze(String reason) {
        if (status == InventoryStatus.FROZEN) {
            return;
        }
        
        status = InventoryStatus.FROZEN;
        
        // 发布库存状态变更事件
        addDomainEvent(new InventoryChangedEvent(
            skuId, warehouseId, 0, getTotalQuantity(), 
            String.format("库存冻结: %s", reason)
        ));
    }
    
    /**
     * 解冻库存
     */
    public void unfreeze(String reason) {
        if (status == InventoryStatus.NORMAL) {
            return;
        }
        
        status = InventoryStatus.NORMAL;
        
        // 发布库存状态变更事件
        addDomainEvent(new InventoryChangedEvent(
            skuId, warehouseId, 0, getTotalQuantity(), 
            String.format("库存解冻: %s", reason)
        ));
    }
    
    /**
     * 获取总库存
     */
    public Integer getTotalQuantity() {
        return availableQuantity + reservedQuantity + occupiedQuantity;
    }
    
    /**
     * 检查是否需要补货
     */
    public boolean needsReplenishment() {
        return getTotalQuantity() <= safetyStock;
    }
    
    /**
     * 检查是否库存积压
     */
    public boolean isOverstocked() {
        return getTotalQuantity() >= maxStock;
    }
    
    /**
     * 设置安全库存
     */
    public void setSafetyStock(Integer safetyStock) {
        if (safetyStock < 0) {
            throw new IllegalArgumentException("安全库存不能为负数");
        }
        this.safetyStock = safetyStock;
    }
    
    /**
     * 设置最大库存
     */
    public void setMaxStock(Integer maxStock) {
        if (maxStock <= 0) {
            throw new IllegalArgumentException("最大库存必须大于0");
        }
        if (maxStock <= safetyStock) {
            throw new IllegalArgumentException("最大库存必须大于安全库存");
        }
        this.maxStock = maxStock;
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
}


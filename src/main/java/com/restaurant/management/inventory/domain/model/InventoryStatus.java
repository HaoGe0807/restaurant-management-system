package com.restaurant.management.inventory.domain.model;

/**
 * 库存状态枚举
 */
public enum InventoryStatus {
    
    /**
     * 正常状态 - 可以进行所有库存操作
     */
    NORMAL("正常"),
    
    /**
     * 冻结状态 - 不能进行库存变更操作，通常用于商品下架或异常情况
     */
    FROZEN("冻结");
    
    private final String description;
    
    InventoryStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
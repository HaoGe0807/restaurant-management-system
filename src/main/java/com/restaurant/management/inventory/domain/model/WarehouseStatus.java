package com.restaurant.management.inventory.domain.model;

/**
 * 仓库状态枚举
 */
public enum WarehouseStatus {
    
    /**
     * 激活状态 - 可以进行正常的库存操作
     */
    ACTIVE("激活"),
    
    /**
     * 停用状态 - 不能进行库存操作
     */
    INACTIVE("停用");
    
    private final String description;
    
    WarehouseStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
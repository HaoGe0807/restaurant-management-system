package com.restaurant.management.inventory.domain.model;

/**
 * 仓库类型枚举
 */
public enum WarehouseType {
    
    /**
     * 中央仓库 - 主要的存储和配送中心
     */
    CENTRAL("中央仓库"),
    
    /**
     * 区域仓库 - 服务特定区域的仓库
     */
    REGIONAL("区域仓库"),
    
    /**
     * 门店仓库 - 门店自有的小型仓库
     */
    STORE("门店仓库"),
    
    /**
     * 临时仓库 - 临时使用的仓库
     */
    TEMPORARY("临时仓库"),
    
    /**
     * 虚拟仓库 - 用于特殊业务场景的虚拟仓库
     */
    VIRTUAL("虚拟仓库");
    
    private final String description;
    
    WarehouseType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
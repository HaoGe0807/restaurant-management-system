package com.restaurant.management.inventory.domain.repository;

import com.restaurant.management.inventory.domain.model.Inventory;

import java.util.Optional;

/**
 * 库存仓储接口（领域层定义）
 */
public interface InventoryRepository {
    
    /**
     * 保存库存
     */
    Inventory save(Inventory inventory);
    
    /**
     * 根据商品ID查询
     */
    Optional<Inventory> findByProductId(Long productId);
    
    /**
     * 根据ID查询
     */
    Optional<Inventory> findById(Long id);
}


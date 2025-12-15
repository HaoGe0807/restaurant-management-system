package com.restaurant.management.inventory.domain.repository;

import com.restaurant.management.inventory.domain.model.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * 库存仓储接口
 * 定义库存数据访问的抽象接口
 */
public interface InventoryRepository {
    
    /**
     * 保存库存
     */
    Inventory save(Inventory inventory);
    
    /**
     * 根据SKU ID和仓库ID查找库存
     */
    Optional<Inventory> findBySkuIdAndWarehouseId(String skuId, String warehouseId);
    
    /**
     * 根据SKU ID查找所有仓库的库存
     */
    List<Inventory> findBySkuId(String skuId);
    
    /**
     * 根据仓库ID查找所有库存
     */
    List<Inventory> findByWarehouseId(String warehouseId);
    
    /**
     * 根据SPU ID查找所有相关库存
     */
    List<Inventory> findBySpuId(String spuId);
    
    /**
     * 删除库存
     */
    void delete(Inventory inventory);
    
    /**
     * 根据ID删除库存
     */
    void deleteById(Long id);
    
    /**
     * 根据SKU ID和仓库ID删除库存
     */
    void deleteBySkuIdAndWarehouseId(String skuId, String warehouseId);
    
    /**
     * 检查库存是否存在
     */
    boolean existsBySkuIdAndWarehouseId(String skuId, String warehouseId);
    
    /**
     * 查找需要补货的库存（库存量低于安全库存）
     */
    List<Inventory> findInventoriesNeedingReplenishment();
    
    /**
     * 查找库存积压的商品（库存量超过最大库存）
     */
    List<Inventory> findOverstockedInventories();
    
    /**
     * 根据状态查找库存
     */
    List<Inventory> findByStatus(com.restaurant.management.inventory.domain.model.InventoryStatus status);
}
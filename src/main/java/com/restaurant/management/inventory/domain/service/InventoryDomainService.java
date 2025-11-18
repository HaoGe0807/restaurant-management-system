package com.restaurant.management.inventory.domain.service;

import com.restaurant.management.inventory.domain.model.Inventory;
import com.restaurant.management.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 库存领域服务
 * 封装库存相关的业务逻辑，包括持久化操作
 */
@Service
@RequiredArgsConstructor
public class InventoryDomainService {
    
    private final InventoryRepository inventoryRepository;
    
    /**
     * 创建库存
     * 封装了库存创建的完整业务逻辑
     */
    public Inventory createInventory(String skuId, int initialQuantity) {
        // 检查是否已存在该商品的库存
        if (inventoryRepository.findBySkuId(skuId).isPresent()) {
            throw new RuntimeException("该 SKU 已存在库存记录");
        }
        
        // 创建库存聚合（领域模型）
        Inventory inventory = Inventory.create(skuId, initialQuantity);
        
        // 持久化库存（由领域服务决定如何保存）
        return inventoryRepository.save(inventory);
    }
    
    /**
     * 预留库存
     */
    public Inventory reserveInventory(String skuId, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new RuntimeException("库存不存在"));
        
        // 调用聚合根的业务方法
        inventory.reserve(quantity);
        
        // 持久化状态变更
        return inventoryRepository.save(inventory);
    }
    
    /**
     * 释放预留库存
     */
    public Inventory releaseReservedInventory(String skuId, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new RuntimeException("库存不存在"));
        
        inventory.releaseReserved(quantity);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * 扣减库存
     */
    public Inventory deductInventory(String skuId, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new RuntimeException("库存不存在"));
        
        inventory.deduct(quantity);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * 增加库存
     */
    public Inventory increaseInventory(String skuId, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new RuntimeException("库存不存在"));
        
        inventory.increase(quantity);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * 根据商品ID查询库存
     */
    public Inventory getInventoryBySkuId(String skuId) {
        return inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new RuntimeException("库存不存在"));
    }
}


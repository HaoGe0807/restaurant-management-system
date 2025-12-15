package com.restaurant.management.inventory.domain.service;

import com.restaurant.management.inventory.domain.model.Inventory;
import com.restaurant.management.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 库存领域服务
 * 处理库存相关的业务逻辑和跨聚合操作
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryDomainService {
    
    private final InventoryRepository inventoryRepository;
    
    /**
     * 创建库存
     */
    public Inventory createInventory(Inventory inventory) {
        // 验证库存数据
        validateInventory(inventory);
        
        // 检查是否已存在
        if (existsInventory(inventory.getSkuId(), inventory.getWarehouseId())) {
            throw new IllegalStateException(
                String.format("库存记录已存在: skuId=%s, warehouseId=%s", 
                    inventory.getSkuId(), inventory.getWarehouseId()));
        }
        
        // 保存库存
        return inventoryRepository.save(inventory);
    }
    
    /**
     * 更新库存
     */
    public Inventory updateInventory(Inventory inventory) {
        validateInventory(inventory);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * 检查库存是否存在
     */
    public boolean existsInventory(String skuId, String warehouseId) {
        return inventoryRepository.findBySkuIdAndWarehouseId(skuId, warehouseId).isPresent();
    }
    
    /**
     * 根据SKU ID和仓库ID获取库存
     */
    public Optional<Inventory> getInventory(String skuId, String warehouseId) {
        return inventoryRepository.findBySkuIdAndWarehouseId(skuId, warehouseId);
    }
    
    /**
     * 根据SKU ID获取所有仓库的库存
     */
    public List<Inventory> findInventoriesBySkuId(String skuId) {
        return inventoryRepository.findBySkuId(skuId);
    }
    
    /**
     * 根据SPU ID获取所有相关库存
     */
    public List<Inventory> findInventoriesBySpuId(String spuId) {
        return inventoryRepository.findBySpuId(spuId);
    }
    
    /**
     * 更新商品信息
     */
    public void updateProductInfo(String skuId, String skuName, String spuName) {
        List<Inventory> inventories = findInventoriesBySkuId(skuId);
        
        for (Inventory inventory : inventories) {
            // 这里可以扩展库存实体来包含商品信息字段
            // 目前库存实体只包含skuId，如果需要存储商品名称等信息，需要扩展实体
            log.info("更新库存商品信息: skuId={}, skuName={}, spuName={}", skuId, skuName, spuName);
            
            // 如果需要，可以在这里更新库存记录中的商品信息
            // inventory.setSkuName(skuName);
            // inventory.setSpuName(spuName);
            // inventoryRepository.save(inventory);
        }
    }
    
    /**
     * 删除库存
     */
    public void deleteInventory(String skuId, String warehouseId) {
        Optional<Inventory> inventoryOpt = getInventory(skuId, warehouseId);
        
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            
            // 验证是否可以删除
            validateInventoryForDeletion(inventory);
            
            // 删除库存记录
            inventoryRepository.delete(inventory);
            
            log.info("删除库存记录: skuId={}, warehouseId={}", skuId, warehouseId);
        } else {
            log.warn("库存记录不存在，无法删除: skuId={}, warehouseId={}", skuId, warehouseId);
        }
    }
    
    /**
     * 预留库存
     */
    public void reserveInventory(String skuId, String warehouseId, Integer quantity, String orderId) {
        Inventory inventory = getInventory(skuId, warehouseId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("库存不存在: skuId=%s, warehouseId=%s", skuId, warehouseId)));
        
        inventory.reserve(quantity, orderId);
        inventoryRepository.save(inventory);
        
        log.info("预留库存: skuId={}, warehouseId={}, quantity={}, orderId={}", 
            skuId, warehouseId, quantity, orderId);
    }
    
    /**
     * 释放预留库存
     */
    public void releaseReservedInventory(String skuId, String warehouseId, Integer quantity, String orderId) {
        Inventory inventory = getInventory(skuId, warehouseId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("库存不存在: skuId=%s, warehouseId=%s", skuId, warehouseId)));
        
        inventory.releaseReserved(quantity, orderId);
        inventoryRepository.save(inventory);
        
        log.info("释放预留库存: skuId={}, warehouseId={}, quantity={}, orderId={}", 
            skuId, warehouseId, quantity, orderId);
    }
    
    /**
     * 确认预留库存
     */
    public void confirmReservedInventory(String skuId, String warehouseId, Integer quantity, String orderId) {
        Inventory inventory = getInventory(skuId, warehouseId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("库存不存在: skuId=%s, warehouseId=%s", skuId, warehouseId)));
        
        inventory.confirmReserved(quantity, orderId);
        inventoryRepository.save(inventory);
        
        log.info("确认预留库存: skuId={}, warehouseId={}, quantity={}, orderId={}", 
            skuId, warehouseId, quantity, orderId);
    }
    
    /**
     * 检查库存可用性
     */
    public boolean checkInventoryAvailability(String skuId, String warehouseId, Integer quantity) {
        Optional<Inventory> inventoryOpt = getInventory(skuId, warehouseId);
        
        if (inventoryOpt.isEmpty()) {
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        return inventory.getAvailableQuantity() >= quantity && 
               inventory.getStatus() == com.restaurant.management.inventory.domain.model.InventoryStatus.NORMAL;
    }
    
    /**
     * 获取库存汇总信息
     */
    public InventorySummary getInventorySummary(String skuId) {
        List<Inventory> inventories = findInventoriesBySkuId(skuId);
        
        int totalAvailable = inventories.stream()
            .mapToInt(Inventory::getAvailableQuantity)
            .sum();
            
        int totalReserved = inventories.stream()
            .mapToInt(Inventory::getReservedQuantity)
            .sum();
            
        int totalOccupied = inventories.stream()
            .mapToInt(Inventory::getOccupiedQuantity)
            .sum();
        
        return new InventorySummary(skuId, totalAvailable, totalReserved, totalOccupied);
    }
    
    /**
     * 验证库存数据
     */
    private void validateInventory(Inventory inventory) {
        if (inventory.getSkuId() == null || inventory.getSkuId().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU ID不能为空");
        }
        
        if (inventory.getWarehouseId() == null || inventory.getWarehouseId().trim().isEmpty()) {
            throw new IllegalArgumentException("仓库ID不能为空");
        }
        
        if (inventory.getAvailableQuantity() < 0) {
            throw new IllegalArgumentException("可用库存不能为负数");
        }
        
        if (inventory.getReservedQuantity() < 0) {
            throw new IllegalArgumentException("预留库存不能为负数");
        }
        
        if (inventory.getOccupiedQuantity() < 0) {
            throw new IllegalArgumentException("占用库存不能为负数");
        }
    }
    
    /**
     * 验证库存是否可以删除
     */
    private void validateInventoryForDeletion(Inventory inventory) {
        if (inventory.getReservedQuantity() > 0) {
            throw new IllegalStateException(
                String.format("存在预留库存，无法删除: skuId=%s, reserved=%d", 
                    inventory.getSkuId(), inventory.getReservedQuantity()));
        }
        
        if (inventory.getOccupiedQuantity() > 0) {
            throw new IllegalStateException(
                String.format("存在占用库存，无法删除: skuId=%s, occupied=%d", 
                    inventory.getSkuId(), inventory.getOccupiedQuantity()));
        }
    }
    
    /**
     * 库存汇总信息
     */
    public static class InventorySummary {
        private final String skuId;
        private final int totalAvailable;
        private final int totalReserved;
        private final int totalOccupied;
        
        public InventorySummary(String skuId, int totalAvailable, int totalReserved, int totalOccupied) {
            this.skuId = skuId;
            this.totalAvailable = totalAvailable;
            this.totalReserved = totalReserved;
            this.totalOccupied = totalOccupied;
        }
        
        public String getSkuId() { return skuId; }
        public int getTotalAvailable() { return totalAvailable; }
        public int getTotalReserved() { return totalReserved; }
        public int getTotalOccupied() { return totalOccupied; }
        public int getTotalQuantity() { return totalAvailable + totalReserved + totalOccupied; }
    }
}
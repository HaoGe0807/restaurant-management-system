package com.restaurant.management.inventory.application.eventhandler;

import com.restaurant.management.inventory.domain.model.Inventory;
import com.restaurant.management.inventory.domain.service.InventoryDomainService;
import com.restaurant.management.product.domain.event.ProductCreatedEvent;
import com.restaurant.management.product.domain.event.ProductDeletionEvent;
import com.restaurant.management.product.domain.event.ProductStatusChangedEvent;
import com.restaurant.management.product.domain.event.ProductUpdatedEvent;
import com.restaurant.management.product.domain.model.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品事件处理器
 * 负责处理商品相关事件，实现商品与库存的自动联动
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventHandler {
    
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * 处理商品创建事件
     * 自动为新创建的商品SKU创建库存记录
     */
    @EventListener
    @Transactional
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("处理商品创建事件: spuId={}, spuName={}, skuCount={}", 
            event.getSpuId(), event.getSpuName(), event.getSkus().size());
        
        try {
            // 为每个SKU创建库存记录
            for (ProductCreatedEvent.SkuSnapshot skuSnapshot : event.getSkus()) {
                // 获取默认仓库ID（实际项目中可能需要从配置或业务规则中获取）
                String defaultWarehouseId = getDefaultWarehouseId();
                
                // 检查库存是否已存在
                if (!inventoryDomainService.existsInventory(skuSnapshot.getSkuId(), defaultWarehouseId)) {
                    // 创建库存记录
                    Inventory inventory = Inventory.create(
                        skuSnapshot.getSkuId(), 
                        defaultWarehouseId, 
                        skuSnapshot.getInitialQuantity()
                    );
                    
                    inventoryDomainService.createInventory(inventory);
                    
                    log.info("为SKU创建库存记录: skuId={}, warehouseId={}, initialQuantity={}", 
                        skuSnapshot.getSkuId(), defaultWarehouseId, skuSnapshot.getInitialQuantity());
                } else {
                    log.warn("SKU库存记录已存在，跳过创建: skuId={}, warehouseId={}", 
                        skuSnapshot.getSkuId(), defaultWarehouseId);
                }
            }
            
            log.info("商品创建事件处理完成: spuId={}", event.getSpuId());
            
        } catch (Exception e) {
            log.error("处理商品创建事件失败: spuId={}", event.getSpuId(), e);
            throw e;
        }
    }
    
    /**
     * 处理商品更新事件
     * 同步更新库存记录中的商品信息
     */
    @EventListener
    @Transactional
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.info("处理商品更新事件: spuId={}, oldName={}, newName={}", 
            event.getSpuId(), event.getOldSpuName(), event.getNewSpuName());
        
        try {
            // 更新所有相关SKU的库存记录中的商品信息
            event.getSkus().forEach(sku -> {
                inventoryDomainService.updateProductInfo(
                    sku.getSkuId(), 
                    sku.getSkuName(), 
                    event.getNewSpuName()
                );
                
                log.debug("更新SKU库存信息: skuId={}, skuName={}", 
                    sku.getSkuId(), sku.getSkuName());
            });
            
            log.info("商品更新事件处理完成: spuId={}", event.getSpuId());
            
        } catch (Exception e) {
            log.error("处理商品更新事件失败: spuId={}", event.getSpuId(), e);
            throw e;
        }
    }
    
    /**
     * 处理商品删除事件
     * 安全删除相关的库存数据
     */
    @EventListener
    @Transactional
    public void handleProductDeletion(ProductDeletionEvent event) {
        log.info("处理商品删除事件: spuId={}, skuIds={}", 
            event.getSpuId(), event.getSkuIds());
        
        try {
            // 验证并删除每个SKU的库存记录
            for (String skuId : event.getSkuIds()) {
                // 检查库存状态，确保可以安全删除
                List<Inventory> inventories = inventoryDomainService.findInventoriesBySkuId(skuId);
                
                for (Inventory inventory : inventories) {
                    // 验证库存是否可以删除
                    validateInventoryForDeletion(inventory);
                    
                    // 删除库存记录
                    inventoryDomainService.deleteInventory(inventory.getSkuId(), inventory.getWarehouseId());
                    
                    log.info("删除SKU库存记录: skuId={}, warehouseId={}", 
                        inventory.getSkuId(), inventory.getWarehouseId());
                }
            }
            
            log.info("商品删除事件处理完成: spuId={}", event.getSpuId());
            
        } catch (Exception e) {
            log.error("处理商品删除事件失败: spuId={}", event.getSpuId(), e);
            throw e;
        }
    }
    
    /**
     * 处理商品状态变更事件
     * 根据商品状态变更冻结或解冻库存
     */
    @EventListener
    @Transactional
    public void handleProductStatusChanged(ProductStatusChangedEvent event) {
        log.info("处理商品状态变更事件: spuId={}, oldStatus={}, newStatus={}", 
            event.getSpuId(), event.getOldStatus(), event.getNewStatus());
        
        try {
            // 获取商品下所有SKU的库存
            List<Inventory> inventories = inventoryDomainService.findInventoriesBySpuId(event.getSpuId());
            
            for (Inventory inventory : inventories) {
                if (event.getNewStatus() == ProductStatus.INACTIVE) {
                    // 商品下架，冻结库存
                    inventory.freeze(String.format("商品下架: %s", event.getSpuId()));
                    log.info("冻结库存: skuId={}, warehouseId={}", 
                        inventory.getSkuId(), inventory.getWarehouseId());
                        
                } else if (event.getNewStatus() == ProductStatus.ACTIVE && 
                          event.getOldStatus() == ProductStatus.INACTIVE) {
                    // 商品重新上架，解冻库存
                    inventory.unfreeze(String.format("商品上架: %s", event.getSpuId()));
                    log.info("解冻库存: skuId={}, warehouseId={}", 
                        inventory.getSkuId(), inventory.getWarehouseId());
                }
                
                // 保存库存变更
                inventoryDomainService.updateInventory(inventory);
            }
            
            log.info("商品状态变更事件处理完成: spuId={}", event.getSpuId());
            
        } catch (Exception e) {
            log.error("处理商品状态变更事件失败: spuId={}", event.getSpuId(), e);
            throw e;
        }
    }
    
    /**
     * 验证库存是否可以删除
     */
    private void validateInventoryForDeletion(Inventory inventory) {
        // 检查是否有预留库存
        if (inventory.getReservedQuantity() > 0) {
            throw new IllegalStateException(
                String.format("SKU[%s]存在预留库存，无法删除", inventory.getSkuId()));
        }
        
        // 检查是否有占用库存
        if (inventory.getOccupiedQuantity() > 0) {
            throw new IllegalStateException(
                String.format("SKU[%s]存在占用库存，无法删除", inventory.getSkuId()));
        }
        
        // 如果有可用库存，给出警告但允许删除
        if (inventory.getAvailableQuantity() > 0) {
            log.warn("SKU[{}]存在可用库存{}，将被删除", 
                inventory.getSkuId(), inventory.getAvailableQuantity());
        }
    }
    
    /**
     * 获取默认仓库ID
     * 实际项目中可能需要从配置服务或业务规则中获取
     */
    private String getDefaultWarehouseId() {
        // 这里简化处理，返回默认仓库ID
        // 实际项目中可能需要：
        // 1. 从配置中心获取默认仓库
        // 2. 根据商品类型选择仓库
        // 3. 根据地理位置选择最近的仓库
        return "WH001"; // 默认仓库
    }
}
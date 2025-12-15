package com.restaurant.management.inventory.application;

import com.restaurant.management.inventory.application.command.ReserveInventoryCommand;
import com.restaurant.management.inventory.domain.model.Inventory;
import com.restaurant.management.inventory.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存应用服务
 * 职责：
 * 1. 协调领域服务（不直接调用Repository）
 * 2. 处理事务边界
 * 3. DTO与领域对象的转换
 */
@Service
@RequiredArgsConstructor
public class InventoryApplicationService {
    
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * 预留库存
     */
    @Transactional
    public Inventory reserveInventory(ReserveInventoryCommand command) {
        // 调用领域服务，由领域层处理业务逻辑和持久化
        String defaultWarehouseId = "DEFAULT_WAREHOUSE";
        inventoryDomainService.reserveInventory(
                command.getSkuId(),
                defaultWarehouseId,
                command.getQuantity(),
                command.getOrderId()
        );
        
        // 返回更新后的库存信息
        return inventoryDomainService.getInventory(command.getSkuId(), defaultWarehouseId)
                .orElseThrow(() -> new IllegalStateException("库存记录不存在"));
    }
    
    /**
     * 根据商品ID和仓库ID查询库存
     */
    public Inventory getInventory(String skuId, String warehouseId) {
        return inventoryDomainService.getInventory(skuId, warehouseId)
                .orElse(null);
    }
    
    /**
     * 根据商品ID查询默认仓库库存
     */
    public Inventory getInventoryBySkuId(String skuId) {
        String defaultWarehouseId = "DEFAULT_WAREHOUSE";
        return inventoryDomainService.getInventory(skuId, defaultWarehouseId)
                .orElse(null);
    }
}


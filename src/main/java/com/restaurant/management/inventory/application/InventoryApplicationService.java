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
        return inventoryDomainService.reserveInventory(
                command.getProductId(),
                command.getQuantity()
        );
    }
    
    /**
     * 根据商品ID查询库存
     */
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryDomainService.getInventoryByProductId(productId);
    }
}


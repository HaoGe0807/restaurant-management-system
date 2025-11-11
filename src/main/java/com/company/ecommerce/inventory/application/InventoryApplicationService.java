package com.company.ecommerce.inventory.application;

import com.company.ecommerce.inventory.application.command.ReserveInventoryCommand;
import com.company.ecommerce.inventory.domain.model.Inventory;
import com.company.ecommerce.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存应用服务
 */
@Service
@RequiredArgsConstructor
public class InventoryApplicationService {
    
    private final InventoryRepository inventoryRepository;
    
    /**
     * 预留库存
     */
    @Transactional
    public Inventory reserveInventory(ReserveInventoryCommand command) {
        Inventory inventory = inventoryRepository.findByProductId(command.getProductId())
                .orElseThrow(() -> new RuntimeException("库存不存在"));
        
        inventory.reserve(command.getQuantity());
        return inventoryRepository.save(inventory);
    }
    
    /**
     * 根据商品ID查询库存
     */
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("库存不存在"));
    }
}


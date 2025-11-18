package com.restaurant.management.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.management.inventory.domain.model.Inventory;
import com.restaurant.management.inventory.domain.repository.InventoryRepository;
import com.restaurant.management.inventory.infrastructure.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存仓储实现（MyBatis-Plus）
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    
    private final InventoryMapper inventoryMapper;
    
    @Override
    public Inventory save(Inventory inventory) {
        if (inventory.getId() == null) {
            inventoryMapper.insert(inventory);
        } else {
            inventoryMapper.updateById(inventory);
        }
        return inventory;
    }
    
    @Override
    public Optional<Inventory> findBySkuId(String skuId) {
        return Optional.ofNullable(inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getSkuId, skuId)
        ));
    }
    
    @Override
    public Optional<Inventory> findById(Long id) {
        return Optional.ofNullable(inventoryMapper.selectById(id));
    }
}


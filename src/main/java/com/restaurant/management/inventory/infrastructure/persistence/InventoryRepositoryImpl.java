package com.restaurant.management.inventory.infrastructure.persistence;

import com.restaurant.management.inventory.domain.model.Inventory;
import com.restaurant.management.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存仓储实现（基础设施层）
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    
    private final InventoryJpaRepository jpaRepository;
    
    @Override
    public Inventory save(Inventory inventory) {
        return jpaRepository.save(inventory);
    }
    
    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }
    
    @Override
    public Optional<Inventory> findById(Long id) {
        return jpaRepository.findById(id);
    }
}


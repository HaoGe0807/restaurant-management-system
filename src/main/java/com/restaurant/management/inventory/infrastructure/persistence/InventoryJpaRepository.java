package com.restaurant.management.inventory.infrastructure.persistence;

import com.restaurant.management.inventory.domain.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存JPA仓储
 */
@Repository
public interface InventoryJpaRepository extends JpaRepository<Inventory, Long> {
    
    Optional<Inventory> findByProductId(Long productId);
}


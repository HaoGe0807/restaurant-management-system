package com.restaurant.management.product.infrastructure.persistence;

import com.restaurant.management.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商品JPA仓储
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByProductName(String productName);
}


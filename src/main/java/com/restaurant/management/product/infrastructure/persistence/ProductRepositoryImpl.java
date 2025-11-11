package com.restaurant.management.product.infrastructure.persistence;

import com.restaurant.management.product.domain.model.Product;
import com.restaurant.management.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商品仓储实现（基础设施层）
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    
    private final ProductJpaRepository jpaRepository;
    
    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }
    
    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<Product> findByProductName(String productName) {
        return jpaRepository.findByProductName(productName);
    }
}


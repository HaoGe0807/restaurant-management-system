package com.restaurant.management.product.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.management.product.domain.model.Product;
import com.restaurant.management.product.domain.repository.ProductRepository;
import com.restaurant.management.product.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商品仓储实现（MyBatis-Plus）
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    
    private final ProductMapper productMapper;
    
    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.updateById(product);
        }
        return product;
    }
    
    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(productMapper.selectById(id));
    }

    @Override
    public Optional<Product> findByProductId(String productId) {
        return Optional.ofNullable(productMapper.selectOne(
            new LambdaQueryWrapper<Product>().eq(Product::getProductId, productId)
        ));
    }

    @Override
    public Optional<Product> findByProductName(String productName) {
        return Optional.ofNullable(productMapper.selectOne(
                new LambdaQueryWrapper<Product>().eq(Product::getProductName, productName)
        ));
    }
}


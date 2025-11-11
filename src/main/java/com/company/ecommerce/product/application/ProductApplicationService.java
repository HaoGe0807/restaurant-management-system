package com.company.ecommerce.product.application;

import com.company.ecommerce.product.application.command.CreateProductCommand;
import com.company.ecommerce.product.domain.model.Product;
import com.company.ecommerce.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品应用服务
 */
@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    
    private final ProductRepository productRepository;
    
    /**
     * 创建商品
     */
    @Transactional
    public Product createProduct(CreateProductCommand command) {
        Product product = Product.create(
                command.getProductName(),
                command.getDescription(),
                command.getPrice(),
                command.getCategoryId()
        );
        return productRepository.save(product);
    }
    
    /**
     * 根据ID查询商品
     */
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }
}


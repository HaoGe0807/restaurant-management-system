package com.restaurant.management.product.domain.service;

import com.restaurant.management.product.domain.model.ProductSku;
import com.restaurant.management.product.domain.model.ProductSpu;
import com.restaurant.management.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品领域服务
 */
@Service
@RequiredArgsConstructor
public class ProductDomainService {

    private final ProductRepository productRepository;

    /**
     * 创建 SPU 及其 SKU，并发布领域事件
     */
    public ProductSpu createProductWithSkus(String spuName, String description, List<ProductSku> skus) {
        if (skus == null || skus.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个 SKU");
        }

        ProductSpu spu = ProductSpu.create(spuName, description);
        skus.forEach(spu::addSku);

        spu = productRepository.save(spu);

        spu.publishProductCreatedEvent();

        return spu;
    }

    public ProductSpu getProductSpu(String spuId) {
        return productRepository.findBySpuId(spuId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    public ProductSku getProductSku(String skuId) {
        return productRepository.findSkuBySkuId(skuId)
                .orElseThrow(() -> new RuntimeException("SKU不存在"));
    }
}


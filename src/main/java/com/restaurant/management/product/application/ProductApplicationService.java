package com.restaurant.management.product.application;

import com.restaurant.management.common.domain.DomainEventPublisher;
import com.restaurant.management.product.application.command.SaveProductCommand;
import com.restaurant.management.product.domain.model.ProductSku;
import com.restaurant.management.product.domain.model.ProductSpu;
import com.restaurant.management.product.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品应用服务（SPU + SKU）
 */
@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductDomainService productDomainService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public ProductSpu createProduct(SaveProductCommand command) {
        if (command.getSkus() == null || command.getSkus().isEmpty()) {
            throw new IllegalArgumentException("至少需要一个SKU");
        }

        List<ProductSku> skus = command.getSkus().stream()
                .map(skuCmd -> ProductSku.create(
                        skuCmd.getSkuName(),
                        skuCmd.getPrice(),
                        skuCmd.getAttributes(),
                        skuCmd.getInitialQuantity()
                ))
                .collect(Collectors.toList());

        ProductSpu productSpu = productDomainService.createProduct(
                command.getSpuName(),
                command.getDescription(),
                skus
        );

        domainEventPublisher.publishAll(productSpu.getDomainEvents());
        productSpu.clearDomainEvents();
        return productSpu;
    }

    public ProductSpu getProduct(String spuId) {
        return productDomainService.getProductSpu(spuId);
    }

    public ProductSku getSku(String skuId) {
        return productDomainService.getProductSku(skuId);
    }

    @Transactional
    public ProductSpu updateProduct(String spuId, SaveProductCommand command) {
        if (command.getSkus() == null || command.getSkus().isEmpty()) {
            throw new IllegalArgumentException("至少需要一个SKU");
        }

        List<ProductSku> skus = command.getSkus().stream()
                .map(skuCmd -> ProductSku.create(
                        skuCmd.getSkuName(),
                        skuCmd.getPrice(),
                        skuCmd.getAttributes(),
                        skuCmd.getInitialQuantity()
                ))
                .collect(Collectors.toList());

        ProductSpu productSpu = productDomainService.updateProduct(
                spuId,
                command.getSpuName(),
                command.getDescription(),
                skus
        );

        return productSpu;
    }

    public List<ProductSpu> getProductList(int pageNum, int pageSize) {
        return productDomainService.getProductList(pageNum, pageSize);
    }
}


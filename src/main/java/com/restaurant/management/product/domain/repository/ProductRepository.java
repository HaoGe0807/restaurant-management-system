package com.restaurant.management.product.domain.repository;

import com.restaurant.management.product.domain.model.ProductSku;
import com.restaurant.management.product.domain.model.ProductSpu;

import java.util.List;
import java.util.Optional;

/**
 * 商品仓储接口（SPU 聚合）
 */
public interface ProductRepository {

    ProductSpu save(ProductSpu productSpu);

    Optional<ProductSpu> findBySpuId(String spuId);

    Optional<ProductSpu> findBySpuName(String spuName);

    Optional<ProductSku> findSkuBySkuId(String skuId);

    Optional<ProductSku> findSkuBySpuIdAndName(String spuId, String skuName);

    /**
     * 查询商品列表（分页）
     * @param pageNum 页码，从1开始
     * @param pageSize 每页大小
     * @return 商品列表
     */
    List<ProductSpu> findAll(int pageNum, int pageSize);
}

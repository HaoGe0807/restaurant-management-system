package com.restaurant.management.product.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.management.product.domain.model.ProductSku;
import com.restaurant.management.product.domain.model.ProductSpu;
import com.restaurant.management.product.domain.repository.ProductRepository;
import com.restaurant.management.product.infrastructure.mapper.ProductMapper;
import com.restaurant.management.product.infrastructure.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 商品仓储实现（SPU + SKU）
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;

    @Override
    @Transactional
    public ProductSpu save(ProductSpu productSpu) {
        if (productSpu.getId() == null) {
            productMapper.insert(productSpu);
        } else {
            productMapper.updateById(productSpu);
            productSkuMapper.delete(new LambdaQueryWrapper<ProductSku>()
                    .eq(ProductSku::getSpuId, productSpu.getSpuId()));
        }

        for (ProductSku sku : productSpu.getSkus()) {
            sku.setSpuId(productSpu.getSpuId());
            productSkuMapper.insert(sku);
        }
        return productSpu;
    }

    @Override
    public Optional<ProductSpu> findBySpuId(String spuId) {
        ProductSpu spu = productMapper.selectOne(new LambdaQueryWrapper<ProductSpu>()
                .eq(ProductSpu::getSpuId, spuId));
        if (spu == null) {
            return Optional.empty();
        }
        List<ProductSku> skus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSpuId, spuId));
        spu.replaceSkus(skus);
        return Optional.of(spu);
    }

    @Override
    public Optional<ProductSpu> findBySpuName(String spuName) {
        ProductSpu spu = productMapper.selectOne(new LambdaQueryWrapper<ProductSpu>()
                .eq(ProductSpu::getSpuName, spuName));
        if (spu == null) {
            return Optional.empty();
        }
        List<ProductSku> skus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSpuId, spu.getSpuId()));
        spu.replaceSkus(skus);
        return Optional.of(spu);
    }

    @Override
    public Optional<ProductSku> findSkuBySkuId(String skuId) {
        return Optional.ofNullable(productSkuMapper.selectOne(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSkuId, skuId)));
    }

    @Override
    public Optional<ProductSku> findSkuBySpuIdAndName(String spuId, String skuName) {
        return Optional.ofNullable(productSkuMapper.selectOne(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSpuId, spuId)
                .eq(ProductSku::getSkuName, skuName)));
    }
}


package com.restaurant.management.product.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.management.common.infrastructure.cache.MultiLevelCacheManager;
import com.restaurant.management.product.domain.model.ProductSku;
import com.restaurant.management.product.domain.model.ProductSpu;
import com.restaurant.management.product.domain.repository.ProductRepository;
import com.restaurant.management.product.infrastructure.mapper.ProductMapper;
import com.restaurant.management.product.infrastructure.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品仓储实现（SPU + SKU）
 * 集成两级缓存：本地缓存（Caffeine）+ Redis 缓存
 * 更新策略：删除模式（Cache-Aside Pattern）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final MultiLevelCacheManager cacheManager;

    // 缓存名称常量
    private static final String CACHE_NAME_SPU = "product:spu:";
    private static final String CACHE_NAME_SPU_BY_NAME = "product:spu:name:";
    private static final String CACHE_NAME_SKU = "product:sku:";

    @Override
    @Transactional
    public ProductSpu save(ProductSpu productSpu) {
        // 根据 spuId（业务ID）查询是否存在
        ProductSpu existingSpu = productMapper.selectOne(
                new LambdaQueryWrapper<ProductSpu>().eq(ProductSpu::getSpuId, productSpu.getSpuId()));
        
        String oldSpuName = null;

        if (existingSpu == null) {
            // 新增：根据 spuId 判断，不存在则新增
            productMapper.insert(productSpu);
            upsertSkus(productSpu);
        } else {
            // 更新：根据 spuId 判断，存在则更新
            oldSpuName = existingSpu.getSpuName();
            
            // 设置数据库ID，用于更新
            productSpu.setId(existingSpu.getId());
            productMapper.updateById(productSpu);

            // 处理 SKU 的更新
            // 1. 先查询数据库中现有的 SKU
            List<ProductSku> existingSkus = productSkuMapper.selectList(
                    new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSpuId, productSpu.getSpuId()));

            // 2. 计算需要保留的 SKU ID（传入的新 SKU 列表）
            Set<String> incomingSkuIds = productSpu.getSkus() == null ? new HashSet<>() :
                    productSpu.getSkus().stream()
                            .map(ProductSku::getSkuId)
                            .collect(Collectors.toSet());

            // 3. 先删除不再需要的 SKU（在数据库中存在，但不在新列表中的）
            deleteRemovedSkus(productSpu.getSpuId(), existingSkus, incomingSkuIds);

            // 4. 然后插入或更新新的 SKU（upsert 会处理新增和更新）
            if (productSpu.getSkus() != null && !productSpu.getSkus().isEmpty()) {
                upsertSkus(productSpu);
            }
        }

        // 删除模式：清除相关缓存
        evictProductCache(productSpu, oldSpuName);

        return productSpu;
    }

    /**
     * 清除商品相关缓存（删除模式）
     */
    private void evictProductCache(ProductSpu productSpu, String oldSpuName) {
        String spuId = productSpu.getSpuId();
        String spuName = productSpu.getSpuName();

        // 清除 SPU 缓存（按 spuId）- 使用完整 key
        String spuFullKey = CACHE_NAME_SPU  + spuId;
        cacheManager.evict(spuFullKey);
        log.debug("已清除 SPU 缓存，spuId: {}", spuId);

        // 清除 SPU 缓存（按 spuName）- 使用完整 key
        String nameFullKey = CACHE_NAME_SPU_BY_NAME + spuName;
        cacheManager.evict(nameFullKey);
        log.debug("已清除 SPU 名称缓存，spuName: {}", spuName);

        // 如果名称变更，清除旧名称的缓存
        if (oldSpuName != null && !oldSpuName.equals(spuName)) {
            String oldNameFullKey = CACHE_NAME_SPU_BY_NAME + oldSpuName;
            cacheManager.evict(oldNameFullKey);
            log.debug("已清除旧 SPU 名称缓存，oldSpuName: {}", oldSpuName);
        }

        // 清除该 SPU 下所有 SKU 的缓存
        if (productSpu.getSkus() != null) {
            for (ProductSku sku : productSpu.getSkus()) {
                String skuFullKey = CACHE_NAME_SKU + sku.getSkuId();
                cacheManager.evict(skuFullKey);
            }
            log.debug("已清除 SKU 缓存，spuId: {}, skuCount: {}", spuId, productSpu.getSkus().size());
        }
    }

    private Set<String> upsertSkus(ProductSpu productSpu) {
        if (productSpu.getSkus() == null || productSpu.getSkus().isEmpty()) {
            return new HashSet<>();
        }
        for (ProductSku sku : productSpu.getSkus()) {
            sku.setSpuId(productSpu.getSpuId());
        }
        productSkuMapper.upsertSkus(productSpu.getSkus());
        return productSpu.getSkus().stream()
                .map(ProductSku::getSkuId)
                .collect(Collectors.toSet());
    }

    private void deleteRemovedSkus(String spuId, List<ProductSku> existingSkus, Set<String> incomingSkuIds) {
        if (existingSkus == null || existingSkus.isEmpty()) {
            return;
        }
        List<String> toDelete = existingSkus.stream()
                .map(ProductSku::getSkuId)
                .filter(skuId -> !incomingSkuIds.contains(skuId))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            // 删除数据库中的 SKU
            productSkuMapper.delete(new LambdaQueryWrapper<ProductSku>()
                    .eq(ProductSku::getSpuId, spuId)
                    .in(ProductSku::getSkuId, toDelete));
            
            // 清除被删除的 SKU 的缓存
            for (String deletedSkuId : toDelete) {
                String skuFullKey = CACHE_NAME_SKU + deletedSkuId;
                cacheManager.evict(skuFullKey);
                log.debug("已清除被删除的 SKU 缓存，skuId: {}", deletedSkuId);
            }
        }
    }

    @Override
    public Optional<ProductSpu> findBySpuId(String spuId) {
        // 使用完整 key（本地缓存和 Redis 都使用此 key）
        String fullKey = CACHE_NAME_SPU + spuId;

        // 使用两级缓存查询
        ProductSpu spu = cacheManager.get(
                fullKey,
                () -> {
                    // 缓存未命中时，从数据库加载
                    ProductSpu dbSpu = productMapper.selectOne(new LambdaQueryWrapper<ProductSpu>()
                            .eq(ProductSpu::getSpuId, spuId));
                    if (dbSpu == null) {
                        return null;
                    }

                    // 加载 SKU 列表
                    List<ProductSku> skus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                            .eq(ProductSku::getSpuId, spuId));
                    dbSpu.replaceSkus(skus);

                    return dbSpu;
                }
        );

        return Optional.ofNullable(spu);
    }

    @Override
    public Optional<ProductSpu> findBySpuName(String spuName) {
        // 使用完整 key（本地缓存和 Redis 都使用此 key）
        String fullKey = CACHE_NAME_SPU_BY_NAME + spuName;

        // 使用两级缓存查询
        ProductSpu spu = cacheManager.get(
                fullKey,
                () -> {
                    // 缓存未命中时，从数据库加载
                    ProductSpu dbSpu = productMapper.selectOne(new LambdaQueryWrapper<ProductSpu>()
                            .eq(ProductSpu::getSpuName, spuName));
                    if (dbSpu == null) {
                        return null;
                    }

                    // 加载 SKU 列表
                    List<ProductSku> skus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                            .eq(ProductSku::getSpuId, dbSpu.getSpuId()));
                    dbSpu.replaceSkus(skus);

                    return dbSpu;
                }
        );

        return Optional.ofNullable(spu);
    }

    @Override
    public Optional<ProductSku> findSkuBySkuId(String skuId) {
        // 使用完整 key（本地缓存和 Redis 都使用此 key）
        String fullKey = CACHE_NAME_SKU + skuId;

        // 使用两级缓存查询
        ProductSku sku = cacheManager.get(
                fullKey,
                () -> {
                    // 缓存未命中时，从数据库加载
                    return productSkuMapper.selectOne(new LambdaQueryWrapper<ProductSku>()
                            .eq(ProductSku::getSkuId, skuId));
                }
        );

        return Optional.ofNullable(sku);
    }

    @Override
    public Optional<ProductSku> findSkuBySpuIdAndName(String spuId, String skuName) {
        return Optional.ofNullable(productSkuMapper.selectOne(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSpuId, spuId)
                .eq(ProductSku::getSkuName, skuName)));
    }

    @Override
    public List<ProductSpu> findAll(int pageNum, int pageSize) {
        Page<ProductSpu> page = new Page<>(pageNum, pageSize);
        Page<ProductSpu> result = productMapper.selectPage(page, new LambdaQueryWrapper<ProductSpu>()
                .orderByDesc(ProductSpu::getCreateTime));
        
        List<ProductSpu> spus = result.getRecords();
        if (spus.isEmpty()) {
            return spus;
        }
        
        // 批量查询所有 SKU
        List<String> spuIds = spus.stream()
                .map(ProductSpu::getSpuId)
                .collect(Collectors.toList());
        List<ProductSku> allSkus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .in(ProductSku::getSpuId, spuIds));
        
        // 按 spuId 分组
        Map<String, List<ProductSku>> skuMap = allSkus.stream()
                .collect(Collectors.groupingBy(ProductSku::getSpuId));
        
        // 为每个 SPU 设置对应的 SKU 列表
        spus.forEach(spu -> spu.replaceSkus(skuMap.getOrDefault(spu.getSpuId(), Collections.emptyList())));
        
        return spus;
    }
}


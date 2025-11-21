package com.restaurant.management.product.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.management.product.domain.model.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    /**
     * 批量插入或更新 SKU（基于 sku_id 唯一键）
     */
    void upsertSkus(@Param("skus") List<ProductSku> skus);
}



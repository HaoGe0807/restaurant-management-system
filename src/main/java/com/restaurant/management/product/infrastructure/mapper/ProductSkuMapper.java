package com.restaurant.management.product.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.management.product.domain.model.ProductSku;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
}



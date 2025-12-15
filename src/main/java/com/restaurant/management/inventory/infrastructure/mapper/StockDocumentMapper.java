package com.restaurant.management.inventory.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.management.inventory.domain.model.StockDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存单据Mapper接口
 * 基于MyBatis-Plus的数据访问层
 */
@Mapper
public interface StockDocumentMapper extends BaseMapper<StockDocument> {
    
}
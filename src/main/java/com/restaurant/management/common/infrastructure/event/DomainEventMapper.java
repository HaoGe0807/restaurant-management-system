package com.restaurant.management.common.infrastructure.event;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 领域事件Mapper
 */
@Mapper
public interface DomainEventMapper extends BaseMapper<DomainEventEntity> {
    
    /**
     * 查询待发布的事件
     */
    List<DomainEventEntity> findPendingEvents(int limit);
}


package com.restaurant.management.common.infrastructure.event;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 标记事件为处理中
     * @return 受影响行数（用于并发控制）
     */
    int markProcessing(Long id);

    /**
     * 标记事件为发布成功
     */
    int markPublished(Long id);

    /**
     * 标记事件为待重试（保存错误信息）
     */
    int markPending(@Param("id") Long id, @Param("errorMessage") String errorMessage);

    /**
     * 标记事件为失败（不再重试）
     */
    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}


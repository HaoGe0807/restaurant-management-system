package com.restaurant.management.order.domain.repository;

import com.restaurant.management.order.domain.model.Order;

import java.util.Optional;

/**
 * 订单仓储接口（领域层定义）
 */
public interface OrderRepository {
    
    /**
     * 保存订单
     */
    Order save(Order order);
    
    /**
     * 根据订单号查询
     */
    Optional<Order> findByOrderNo(String orderNo);
    
    /**
     * 根据ID查询
     */
    Optional<Order> findById(Long id);
}


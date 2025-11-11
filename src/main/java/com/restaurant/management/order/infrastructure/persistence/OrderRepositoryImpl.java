package com.restaurant.management.order.infrastructure.persistence;

import com.restaurant.management.order.domain.model.Order;
import com.restaurant.management.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 订单仓储实现（基础设施层）
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    
    private final OrderJpaRepository jpaRepository;
    
    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }
    
    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return jpaRepository.findByOrderNo(orderNo);
    }
    
    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id);
    }
}


package com.company.ecommerce.order.infrastructure.persistence;

import com.company.ecommerce.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 订单JPA仓储
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNo(String orderNo);
}


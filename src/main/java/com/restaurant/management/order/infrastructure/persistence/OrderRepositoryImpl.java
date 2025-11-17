package com.restaurant.management.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.management.order.domain.model.Order;
import com.restaurant.management.order.domain.model.OrderItem;
import com.restaurant.management.order.domain.repository.OrderRepository;
import com.restaurant.management.order.infrastructure.mapper.OrderItemMapper;
import com.restaurant.management.order.infrastructure.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 订单仓储实现（MyBatis-Plus）
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    
    @Override
    @Transactional
    public Order save(Order order) {
        if (order.getId() == null) {
            orderMapper.insert(order);
        } else {
            orderMapper.updateById(order);
            orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, order.getId()));
        }
        persistOrderItems(order);
        return attachItems(order);
    }
    
    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
        );
        return Optional.ofNullable(attachItems(order));
    }
    
    @Override
    public Optional<Order> findById(Long id) {
        Order order = orderMapper.selectById(id);
        return Optional.ofNullable(attachItems(order));
    }
    
    private void persistOrderItems(Order order) {
        if (order.getItems() == null) {
            return;
        }
        for (OrderItem item : order.getItems()) {
            item.setOrderId(order.getId());
            item.setOrder(order);
            orderItemMapper.insert(item);
        }
    }
    
    private Order attachItems(Order order) {
        if (order == null) {
            return null;
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
        );
        items.forEach(item -> item.setOrder(order));
        order.setItems(items);
        return order;
    }
}

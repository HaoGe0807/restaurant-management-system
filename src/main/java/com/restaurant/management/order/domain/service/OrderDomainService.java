package com.restaurant.management.order.domain.service;

import com.restaurant.management.order.domain.model.Order;
import com.restaurant.management.order.domain.model.OrderItem;
import com.restaurant.management.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单领域服务
 * 封装订单相关的业务逻辑，包括持久化操作
 * 应用层应该调用领域服务，而不是直接调用Repository
 */
@Service
@RequiredArgsConstructor
public class OrderDomainService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 创建订单
     * 封装了订单创建的完整业务逻辑，包括：
     * 1. 生成订单号
     * 2. 创建订单聚合
     * 3. 持久化订单
     */
    public Order createOrder(Long userId, List<OrderItem> items) {
        // 生成订单号（业务逻辑）
        String orderNo = generateOrderNo();
        
        // 创建订单聚合（领域模型）
        Order order = Order.create(orderNo, userId, items);
        
        // 持久化订单（由领域服务决定如何保存）
        return orderRepository.save(order);
    }
    
    /**
     * 支付订单
     * 封装支付业务逻辑
     */
    public Order payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        // 调用聚合根的业务方法
        order.pay();
        
        // 持久化状态变更
        return orderRepository.save(order);
    }
    
    /**
     * 取消订单
     * 封装取消业务逻辑
     */
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        // 调用聚合根的业务方法
        order.cancel();
        
        // 持久化状态变更
        return orderRepository.save(order);
    }
    
    /**
     * 根据ID查询订单
     */
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }
    
    /**
     * 根据订单号查询订单
     */
    public Order getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }
    
    /**
     * 生成订单号
     * 这是领域逻辑，应该在领域层
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }
}


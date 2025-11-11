package com.company.ecommerce.order.application;

import com.company.ecommerce.order.application.command.CreateOrderCommand;
import com.company.ecommerce.order.domain.model.Order;
import com.company.ecommerce.order.domain.model.OrderItem;
import com.company.ecommerce.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单应用服务
 * 负责协调领域对象，处理事务边界
 */
@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        // 转换命令为领域对象
        List<OrderItem> items = command.getItems().stream()
                .map(item -> OrderItem.create(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .collect(Collectors.toList());
        
        // 生成订单号
        String orderNo = generateOrderNo();
        
        // 创建订单聚合
        Order order = Order.create(orderNo, command.getUserId(), items);
        
        // 保存订单
        return orderRepository.save(order);
    }
    
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }
}


package com.restaurant.management.order.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.AggregateRoot;
import com.restaurant.management.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单聚合根
 */
@Getter
@Setter
@TableName("orders")
public class Order extends BaseEntity implements AggregateRoot {
    
    private String orderNo;
    
    private Long userId;
    
    private OrderStatus status;
    
    private BigDecimal totalAmount;
    
    @TableField(exist = false)
    private List<OrderItem> items = new ArrayList<>();
    
    /**
     * 创建订单
     */
    public static Order create(String orderNo, Long userId, List<OrderItem> items) {
        Order order = new Order();
        order.orderNo = orderNo;
        order.userId = userId;
        order.status = OrderStatus.CREATED;
        order.items = items;
        if (order.items != null) {
            order.items.forEach(item -> item.setOrder(order));
        }
        order.calculateTotalAmount();
        return order;
    }
    
    /**
     * 计算总金额
     */
    private void calculateTotalAmount() {
        if (items == null || items.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            return;
        }
        this.totalAmount = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 支付订单
     */
    public void pay() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("订单状态不正确，无法支付");
        }
        this.status = OrderStatus.PAID;
    }
    
    /**
     * 取消订单
     */
    public void cancel() {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("订单已发货或已完成，无法取消");
        }
        this.status = OrderStatus.CANCELLED;
    }
}


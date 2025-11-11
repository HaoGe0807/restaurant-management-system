package com.restaurant.management.order.api;

import com.restaurant.management.order.api.dto.CreateOrderRequest;
import com.restaurant.management.order.api.dto.OrderResponse;
import com.restaurant.management.order.application.OrderApplicationService;
import com.restaurant.management.order.application.command.CreateOrderCommand;
import com.restaurant.management.order.domain.model.Order;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderApplicationService orderApplicationService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = convertToCommand(request);
        Order order = orderApplicationService.createOrder(command);
        return convertToResponse(order);
    }
    
    private CreateOrderCommand convertToCommand(CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand();
        command.setUserId(request.getUserId());
        command.setItems(request.getItems().stream()
                .map(item -> {
                    CreateOrderCommand.OrderItemCommand cmd = new CreateOrderCommand.OrderItemCommand();
                    cmd.setProductId(item.getProductId());
                    cmd.setProductName(item.getProductName());
                    cmd.setQuantity(item.getQuantity());
                    cmd.setUnitPrice(item.getUnitPrice());
                    return cmd;
                })
                .toList());
        return command;
    }
    
    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus().name());
        response.setTotalAmount(order.getTotalAmount());
        response.setItems(order.getItems().stream()
                .map(item -> {
                    OrderResponse.OrderItemResponse itemResp = new OrderResponse.OrderItemResponse();
                    itemResp.setProductId(item.getProductId());
                    itemResp.setProductName(item.getProductName());
                    itemResp.setQuantity(item.getQuantity());
                    itemResp.setUnitPrice(item.getUnitPrice());
                    itemResp.setSubTotal(item.getSubTotal());
                    return itemResp;
                })
                .toList());
        response.setCreateTime(order.getCreateTime());
        return response;
    }
}


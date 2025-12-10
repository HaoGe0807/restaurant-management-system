package com.restaurant.management.order.api;

import com.restaurant.management.order.api.dto.CreateOrderRequest;
import com.restaurant.management.order.api.dto.OrderResponse;
import com.restaurant.management.order.application.OrderApplicationService;
import com.restaurant.management.order.application.command.CreateOrderCommand;
import com.restaurant.management.order.domain.model.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Tag(name = "订单管理", description = "订单相关的 API，包括创建订单等操作")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderApplicationService orderApplicationService;
    
    /**
     * 创建订单
     */
    @Operation(summary = "创建订单", description = "创建新的订单，会自动预留库存")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误或库存不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
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
                    cmd.setSkuId(item.getSkuId());
                    cmd.setSkuName(item.getSkuName());
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
                    itemResp.setSkuId(item.getSkuId());
                    itemResp.setSkuName(item.getSkuName());
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


package com.restaurant.management.order.application;

import com.restaurant.management.common.exception.DomainException;
import com.restaurant.management.inventory.domain.model.Inventory;
import com.restaurant.management.inventory.domain.service.InventoryDomainService;
import com.restaurant.management.order.application.command.CreateOrderCommand;
import com.restaurant.management.order.domain.model.Order;
import com.restaurant.management.order.domain.model.OrderItem;
import com.restaurant.management.order.domain.service.OrderDomainService;
import com.restaurant.management.product.domain.model.ProductSku;
import com.restaurant.management.product.domain.model.ProductSpu;
import com.restaurant.management.product.domain.model.ProductStatus;
import com.restaurant.management.product.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单应用服务
 * 职责：
 * 1. 协调领域服务（不直接调用Repository）
 * 2. 处理事务边界
 * 3. DTO与领域对象的转换
 * 4. 跨聚合的编排（强一致性场景）
 * 
 * 规则：
 * - 强一致性：应用层编排，直接调用领域服务进行验证
 * - 最终一致性：使用领域事件，由事件处理器异步处理
 */
@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    
    private final OrderDomainService orderDomainService;
    private final ProductDomainService productDomainService;  // 用于验证商品
    private final InventoryDomainService inventoryDomainService;  // 用于验证库存
    
    /**
     * 创建订单
     * 强一致性场景：需要验证商品和库存，必须同步验证
     * 
     * 应用层编排：
     * 1. 验证商品（存在性、状态、价格）
     * 2. 验证库存（可用数量）
     * 3. 预留库存
     * 4. 创建订单
     */
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        // 1. 验证商品和库存（强一致性，必须同步验证）
        Map<String, ProductSku> skuMap = validateProductsAndInventory(command);
        
        // 2. 预留库存（强一致性，必须同步预留）
        reserveInventory(command);
        
        // 3. 转换命令为领域对象（这是应用层的职责：适配外部输入）
        List<OrderItem> items = command.getItems().stream()
                .map(item -> {
                    ProductSku sku = skuMap.get(item.getSkuId());
                    return OrderItem.create(
                            sku.getSpuId(),
                            sku.getSkuId(),
                            sku.getSkuName(),
                            item.getQuantity(),
                            sku.getPrice()
                    );
                })
                .collect(Collectors.toList());
        
        // 4. 创建订单（调用领域服务）
        return orderDomainService.createOrder(command.getUserId(), items);
    }
    
    /**
     * 验证商品和库存
     * 强一致性：必须同步验证，确保数据一致性
     */
    private Map<String, ProductSku> validateProductsAndInventory(CreateOrderCommand command) {
        Map<String, ProductSku> skuCache = new HashMap<>();
        Map<String, ProductSpu> spuCache = new HashMap<>();
        for (CreateOrderCommand.OrderItemCommand item : command.getItems()) {
            // 验证商品 SKU
            ProductSku productSku = skuCache.computeIfAbsent(item.getSkuId(),
                    id -> productDomainService.getProductSku(id));
            
            // 验证商品 SPU 状态（通过 SKU 的 spuId 获取 SPU）
            ProductSpu productSpu = spuCache.computeIfAbsent(productSku.getSpuId(),
                    id -> productDomainService.getProductSpu(id));
            
            // 验证商品状态
            if (productSpu.getStatus() != ProductStatus.ACTIVE) {
                throw new DomainException("PRODUCT_INACTIVE", 
                        "SKU[" + productSku.getSkuName() + "]所属商品已下架，无法下单");
            }
            
            // 验证商品价格
            if (productSku.getPrice().compareTo(item.getUnitPrice()) != 0) {
                throw new DomainException("PRODUCT_PRICE_CHANGED", 
                        "SKU[" + productSku.getSkuName() + "]价格已变更，请刷新后重试");
            }
            
            // 验证库存
            Inventory inventory = inventoryDomainService.getInventoryBySkuId(item.getSkuId());
            
            // 验证库存数量
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new DomainException("INVENTORY_INSUFFICIENT", 
                        "SKU[" + productSku.getSkuName() + "]库存不足，当前可用库存：" + 
                        inventory.getAvailableQuantity());
            }
        }
        return skuCache;
    }
    
    /**
     * 预留库存
     * 强一致性：必须同步预留，确保库存被锁定
     */
    private void reserveInventory(CreateOrderCommand command) {
        for (CreateOrderCommand.OrderItemCommand item : command.getItems()) {
            // 预留库存（强一致性，同步操作）
            inventoryDomainService.reserveInventory(item.getSkuId(), item.getQuantity());
        }
    }
    
    /**
     * 支付订单
     */
    @Transactional
    public Order payOrder(Long orderId) {
        return orderDomainService.payOrder(orderId);
    }
    
    /**
     * 取消订单
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        return orderDomainService.cancelOrder(orderId);
    }
    
    /**
     * 查询订单
     */
    public Order getOrder(Long orderId) {
        return orderDomainService.getOrder(orderId);
    }
}


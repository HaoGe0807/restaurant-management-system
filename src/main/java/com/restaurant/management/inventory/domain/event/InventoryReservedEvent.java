package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import lombok.Getter;

/**
 * 库存预留事件
 * 当库存被预留时发布此事件
 */
@Getter
public class InventoryReservedEvent extends DomainEvent {
    
    private final String skuId;
    private final String warehouseId;
    private final Integer quantity;
    private final String orderId;
    
    public InventoryReservedEvent(String skuId, String warehouseId, Integer quantity, String orderId) {
        super();
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.orderId = orderId;
    }
}
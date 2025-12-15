package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import lombok.Getter;

/**
 * 库存释放事件
 * 当预留库存被释放时发布此事件
 */
@Getter
public class InventoryReleasedEvent extends DomainEvent {
    
    private final String skuId;
    private final String warehouseId;
    private final Integer quantity;
    private final String orderId;
    
    public InventoryReleasedEvent(String skuId, String warehouseId, Integer quantity, String orderId) {
        super();
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.orderId = orderId;
    }
}
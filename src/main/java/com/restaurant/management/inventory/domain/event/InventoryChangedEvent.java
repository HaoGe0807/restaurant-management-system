package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import lombok.Getter;

/**
 * 库存变更事件
 * 当库存数量发生变化时发布此事件
 */
@Getter
public class InventoryChangedEvent extends DomainEvent {
    
    private final String skuId;
    private final String warehouseId;
    private final Integer changeQuantity;
    private final Integer currentQuantity;
    private final String reason;
    
    public InventoryChangedEvent(String skuId, String warehouseId, Integer changeQuantity, 
                               Integer currentQuantity, String reason) {
        super();
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.changeQuantity = changeQuantity;
        this.currentQuantity = currentQuantity;
        this.reason = reason;
    }
}
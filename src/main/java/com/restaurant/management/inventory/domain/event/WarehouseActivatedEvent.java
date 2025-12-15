package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import lombok.Getter;

/**
 * 仓库激活事件
 */
@Getter
public class WarehouseActivatedEvent extends DomainEvent {
    
    private final String warehouseId;
    private final String warehouseName;
    
    public WarehouseActivatedEvent(String warehouseId, String warehouseName) {
        super();
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
    }
}
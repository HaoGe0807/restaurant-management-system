package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import lombok.Getter;

/**
 * 仓库停用事件
 */
@Getter
public class WarehouseDeactivatedEvent extends DomainEvent {
    
    private final String warehouseId;
    private final String warehouseName;
    private final String reason;
    
    public WarehouseDeactivatedEvent(String warehouseId, String warehouseName, String reason) {
        super();
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.reason = reason;
    }
}
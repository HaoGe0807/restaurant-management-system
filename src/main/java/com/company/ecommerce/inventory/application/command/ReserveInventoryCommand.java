package com.company.ecommerce.inventory.application.command;

import lombok.Data;

/**
 * 预留库存命令
 */
@Data
public class ReserveInventoryCommand {
    
    private Long productId;
    private Integer quantity;
}


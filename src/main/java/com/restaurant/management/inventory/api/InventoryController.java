package com.restaurant.management.inventory.api;

import com.restaurant.management.inventory.api.dto.InventoryResponse;
import com.restaurant.management.inventory.api.dto.ReserveInventoryRequest;
import com.restaurant.management.inventory.application.InventoryApplicationService;
import com.restaurant.management.inventory.application.command.ReserveInventoryCommand;
import com.restaurant.management.inventory.domain.model.Inventory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 库存控制器
 */
@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryApplicationService inventoryApplicationService;
    
    /**
     * 预留库存
     */
    @PostMapping("/reserve")
    public InventoryResponse reserveInventory(@Valid @RequestBody ReserveInventoryRequest request) {
        ReserveInventoryCommand command = convertToCommand(request);
        Inventory inventory = inventoryApplicationService.reserveInventory(command);
        return convertToResponse(inventory);
    }
    
    /**
     * 根据商品ID查询库存
     */
    @GetMapping("/product/{productId}")
    public InventoryResponse getInventoryByProductId(@PathVariable String productId) {
        Inventory inventory = inventoryApplicationService.getInventoryByProductId(productId);
        return convertToResponse(inventory);
    }
    
    private ReserveInventoryCommand convertToCommand(ReserveInventoryRequest request) {
        ReserveInventoryCommand command = new ReserveInventoryCommand();
        command.setProductId(request.getProductId());
        command.setQuantity(request.getQuantity());
        return command;
    }
    
    private InventoryResponse convertToResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId());
        response.setProductId(inventory.getProductId());
        response.setAvailableQuantity(inventory.getAvailableQuantity());
        response.setReservedQuantity(inventory.getReservedQuantity());
        response.setTotalQuantity(inventory.getTotalQuantity());
        response.setCreateTime(inventory.getCreateTime());
        return response;
    }
}


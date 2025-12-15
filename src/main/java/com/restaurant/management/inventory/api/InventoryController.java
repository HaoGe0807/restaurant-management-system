package com.restaurant.management.inventory.api;

import com.restaurant.management.inventory.api.dto.InventoryResponse;
import com.restaurant.management.inventory.api.dto.ReserveInventoryRequest;
import com.restaurant.management.inventory.application.InventoryApplicationService;
import com.restaurant.management.inventory.application.command.ReserveInventoryCommand;
import com.restaurant.management.inventory.domain.model.Inventory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 库存控制器
 */
@Tag(name = "库存管理", description = "库存相关的 API，包括预留库存、查询库存等操作")
@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryApplicationService inventoryApplicationService;
    
    /**
     * 预留库存
     */
    @Operation(summary = "预留库存", description = "为指定 SKU 预留库存")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "预留成功",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误或库存不足"),
            @ApiResponse(responseCode = "404", description = "库存不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/reserve")
    public InventoryResponse reserveInventory(@Valid @RequestBody ReserveInventoryRequest request) {
        ReserveInventoryCommand command = convertToCommand(request);
        Inventory inventory = inventoryApplicationService.reserveInventory(command);
        return convertToResponse(inventory);
    }
    
    /**
     * 根据 SKU 查询库存
     */
    @Operation(summary = "根据 SKU 查询库存", description = "根据 SKU ID 查询库存详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "库存不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/sku/{skuId}")
    public InventoryResponse getInventoryBySkuId(
            @Parameter(description = "SKU ID", required = true, example = "SKU001")
            @PathVariable String skuId) {
        Inventory inventory = inventoryApplicationService.getInventoryBySkuId(skuId);
        return convertToResponse(inventory);
    }
    
    private ReserveInventoryCommand convertToCommand(ReserveInventoryRequest request) {
        ReserveInventoryCommand command = new ReserveInventoryCommand();
        command.setSkuId(request.getSkuId());
        command.setQuantity(request.getQuantity());
        command.setOrderId(request.getOrderId());
        return command;
    }
    
    private InventoryResponse convertToResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId());
        response.setSkuId(inventory.getSkuId());
        response.setAvailableQuantity(inventory.getAvailableQuantity());
        response.setReservedQuantity(inventory.getReservedQuantity());
        response.setTotalQuantity(inventory.getTotalQuantity());
        response.setCreateTime(inventory.getCreateTime());
        return response;
    }
}


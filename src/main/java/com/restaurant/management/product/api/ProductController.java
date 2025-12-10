package com.restaurant.management.product.api;

import com.restaurant.management.product.api.dto.SaveProductRequest;
import com.restaurant.management.product.api.dto.ProductResponse;
import com.restaurant.management.product.application.ProductApplicationService;
import com.restaurant.management.product.application.command.SaveProductCommand;
import com.restaurant.management.product.domain.model.ProductSku;
import com.restaurant.management.product.domain.model.ProductSpu;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 商品控制器
 * @date 2025-11-18 14:35
 * @author wuyuhao.29
 */
@Tag(name = "商品管理", description = "商品相关的 API，包括创建、查询、更新商品等操作")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    /**
     * 商品应用服务，用于处理商品相关的业务逻辑。
     */
    private final ProductApplicationService productApplicationService;
    
    /**
     * 创建商品
     */
    @Operation(summary = "创建商品", description = "创建新的商品 SPU 和 SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping(value = "/createProduct")
    public ProductResponse createProduct(@Valid @RequestBody SaveProductRequest request) {
        SaveProductCommand command = convertToCommand(request);
        ProductSpu product = productApplicationService.createProduct(command);
        return convertToResponse(product);
    }
    
    /**
     * 根据ID查询商品
     */
    @Operation(summary = "根据ID查询商品", description = "根据 SPU ID 查询商品详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "商品不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/getProduct/{spuId}")
    public ProductResponse getProduct(
            @Parameter(description = "商品 SPU ID", required = true, example = "SPU001")
            @PathVariable String spuId) {
        ProductSpu product = productApplicationService.getProduct(spuId);
        return convertToResponse(product);
    }

    /**
     * 更新商品
     */
    @Operation(summary = "更新商品", description = "更新已存在的商品信息，需要提供 spuId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误（spuId 不能为空）"),
            @ApiResponse(responseCode = "404", description = "商品不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PutMapping(value = "/updateProduct")
    public ProductResponse updateProduct(@Valid @RequestBody SaveProductRequest request) {
        if (request.getSpuId() == null || request.getSpuId().trim().isEmpty()) {
            throw new IllegalArgumentException("SPU ID不能为空");
        }
        SaveProductCommand command = convertToCommand(request);
        ProductSpu product = productApplicationService.updateProduct(request.getSpuId(), command);
        return convertToResponse(product);
    }

    /**
     * 查询商品列表
     */
    @Operation(summary = "查询商品列表", description = "分页查询商品列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/list")
    public List<ProductResponse> getProductList(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {
        List<ProductSpu> products = productApplicationService.getProductList(pageNum, pageSize);
        return products.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private SaveProductCommand convertToCommand(SaveProductRequest request) {
        if (request.getSkus() == null || request.getSkus().isEmpty()) {
            throw new IllegalArgumentException("至少需要一个SKU");
        }
        SaveProductCommand command = new SaveProductCommand();
        command.setSpuName(request.getSpuName());
        command.setDescription(request.getDescription());

        List<SaveProductCommand.SkuCommand> skuCommands = new ArrayList<>();
        for (SaveProductRequest.SkuRequest sku : request.getSkus()) {
            SaveProductCommand.SkuCommand skuCommand = new SaveProductCommand.SkuCommand();
            skuCommand.setSkuName(sku.getSkuName());
            skuCommand.setPrice(sku.getPrice());
            skuCommand.setInitialQuantity(sku.getInitialQuantity());
            skuCommands.add(skuCommand);
        }
        command.setSkus(skuCommands);
        return command;
    }
    
    private ProductResponse convertToResponse(ProductSpu product) {
        ProductResponse response = new ProductResponse();
        response.setSpuId(product.getSpuId());
        response.setSpuName(product.getSpuName());
        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus().name());
        response.setCreateTime(product.getCreateTime());

        List<ProductResponse.SkuResponse> skuResponses = new ArrayList<>();
        if (product.getSkus() != null) {
            for (ProductSku sku : product.getSkus()) {
                skuResponses.add(convertSku(sku));
            }
        }
        response.setSkus(skuResponses);
        return response;
    }

    private ProductResponse.SkuResponse convertSku(ProductSku sku) {
        ProductResponse.SkuResponse response = new ProductResponse.SkuResponse();
        response.setSkuId(sku.getSkuId());
        response.setSkuName(sku.getSkuName());
        response.setPrice(sku.getPrice());
        return response;
    }
}


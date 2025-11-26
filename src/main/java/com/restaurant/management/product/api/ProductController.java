package com.restaurant.management.product.api;

import com.restaurant.management.product.api.dto.SaveProductRequest;
import com.restaurant.management.product.api.dto.ProductResponse;
import com.restaurant.management.product.application.ProductApplicationService;
import com.restaurant.management.product.application.command.CreateProductCommand;
import com.restaurant.management.product.domain.model.ProductSku;
import com.restaurant.management.product.domain.model.ProductSpu;
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
    @PostMapping(value = "/createProduct")
    public ProductResponse createProduct(@Valid @RequestBody SaveProductRequest request) {
        CreateProductCommand command = convertToCommand(request);
        ProductSpu product = productApplicationService.createProduct(command);
        return convertToResponse(product);
    }
    
    /**
     * 根据ID查询商品
     */
    @GetMapping("/getProduct/{spuId}")
    public ProductResponse getProduct(@PathVariable String spuId) {
        ProductSpu product = productApplicationService.getProduct(spuId);
        return convertToResponse(product);
    }

    /**
     * 更新商品
     */
    @PutMapping(value = "/updateProduct")
    public ProductResponse updateProduct(@Valid @RequestBody SaveProductRequest request) {
        if (request.getSpuId() == null || request.getSpuId().trim().isEmpty()) {
            throw new IllegalArgumentException("SPU ID不能为空");
        }
        CreateProductCommand command = convertToCommand(request);
        ProductSpu product = productApplicationService.updateProduct(request.getSpuId(), command);
        return convertToResponse(product);
    }

    /**
     * 查询商品列表
     */
    @GetMapping("/list")
    public List<ProductResponse> getProductList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<ProductSpu> products = productApplicationService.getProductList(pageNum, pageSize);
        return products.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private CreateProductCommand convertToCommand(SaveProductRequest request) {
        if (request.getSkus() == null || request.getSkus().isEmpty()) {
            throw new IllegalArgumentException("至少需要一个SKU");
        }
        CreateProductCommand command = new CreateProductCommand();
        command.setSpuName(request.getSpuName());
        command.setDescription(request.getDescription());

        List<CreateProductCommand.SkuCommand> skuCommands = new ArrayList<>();
        for (SaveProductRequest.SkuRequest sku : request.getSkus()) {
            CreateProductCommand.SkuCommand skuCommand = new CreateProductCommand.SkuCommand();
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


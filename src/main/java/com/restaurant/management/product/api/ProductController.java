package com.restaurant.management.product.api;

import com.restaurant.management.product.api.dto.CreateProductRequest;
import com.restaurant.management.product.api.dto.ProductResponse;
import com.restaurant.management.product.application.ProductApplicationService;
import com.restaurant.management.product.application.command.CreateProductCommand;
import com.restaurant.management.product.domain.model.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductApplicationService productApplicationService;
    
    /**
     * 创建商品
     */
    @PostMapping
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductCommand command = convertToCommand(request);
        Product product = productApplicationService.createProduct(command);
        return convertToResponse(product);
    }
    
    /**
     * 根据ID查询商品
     */
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        Product product = productApplicationService.getProduct(id);
        return convertToResponse(product);
    }
    
    private CreateProductCommand convertToCommand(CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand();
        command.setProductName(request.getProductName());
        command.setDescription(request.getDescription());
        command.setPrice(request.getPrice());
        command.setCategoryId(request.getCategoryId());
        return command;
    }
    
    private ProductResponse convertToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setProductName(product.getProductName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStatus(product.getStatus().name());
        response.setCategoryId(product.getCategoryId());
        response.setCreateTime(product.getCreateTime());
        return response;
    }
}


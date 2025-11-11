package com.company.ecommerce.product.domain.model;

/**
 * 商品状态值对象
 */
public enum ProductStatus {
    ACTIVE("上架"),
    INACTIVE("下架");
    
    private final String description;
    
    ProductStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}


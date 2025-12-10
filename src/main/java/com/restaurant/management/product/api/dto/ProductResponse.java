package com.restaurant.management.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品响应DTO（SPU + SKU）
 */
@Schema(description = "商品响应信息")
@Data
public class ProductResponse {

    @Schema(description = "商品 SPU ID", example = "SPU001")
    private String spuId;
    
    @Schema(description = "商品 SPU 名称", example = "iPhone 15 Pro")
    private String spuName;
    
    @Schema(description = "商品描述", example = "最新款 iPhone，性能强劲")
    private String description;
    
    @Schema(description = "商品状态", example = "ACTIVE")
    private String status;
    
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;
    
    @Schema(description = "SKU 列表")
    private List<SkuResponse> skus;

    @Schema(description = "SKU 信息")
    @Data
    public static class SkuResponse {
        @Schema(description = "SKU ID", example = "SKU001")
        private String skuId;
        
        @Schema(description = "SKU 名称", example = "iPhone 15 Pro 256GB 深空黑色")
        private String skuName;
        
        @Schema(description = "价格", example = "8999.00")
        private BigDecimal price;
        
        @Schema(description = "SKU 属性", example = "颜色:深空黑色,存储:256GB")
        private String attributes;
    }
}


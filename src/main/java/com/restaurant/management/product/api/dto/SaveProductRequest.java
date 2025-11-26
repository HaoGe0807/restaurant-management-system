package com.restaurant.management.product.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建商品请求DTO（SPU + SKU）
 */
@Data
public class SaveProductRequest {

    private String spuId;

    @NotBlank(message = "SPU名称不能为空")
    private String spuName;

    private String description;

    @NotNull(message = "状态不能为空")
    private String status;

    @NotEmpty(message = "至少需要一个SKU")
    @Valid
    private List<SkuRequest> skus;

    @Data
    public static class SkuRequest {
        @NotBlank(message = "SKU名称不能为空")
        private String skuName;

        @NotNull(message = "价格不能为空")
        @Positive(message = "价格必须大于0")
        private BigDecimal price;

        private int initialQuantity;
    }
}


package com.restaurant.management.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
@Schema(description = "创建/更新商品请求")
@Data
public class SaveProductRequest {

    @Schema(description = "商品 SPU ID，更新时必填，创建时不需要", example = "SPU001")
    private String spuId;

    @Schema(description = "商品 SPU 名称", requiredMode = RequiredMode.REQUIRED, example = "iPhone 15 Pro")
    @NotBlank(message = "SPU名称不能为空")
    private String spuName;

    @Schema(description = "商品描述", example = "最新款 iPhone，性能强劲")
    private String description;

    @Schema(description = "商品状态", requiredMode = RequiredMode.REQUIRED, example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    @NotNull(message = "状态不能为空")
    private String status;

    @Schema(description = "SKU 列表，至少需要一个", requiredMode = RequiredMode.REQUIRED)
    @NotEmpty(message = "至少需要一个SKU")
    @Valid
    private List<SkuRequest> skus;

    @Schema(description = "SKU 信息")
    @Data
    public static class SkuRequest {
        @Schema(description = "SKU 名称", requiredMode = RequiredMode.REQUIRED, example = "iPhone 15 Pro 256GB 深空黑色")
        @NotBlank(message = "SKU名称不能为空")
        private String skuName;

        @Schema(description = "价格", requiredMode = RequiredMode.REQUIRED, example = "8999.00")
        @NotNull(message = "价格不能为空")
        @Positive(message = "价格必须大于0")
        private BigDecimal price;

        @Schema(description = "初始库存数量", example = "100")
        private int initialQuantity;
    }
}


package com.restaurant.management.payment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "支付成功回调请求")
public class SuccessCallbackRequest {

    @NotBlank
    @Schema(description = "渠道交易号", example = "WX20241211001")
    private String channelTradeNo;

    @NotNull
    @Schema(description = "回调金额", example = "199.99")
    private BigDecimal amount;
}


package com.restaurant.management.payment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "关闭支付单请求")
public class ClosePaymentRequest {

    @NotBlank
    @Schema(description = "关闭原因", example = "订单取消或超时")
    private String reason;
}


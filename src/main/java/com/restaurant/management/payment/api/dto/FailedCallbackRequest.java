package com.restaurant.management.payment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "支付失败回调请求")
public class FailedCallbackRequest {

    @NotBlank
    @Schema(description = "失败原因", example = "USERPAYING 或 BANK_ERROR")
    private String reason;
}


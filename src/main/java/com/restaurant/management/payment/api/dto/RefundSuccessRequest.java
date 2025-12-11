package com.restaurant.management.payment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "退款成功请求")
public class RefundSuccessRequest {

    @NotBlank
    @Schema(description = "退款原因", example = "用户取消或售后退款")
    private String reason;
}


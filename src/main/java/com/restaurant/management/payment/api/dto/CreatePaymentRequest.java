package com.restaurant.management.payment.api.dto;

import com.restaurant.management.payment.domain.model.PaymentChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "创建支付单请求")
public class CreatePaymentRequest {

    @NotBlank
    @Schema(description = "订单号", example = "ORD20241211001")
    private String orderNo;

    @NotNull
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull
    @Schema(description = "支付金额", example = "199.99")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "支付渠道", example = "WECHAT")
    private PaymentChannel channel;

    @Schema(description = "过期时间，不传默认30分钟后")
    private LocalDateTime expireTime;
}


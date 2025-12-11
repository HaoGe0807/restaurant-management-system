package com.restaurant.management.payment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "渠道下单成功请求")
public class PendingRequest {

    @NotBlank
    @Schema(description = "预支付凭证，如微信 prepay_id 或支付宝 orderString")
    private String credential;

    @Schema(description = "渠道交易号，如微信 transaction_id")
    private String channelTradeNo;
}


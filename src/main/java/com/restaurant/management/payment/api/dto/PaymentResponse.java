package com.restaurant.management.payment.api.dto;

import com.restaurant.management.payment.domain.model.PaymentChannel;
import com.restaurant.management.payment.domain.model.PaymentOrder;
import com.restaurant.management.payment.domain.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "支付单响应")
public class PaymentResponse {

    @Schema(description = "支付单号")
    private String paymentNo;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "支付渠道")
    private PaymentChannel channel;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "状态")
    private PaymentStatus status;

    @Schema(description = "预支付凭证")
    private String credential;

    @Schema(description = "渠道交易号")
    private String channelTradeNo;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "原因（失败/关闭/退款原因）")
    private String reason;

    public static PaymentResponse from(PaymentOrder paymentOrder) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentNo(paymentOrder.getPaymentNo());
        response.setOrderNo(paymentOrder.getOrderNo());
        response.setUserId(paymentOrder.getUserId());
        response.setChannel(paymentOrder.getChannel());
        response.setAmount(paymentOrder.getAmount());
        response.setStatus(paymentOrder.getStatus());
        response.setCredential(paymentOrder.getCredential());
        response.setChannelTradeNo(paymentOrder.getChannelTradeNo());
        response.setExpireTime(paymentOrder.getExpireTime());
        response.setReason(paymentOrder.getReason());
        return response;
    }
}


package com.restaurant.management.payment.application;

import com.restaurant.management.payment.application.command.CreatePaymentCommand;
import com.restaurant.management.payment.domain.model.PaymentChannel;
import com.restaurant.management.payment.domain.model.PaymentOrder;
import com.restaurant.management.payment.domain.service.PaymentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付应用服务
 * 负责编排领域服务，供接口层调用
 */
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentDomainService paymentDomainService;

    /**
     * 创建支付单
     */
    @Transactional
    public PaymentOrder createPayment(CreatePaymentCommand command) {
        return paymentDomainService.createPayment(
                command.getOrderNo(),
                command.getUserId(),
                command.getAmount(),
                command.getChannel(),
                defaultExpireTime(command.getExpireTime(), command.getChannel()));
    }

    /**
     * 渠道下单成功（返回预支付信息）
     */
    @Transactional
    public PaymentOrder markPending(String paymentNo, String credential, String channelTradeNo) {
        return paymentDomainService.markPending(paymentNo, credential, channelTradeNo);
    }

    /**
     * 支付成功回调
     */
    @Transactional
    public PaymentOrder handleSuccessCallback(String paymentNo, String channelTradeNo, BigDecimal amount) {
        return paymentDomainService.onSuccess(paymentNo, channelTradeNo, amount);
    }

    /**
     * 支付失败回调
     */
    @Transactional
    public PaymentOrder handleFailedCallback(String paymentNo, String reason) {
        return paymentDomainService.onFailed(paymentNo, reason);
    }

    /**
     * 关闭支付单
     */
    @Transactional
    public PaymentOrder closePayment(String paymentNo, String reason) {
        return paymentDomainService.close(paymentNo, reason);
    }

    /**
     * 退款成功
     */
    @Transactional
    public PaymentOrder refundSuccess(String paymentNo, String reason) {
        return paymentDomainService.refundSuccess(paymentNo, reason);
    }

    private LocalDateTime defaultExpireTime(LocalDateTime expireTime, PaymentChannel channel) {
        // 默认 30 分钟过期，可按渠道调整
        return expireTime != null ? expireTime : LocalDateTime.now().plusMinutes(30);
    }
}


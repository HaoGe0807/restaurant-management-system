package com.restaurant.management.payment.domain.service;

import com.restaurant.management.payment.domain.model.PaymentChannel;
import com.restaurant.management.payment.domain.model.PaymentOrder;
import com.restaurant.management.payment.domain.model.PaymentStatus;
import com.restaurant.management.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付领域服务
 * 封装渠道无关的核心支付流程：创建、回调处理、关闭、退款等
 */
@Service
@RequiredArgsConstructor
public class PaymentDomainService {

    private final PaymentRepository paymentRepository;

    /**
     * 为订单创建支付单
     */
    public PaymentOrder createPayment(String orderNo, Long userId, BigDecimal amount,
                                      PaymentChannel channel, LocalDateTime expireTime) {
        PaymentOrder paymentOrder = PaymentOrder.create(orderNo, userId, amount, channel, expireTime);
        return paymentRepository.save(paymentOrder);
    }

    /**
     * 渠道下单成功，拿到凭证/渠道交易号，置为待支付
     */
    public PaymentOrder markPending(String paymentNo, String credential, String channelTradeNo) {
        PaymentOrder paymentOrder = loadPayment(paymentNo);
        paymentOrder.markPending(credential, channelTradeNo);
        return paymentRepository.save(paymentOrder);
    }

    /**
     * 支付成功回调
     */
    public PaymentOrder onSuccess(String paymentNo, String channelTradeNo, BigDecimal amount) {
        PaymentOrder paymentOrder = loadPayment(paymentNo);
        if (paymentOrder.isTerminal()) {
            return paymentOrder;
        }
        paymentOrder.assertAmount(amount);
        paymentOrder.markSuccess(channelTradeNo);
        return paymentRepository.save(paymentOrder);
    }

    /**
     * 支付失败/回调失败
     */
    public PaymentOrder onFailed(String paymentNo, String reason) {
        PaymentOrder paymentOrder = loadPayment(paymentNo);
        if (paymentOrder.getStatus() == PaymentStatus.SUCCESS) {
            return paymentOrder;
        }
        paymentOrder.markFailed(reason);
        return paymentRepository.save(paymentOrder);
    }

    /**
     * 关闭支付单（订单取消或超时）
     */
    public PaymentOrder close(String paymentNo, String reason) {
        PaymentOrder paymentOrder = loadPayment(paymentNo);
        if (paymentOrder.isTerminal()) {
            return paymentOrder;
        }
        paymentOrder.close(reason);
        return paymentRepository.save(paymentOrder);
    }

    /**
     * 退款成功
     */
    public PaymentOrder refundSuccess(String paymentNo, String refundReason) {
        PaymentOrder paymentOrder = loadPayment(paymentNo);
        paymentOrder.refundSuccess(refundReason);
        return paymentRepository.save(paymentOrder);
    }

    private PaymentOrder loadPayment(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("支付单不存在: " + paymentNo));
    }
}


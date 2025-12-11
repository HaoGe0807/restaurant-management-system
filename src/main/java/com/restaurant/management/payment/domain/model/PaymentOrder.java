package com.restaurant.management.payment.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.AggregateRoot;
import com.restaurant.management.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 支付单聚合根
 */
@Getter
@Setter
@TableName("payments")
public class PaymentOrder extends BaseEntity implements AggregateRoot {

    /**
     * 支付单号（业务号）
     */
    private String paymentNo;

    /**
     * 关联的业务订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付渠道
     */
    private PaymentChannel channel;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 渠道生成的预支付凭证（如微信 prepay_id、支付宝 orderString）
     */
    private String credential;

    /**
     * 渠道侧交易号（如微信 transaction_id、支付宝 trade_no）
     */
    private String channelTradeNo;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 失败原因或关闭原因
     */
    private String reason;

    /**
     * 创建支付单
     */
    public static PaymentOrder create(String orderNo, Long userId, BigDecimal amount, PaymentChannel channel, LocalDateTime expireTime) {
        PaymentOrder payment = new PaymentOrder();
        payment.paymentNo = generatePaymentNo();
        payment.orderNo = orderNo;
        payment.userId = userId;
        payment.amount = amount;
        payment.channel = channel;
        payment.expireTime = expireTime;
        payment.status = PaymentStatus.INIT;
        return payment;
    }

    /**
     * 标记为待支付（下单成功，已拿到支付凭证）
     */
    public void markPending(String credential, String channelTradeNo) {
        if (status != PaymentStatus.INIT && status != PaymentStatus.FAILED) {
            throw new IllegalStateException("当前状态不允许标记为待支付");
        }
        this.credential = credential;
        this.channelTradeNo = channelTradeNo;
        this.status = PaymentStatus.PENDING;
        this.reason = null;
    }

    /**
     * 支付成功
     */
    public void markSuccess(String channelTradeNo) {
        if (status == PaymentStatus.SUCCESS || status == PaymentStatus.REFUNDED) {
            return;
        }
        this.channelTradeNo = channelTradeNo;
        this.status = PaymentStatus.SUCCESS;
        this.reason = null;
    }

    /**
     * 支付失败
     */
    public void markFailed(String failReason) {
        if (status == PaymentStatus.SUCCESS || status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("成功/已退款订单无法标记失败");
        }
        this.status = PaymentStatus.FAILED;
        this.reason = failReason;
    }

    /**
     * 关闭支付单
     */
    public void close(String closeReason) {
        if (status == PaymentStatus.SUCCESS || status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("成功/已退款订单无法关闭");
        }
        this.status = PaymentStatus.CLOSED;
        this.reason = closeReason;
    }

    /**
     * 退款成功
     */
    public void refundSuccess(String refundReason) {
        if (status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("仅支付成功后才能退款");
        }
        this.status = PaymentStatus.REFUNDED;
        this.reason = refundReason;
    }

    /**
     * 辅助方法：生成支付单号
     */
    private static String generatePaymentNo() {
        // 简单示例，可替换为雪花算法等
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    /**
     * 校验回调金额与下单金额一致
     */
    public void assertAmount(BigDecimal callbackAmount) {
        if (callbackAmount == null || amount == null || amount.compareTo(callbackAmount) != 0) {
            throw new IllegalArgumentException("支付金额不一致");
        }
    }

    /**
     * 幂等判断：是否已终态
     */
    public boolean isTerminal() {
        return Objects.requireNonNull(status) == PaymentStatus.SUCCESS
                || status == PaymentStatus.CLOSED
                || status == PaymentStatus.REFUNDED;
    }
}


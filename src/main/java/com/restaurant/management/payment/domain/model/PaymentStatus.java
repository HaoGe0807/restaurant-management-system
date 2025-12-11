package com.restaurant.management.payment.domain.model;

/**
 * 支付状态
 */
public enum PaymentStatus {
    INIT("待下单"),
    PENDING("待支付/待回调"),
    SUCCESS("支付成功"),
    FAILED("支付失败"),
    CLOSED("已关闭"),
    REFUNDED("已退款");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


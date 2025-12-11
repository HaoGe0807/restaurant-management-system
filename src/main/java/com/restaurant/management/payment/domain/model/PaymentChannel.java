package com.restaurant.management.payment.domain.model;

/**
 * 支付渠道
 */
public enum PaymentChannel {
    WECHAT("微信支付"),
    ALIPAY("支付宝支付"),
    CASH("现金");

    private final String description;

    PaymentChannel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


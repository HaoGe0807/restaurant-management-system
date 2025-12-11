package com.restaurant.management.payment.domain.repository;

import com.restaurant.management.payment.domain.model.PaymentOrder;

import java.util.Optional;

/**
 * 支付仓储接口
 */
public interface PaymentRepository {

    PaymentOrder save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByPaymentNo(String paymentNo);

    Optional<PaymentOrder> findByOrderNo(String orderNo);
}


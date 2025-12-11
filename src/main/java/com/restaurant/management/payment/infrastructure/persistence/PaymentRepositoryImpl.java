package com.restaurant.management.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.management.payment.domain.model.PaymentOrder;
import com.restaurant.management.payment.domain.repository.PaymentRepository;
import com.restaurant.management.payment.infrastructure.mapper.PaymentOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentOrderMapper paymentOrderMapper;

    @Override
    public PaymentOrder save(PaymentOrder paymentOrder) {
        if (paymentOrder.getId() == null) {
            paymentOrderMapper.insert(paymentOrder);
        } else {
            paymentOrderMapper.updateById(paymentOrder);
        }
        return paymentOrder;
    }

    @Override
    public Optional<PaymentOrder> findByPaymentNo(String paymentNo) {
        PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getPaymentNo, paymentNo));
        return Optional.ofNullable(paymentOrder);
    }

    @Override
    public Optional<PaymentOrder> findByOrderNo(String orderNo) {
        PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
        return Optional.ofNullable(paymentOrder);
    }
}


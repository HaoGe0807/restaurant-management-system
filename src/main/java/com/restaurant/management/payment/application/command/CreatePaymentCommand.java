package com.restaurant.management.payment.application.command;

import com.restaurant.management.payment.domain.model.PaymentChannel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreatePaymentCommand {
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private PaymentChannel channel;
    private LocalDateTime expireTime;
}


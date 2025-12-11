package com.restaurant.management.payment.api;

import com.restaurant.management.payment.api.dto.*;
import com.restaurant.management.payment.application.PaymentApplicationService;
import com.restaurant.management.payment.application.command.CreatePaymentCommand;
import com.restaurant.management.payment.domain.model.PaymentOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "支付接口", description = "支付域：创建、渠道回调、关闭、退款")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    @PostMapping
    @Operation(summary = "创建支付单")
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        CreatePaymentCommand command = new CreatePaymentCommand();
        command.setOrderNo(request.getOrderNo());
        command.setUserId(request.getUserId());
        command.setAmount(request.getAmount());
        command.setChannel(request.getChannel());
        command.setExpireTime(request.getExpireTime());
        PaymentOrder paymentOrder = paymentApplicationService.createPayment(command);
        return PaymentResponse.from(paymentOrder);
    }

    @PostMapping("/{paymentNo}/pending")
    @Operation(summary = "渠道下单成功，标记为待支付并返回凭证")
    public PaymentResponse markPending(@PathVariable String paymentNo, @Valid @RequestBody PendingRequest request) {
        PaymentOrder paymentOrder = paymentApplicationService.markPending(paymentNo, request.getCredential(), request.getChannelTradeNo());
        return PaymentResponse.from(paymentOrder);
    }

    @PostMapping("/{paymentNo}/success")
    @Operation(summary = "支付成功回调")
    public PaymentResponse successCallback(@PathVariable String paymentNo, @Valid @RequestBody SuccessCallbackRequest request) {
        PaymentOrder paymentOrder = paymentApplicationService.handleSuccessCallback(
                paymentNo, request.getChannelTradeNo(), request.getAmount());
        return PaymentResponse.from(paymentOrder);
    }

    @PostMapping("/{paymentNo}/failed")
    @Operation(summary = "支付失败回调")
    public PaymentResponse failedCallback(@PathVariable String paymentNo, @Valid @RequestBody FailedCallbackRequest request) {
        PaymentOrder paymentOrder = paymentApplicationService.handleFailedCallback(paymentNo, request.getReason());
        return PaymentResponse.from(paymentOrder);
    }

    @PostMapping("/{paymentNo}/close")
    @Operation(summary = "关闭支付单")
    public PaymentResponse closePayment(@PathVariable String paymentNo, @Valid @RequestBody ClosePaymentRequest request) {
        PaymentOrder paymentOrder = paymentApplicationService.closePayment(paymentNo, request.getReason());
        return PaymentResponse.from(paymentOrder);
    }

    @PostMapping("/{paymentNo}/refund-success")
    @Operation(summary = "退款成功通知")
    public PaymentResponse refundSuccess(@PathVariable String paymentNo, @Valid @RequestBody RefundSuccessRequest request) {
        PaymentOrder paymentOrder = paymentApplicationService.refundSuccess(paymentNo, request.getReason());
        return PaymentResponse.from(paymentOrder);
    }
}


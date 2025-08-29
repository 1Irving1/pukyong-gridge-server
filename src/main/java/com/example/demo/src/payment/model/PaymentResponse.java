package com.example.demo.src.payment.model;

import com.example.demo.src.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long id;
    private Long userId;
    private String impUid;
    private String merchantUid;
    private BigDecimal amount;
    private Payment.PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    
    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.userId = payment.getUserId();
        this.impUid = payment.getImpUid();
        this.merchantUid = payment.getMerchantUid();
        this.amount = payment.getAmount();
        this.paymentStatus = payment.getPaymentStatus();
        this.createdAt = payment.getCreatedAt();
    }
}

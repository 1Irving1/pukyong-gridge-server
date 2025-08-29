package com.example.demo.src.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 결제 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    // 사용자 ID
    private Long userId;
    
    // 결제 금액
    private BigDecimal amount;
    
    // 가맹점 주문 ID (중복 방지용)
    private String merchantUid;
    
    // 결제 방법 (card, bank_transfer, mobile)
    private String paymentMethod;
    
    // PortOne 결제 ID (실제 결제 후 받은 ID)
    private String impUid;
}

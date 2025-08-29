package com.example.demo.src.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 검증 요청 DTO (PortOne 콜백용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationRequest {
    
    // PortOne 결제 ID
    private String impUid;
    
    // 가맹점 주문 ID
    private String merchantUid;
    
    // 결제 상태 (success, failed)
    private String status;
}

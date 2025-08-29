package com.example.demo.src.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 포트원 결제창 연동 응답 DTO
 * 결제창 호출에 필요한 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortOnePaymentResponse {

    // 결제창 호출 URL
    private String paymentUrl;

    // 결제창 호출에 필요한 파라미터들
    private String impUid;
    private String merchantUid;
    private String amount;
    private String name;
    private String buyerEmail;
    private String buyerName;
    private String buyerTel;
    private String paymentMethod;

    // 결제창 호출을 위한 JavaScript 코드
    private String paymentScript;
}

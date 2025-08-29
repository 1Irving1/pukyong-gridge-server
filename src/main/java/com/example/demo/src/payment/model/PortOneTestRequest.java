package com.example.demo.src.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 포트원 결제 테스트 요청 DTO
 * 실제 포트원 결제창 연동을 위한 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortOneTestRequest {

    // 사용자 ID
    private Long userId;

    // 결제 금액
    private BigDecimal amount;

    // 가맹점 주문 ID
    private String merchantUid;

    // 결제 방법
    private String paymentMethod;

    // 상품명
    private String productName;

    // 구매자 이름
    private String buyerName;

    // 구매자 이메일
    private String buyerEmail;

    // 구매자 전화번호
    private String buyerTel;
}

package com.example.demo.src.payment;

import com.example.demo.src.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // PortOne 결제 ID로 조회 (imp_uid) - 결제 검증용
    Optional<Payment> findByImpUid(String impUid);
    
    // 가맹점 주문 ID로 조회 (merchant_uid) - 중복 주문 방지용
    Optional<Payment> findByMerchantUid(String merchantUid);
    
    // 사용자별 결제 내역 조회 - 결제 내역 조회용
    List<Payment> findByUserId(Long userId);
    
    // 결제 상태별 조회 - 어드민용
    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);
}

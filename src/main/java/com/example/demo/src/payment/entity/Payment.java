package com.example.demo.src.payment.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity
@Table(name = "payments")
public class Payment {
    
    /**
     * 결제 상태 enum
     */
    public enum PaymentStatus {
        READY,      // 결제 준비
        PAID,       // 결제 완료
        CANCELLED,  // 결제 취소
        FAILED      // 결제 실패
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "subscription_id")
    private Long subscriptionId;
    
    @Column(name = "imp_uid", nullable = false, unique = true, length = 255)
    private String impUid;
    
    @Column(name = "merchant_uid", nullable = false, length = 255)
    private String merchantUid;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.EnumType")
    @org.hibernate.annotations.ColumnTransformer(
        read = "UPPER(payment_status)",
        write = "UPPER(?)"
    )
    private PaymentStatus paymentStatus;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 결제 생성
     */
    @Builder
    public Payment(Long userId, Long subscriptionId, String impUid, String merchantUid, 
                   BigDecimal amount, PaymentStatus paymentStatus) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.impUid = impUid;
        this.merchantUid = merchantUid;
        this.amount = amount;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.READY;
    }
    
    /**
     * 결제 상태 변경
     */
    public void updatePaymentStatus(PaymentStatus status) {
        this.paymentStatus = status;
    }
    
    /**
     * 구독 연결
     */
    public void connectSubscription(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    
    /**
     * 엔티티 저장 전 실행
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.READY;
        }
    }
    
    /**
     * 엔티티 업데이트 전 실행
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
package com.example.demo.src.subscription.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 구독 엔티티
 * 사용자의 구독 정보를 관리
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity
@Table(name = "subscriptions")
public class Subscription {

    /**
     * 결제 방법 enum
     */
    public enum PaymentMethod {
        card, bank_transfer, mobile
    }

    /**
     * 결제 상태 enum
     */
    public enum PaymentStatus {
        pending, completed, failed, refunded
    }

    /**
     * 구독 상태 enum
     */
    public enum SubscriptionStatus {
        active, expired, cancelled
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount = new BigDecimal("9900.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.pending;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubscriptionStatus status = SubscriptionStatus.active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Subscription(Long id, Long userId, BigDecimal amount, PaymentMethod paymentMethod,
                       PaymentStatus paymentStatus, LocalDate startDate, LocalDate endDate,
                       SubscriptionStatus status) {
        this.id = id;
        this.userId = userId;
        this.amount = amount != null ? amount : new BigDecimal("9900.00");
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.pending;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status != null ? status : SubscriptionStatus.active;
    }

    /**
     * 결제 상태 업데이트
     */
    public void updatePaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    /**
     * 구독 상태 업데이트
     */
    public void updateStatus(SubscriptionStatus status) {
        this.status = status;
    }

    /**
     * 구독 기간 업데이트
     */
    public void updatePeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 구독 취소
     */
    public void cancel() {
        this.status = SubscriptionStatus.cancelled;
    }

    /**
     * 구독 만료 처리
     */
    public void expire() {
        this.status = SubscriptionStatus.expired;
    }

    /**
     * 활성 구독 여부 확인
     */
    public boolean isActive() {
        return this.status == SubscriptionStatus.active && 
               this.paymentStatus == PaymentStatus.completed &&
               LocalDate.now().isBefore(this.endDate);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

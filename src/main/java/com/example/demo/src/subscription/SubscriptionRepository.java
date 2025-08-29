package com.example.demo.src.subscription;

import com.example.demo.src.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 구독 레포지토리
 * 구독 정보에 대한 데이터 접근을 담당
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 사용자별 구독 조회
     */
    List<Subscription> findByUserId(Long userId);

    /**
     * 사용자의 활성 구독 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = 'active' AND s.paymentStatus = 'completed' AND s.endDate > :today")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    /**
     * 구독 상태별 조회
     */
    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);

    /**
     * 결제 상태별 조회
     */
    List<Subscription> findByPaymentStatus(Subscription.PaymentStatus paymentStatus);

    /**
     * 만료 예정 구독 조회 (7일 이내)
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'active' AND s.endDate BETWEEN :startDate AND :endDate")
    List<Subscription> findExpiringSubscriptions(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 만료된 구독 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'active' AND s.endDate < :today")
    List<Subscription> findExpiredSubscriptions(@Param("today") LocalDate today);

    /**
     * 사용자의 최신 구독 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId ORDER BY s.createdAt DESC")
    List<Subscription> findLatestSubscriptionsByUserId(@Param("userId") Long userId);

    /**
     * 특정 기간 내 생성된 구독 조회
     */
    List<Subscription> findByCreatedAtBetween(java.time.LocalDateTime startDateTime, java.time.LocalDateTime endDateTime);
}

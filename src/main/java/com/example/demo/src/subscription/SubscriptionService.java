package com.example.demo.src.subscription;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.subscription.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 구독 서비스
 * 구독 생성, 조회, 관리 등의 비즈니스 로직을 담당
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * 구독 생성
     */
    @Transactional
    public Subscription createSubscription(Long userId, Subscription.PaymentMethod paymentMethod) {
        // 기존 활성 구독이 있는지 확인
        LocalDate today = LocalDate.now();
        subscriptionRepository.findActiveSubscriptionByUserId(userId, today)
                .ifPresent(existing -> {
                    throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
                });

        // 구독 기간 설정 (1개월)
        LocalDate startDate = today;
        LocalDate endDate = startDate.plusMonths(1);

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .paymentMethod(paymentMethod)
                .startDate(startDate)
                .endDate(endDate)
                .status(Subscription.SubscriptionStatus.active)
                .paymentStatus(Subscription.PaymentStatus.pending)
                .build();

        return subscriptionRepository.save(subscription);
    }

    /**
     * 구독 조회 (ID로)
     */
    public Subscription getSubscription(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.RESPONSE_ERROR));
    }

    /**
     * 사용자별 구독 조회
     */
    public List<Subscription> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    /**
     * 사용자의 활성 구독 조회
     */
    public Subscription getActiveSubscription(Long userId) {
        LocalDate today = LocalDate.now();
        return subscriptionRepository.findActiveSubscriptionByUserId(userId, today)
                .orElse(null);
    }

    /**
     * 구독 상태별 조회
     */
    public List<Subscription> getSubscriptionsByStatus(Subscription.SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status);
    }

    /**
     * 결제 상태별 조회
     */
    public List<Subscription> getSubscriptionsByPaymentStatus(Subscription.PaymentStatus paymentStatus) {
        return subscriptionRepository.findByPaymentStatus(paymentStatus);
    }

    /**
     * 만료 예정 구독 조회 (7일 이내)
     */
    public List<Subscription> getExpiringSubscriptions() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);
        return subscriptionRepository.findExpiringSubscriptions(startDate, endDate);
    }

    /**
     * 만료된 구독 조회
     */
    public List<Subscription> getExpiredSubscriptions() {
        LocalDate today = LocalDate.now();
        return subscriptionRepository.findExpiredSubscriptions(today);
    }

    /**
     * 구독 결제 상태 업데이트
     */
    @Transactional
    public Subscription updatePaymentStatus(Long subscriptionId, Subscription.PaymentStatus paymentStatus) {
        Subscription subscription = getSubscription(subscriptionId);
        subscription.updatePaymentStatus(paymentStatus);
        return subscriptionRepository.save(subscription);
    }

    /**
     * 구독 상태 업데이트
     */
    @Transactional
    public Subscription updateStatus(Long subscriptionId, Subscription.SubscriptionStatus status) {
        Subscription subscription = getSubscription(subscriptionId);
        subscription.updateStatus(status);
        return subscriptionRepository.save(subscription);
    }

    /**
     * 구독 취소
     */
    @Transactional
    public Subscription cancelSubscription(Long subscriptionId) {
        Subscription subscription = getSubscription(subscriptionId);
        subscription.cancel();
        return subscriptionRepository.save(subscription);
    }

    /**
     * 구독 갱신
     */
    @Transactional
    public Subscription renewSubscription(Long subscriptionId) {
        Subscription subscription = getSubscription(subscriptionId);
        
        // 새로운 구독 기간 설정
        LocalDate newStartDate = LocalDate.now();
        LocalDate newEndDate = newStartDate.plusMonths(1);
        
        subscription.updatePeriod(newStartDate, newEndDate);
        subscription.updateStatus(Subscription.SubscriptionStatus.active);
        subscription.updatePaymentStatus(Subscription.PaymentStatus.pending);
        
        return subscriptionRepository.save(subscription);
    }

    /**
     * 만료된 구독 처리 (스케줄러용)
     */
    @Transactional
    public void processExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = getExpiredSubscriptions();
        
        for (Subscription subscription : expiredSubscriptions) {
            subscription.expire();
            subscriptionRepository.save(subscription);
        }
    }
}

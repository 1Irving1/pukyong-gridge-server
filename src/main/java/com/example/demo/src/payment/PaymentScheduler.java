package com.example.demo.src.payment;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 결제 스케줄러
 * 가이드라인 요구사항: 정기적으로 결제된 금액을 검증하고, 다른 금액에 대한 예외 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final PortOneService portOneService;

    /**
     * 매일 새벽 2시에 실행되는 결제 금액 검증 스케줄러
     * 가이드라인 요구사항: 정기적으로 결제된 금액을 검증하고, 다른 금액에 대한 예외 처리
     */
    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
    public void verifyPaymentAmounts() {
        log.info("결제 금액 검증 스케줄러 시작");
        
        try {
            // READY 상태의 결제들을 조회
            List<Payment> readyPayments = paymentRepository.findByPaymentStatus(Payment.PaymentStatus.READY);
            
            for (Payment payment : readyPayments) {
                try {
                    // PortOne에서 실제 결제 정보 조회
                    boolean isVerified = portOneService.verifyAmount(payment.getImpUid(), payment.getAmount().doubleValue());
                    
                    if (!isVerified) {
                        log.warn("결제 금액 불일치: Payment ID={}, Expected={}, Actual=PortOne에서 조회된 금액", 
                                payment.getId(), payment.getAmount());
                        
                        // 금액이 일치하지 않으면 결제 실패 처리
                        payment.updatePaymentStatus(Payment.PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                        
                        // 예외 처리: 다른 금액에 대한 예외 처리 (가이드라인 요구사항)
                        handleAmountMismatch(payment);
                    } else {
                        log.info("결제 금액 검증 성공: Payment ID={}", payment.getId());
                    }
                } catch (Exception e) {
                    log.error("결제 금액 검증 중 오류 발생: Payment ID={}, Error={}", payment.getId(), e.getMessage());
                    
                    // 검증 실패 시 결제 실패 처리
                    payment.updatePaymentStatus(Payment.PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                }
            }
            
            log.info("결제 금액 검증 스케줄러 완료");
        } catch (Exception e) {
            log.error("결제 금액 검증 스케줄러 실행 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 금액 불일치 시 예외 처리 (가이드라인 요구사항)
     */
    private void handleAmountMismatch(Payment payment) {
        // 1. 로그 기록
        log.error("결제 금액 불일치 감지: Payment ID={}, Amount={}", payment.getId(), payment.getAmount());
        
        // 2. 관리자 알림 (실제로는 이메일, SMS 등으로 알림)
        // sendAdminNotification(payment);
        
        // 3. 결제 취소 처리 (PortOne API 호출)
        try {
            portOneService.cancelPayment(payment.getImpUid(), "금액 불일치로 인한 자동 취소");
            log.info("결제 취소 완료: Payment ID={}", payment.getId());
        } catch (Exception e) {
            log.error("결제 취소 실패: Payment ID={}, Error={}", payment.getId(), e.getMessage());
        }
    }

    /**
     * 매월 1일 자정에 실행되는 정기 결제 스케줄러
     * 가이드라인 요구사항: 정기적으로 스케줄러를 실행하여 해당하는 날짜에 결제가 이루어지도록
     */
    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 자정
    public void processRecurringPayments() {
        log.info("정기 결제 스케줄러 시작");
        
        try {
            // TODO: 구독 중인 사용자들의 정기 결제 처리
            // 1. 구독 테이블에서 활성 구독 조회
            // 2. 각 구독에 대해 정기 결제 생성
            // 3. PortOne API를 통한 결제 처리
            
            log.info("정기 결제 스케줄러 완료");
        } catch (Exception e) {
            log.error("정기 결제 스케줄러 실행 중 오류 발생: {}", e.getMessage());
        }
    }
}

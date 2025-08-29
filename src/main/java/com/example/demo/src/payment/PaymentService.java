package com.example.demo.src.payment;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.payment.entity.Payment;
import com.example.demo.src.payment.model.PaymentRequest;
import com.example.demo.src.payment.model.PaymentResponse;
import com.example.demo.src.payment.model.PaymentVerificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 결제 서비스
 * 결제 생성, 조회, 검증 등의 비즈니스 로직을 담당
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PortOneService portOneService;

    /**
     * 결제 생성
     */
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // 중복 주문 확인
        if (paymentRepository.findByMerchantUid(request.getMerchantUid()).isPresent()) {
            throw new BaseException(BaseResponseStatus.DUPLICATED_ORDER);
        }

        // 결제 전 금액 검증 (가이드라인 요구사항)
        BigDecimal expectedAmount = calculateExpectedAmount(request.getUserId());
        if (!request.getAmount().equals(expectedAmount)) {
            throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
        }

        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .merchantUid(request.getMerchantUid())
                .amount(request.getAmount())
                .impUid(request.getImpUid() != null ? request.getImpUid() : "temp_" + System.currentTimeMillis())
                .paymentStatus(Payment.PaymentStatus.READY)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return new PaymentResponse(savedPayment);
    }

    /**
     * 서버에서 계산한 실제 상품 금액 (가이드라인 요구사항)
     */
    private BigDecimal calculateExpectedAmount(Long userId) {
        // 현재는 고정 금액 9900원 반환
        // 실제로는 사용자별 할인, 쿠폰, 구독 상태 등을 고려하여 계산
        return new BigDecimal("9900.00");
    }

    /**
     * 결제 조회 (ID로)
     */
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.RESPONSE_ERROR));
        return new PaymentResponse(payment);
    }

    /**
     * 사용자별 결제 내역 조회
     */
    public List<PaymentResponse> getUserPayments(Long userId) {
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 결제 상태별 조회 (관리자용)
     */
    public List<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByPaymentStatus(status);
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 결제 검증 및 상태 업데이트 (PortOne 콜백용)
     */
    @Transactional
    public PaymentResponse verifyPayment(PaymentVerificationRequest request) {
        // 결제 내역 조회 (없으면 생성)
        Payment payment = paymentRepository.findByImpUid(request.getImpUid())
                .orElseGet(() -> {
                    // PortOne에서 결제 정보 조회
                    try {
                        Map<String, Object> portoneInfo = portOneService.getPaymentInfo(request.getImpUid());
                        if (portoneInfo.containsKey("response")) {
                            Map<String, Object> response = (Map<String, Object>) portoneInfo.get("response");
                            BigDecimal amount = new BigDecimal(response.get("amount").toString());
                            
                            // 새로운 결제 생성
                            Payment newPayment = Payment.builder()
                                    .userId(1L) // 실제로는 request에서 받아야 함
                                    .impUid(request.getImpUid())
                                    .merchantUid(request.getMerchantUid())
                                    .amount(amount)
                                    .paymentStatus(Payment.PaymentStatus.READY)
                                    .build();
                            
                            return paymentRepository.save(newPayment);
                        }
                    } catch (Exception e) {
                        throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
                    }
                    throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
                });

        // 주문 ID 일치 확인
        if (!payment.getMerchantUid().equals(request.getMerchantUid())) {
            throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
        }

        // PortOne에서 실제 결제 정보 조회 및 검증
        if ("success".equals(request.getStatus())) {
            // 실제 PortOne API 호출하여 결제 정보 검증
            boolean isVerified = portOneService.verifyAmount(request.getImpUid(), payment.getAmount().doubleValue());
            
            if (!isVerified) {
                throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
            }
        }

        // 결제 상태 업데이트
        Payment.PaymentStatus newStatus = "success".equals(request.getStatus()) 
                ? Payment.PaymentStatus.PAID 
                : Payment.PaymentStatus.FAILED;
        
        payment.updatePaymentStatus(newStatus);
        
        return new PaymentResponse(payment);
    }


    /**
     * 결제 취소
     */
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.RESPONSE_ERROR));

        // 결제 완료된 건만 취소 가능
        if (payment.getPaymentStatus() != Payment.PaymentStatus.PAID) {
            throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
        }

        payment.updatePaymentStatus(Payment.PaymentStatus.CANCELLED);
        return new PaymentResponse(payment);
    }
}

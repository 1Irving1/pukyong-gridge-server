package com.example.demo.src.payment;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.payment.model.PaymentRequest;
import com.example.demo.src.payment.model.PaymentResponse;
import com.example.demo.src.payment.model.PaymentVerificationRequest;
import com.example.demo.src.payment.model.PortOnePaymentResponse;
import com.example.demo.src.payment.model.PortOneTestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결제 컨트롤러
 * 결제 관련 API 엔드포인트를 제공
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;
    private final PortOneService portOneService;

    /**
     * 결제 생성
     */
    @Operation(summary = "결제 생성", description = "새로운 결제를 생성합니다.")
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        // 기본 유효성 검사
        if (request.getUserId() == null) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }
        if (request.getMerchantUid() == null || request.getMerchantUid().trim().isEmpty()) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }

        PaymentResponse response = paymentService.createPayment(request);
        return new BaseResponse<>(response);
    }

    /**
     * 포트원 결제창 연동 테스트
     */
    @Operation(summary = "포트원 결제창 연동", description = "포트원 결제창을 호출하기 위한 정보를 생성합니다.")
    @ResponseBody
    @PostMapping("/portone/payment")
    public BaseResponse<PortOnePaymentResponse> createPortOnePayment(@RequestBody PortOneTestRequest request) {
        // 기본 유효성 검사
        if (request.getUserId() == null) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }
        if (request.getMerchantUid() == null || request.getMerchantUid().trim().isEmpty()) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }

        // 포트원 결제창 호출 정보 생성
        PortOnePaymentResponse response = new PortOnePaymentResponse();
        response.setMerchantUid(request.getMerchantUid());
        response.setAmount(request.getAmount().toString());
        response.setName(request.getProductName() != null ? request.getProductName() : "테스트 상품");
        response.setBuyerEmail(request.getBuyerEmail() != null ? request.getBuyerEmail() : "test@example.com");
        response.setBuyerName(request.getBuyerName() != null ? request.getBuyerName() : "테스트 사용자");
        response.setBuyerTel(request.getBuyerTel() != null ? request.getBuyerTel() : "010-1234-5678");
        response.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "card");

        // 결제창 호출을 위한 JavaScript 코드 생성
        String paymentScript = generatePaymentScript(response);
        response.setPaymentScript(paymentScript);

        return new BaseResponse<>(response);
    }

    /**
     * 결제창 호출을 위한 JavaScript 코드 생성
     */
    private String generatePaymentScript(PortOnePaymentResponse response) {
        return String.format(
            "var IMP = window.IMP;\n" +
            "IMP.init('0366407778602480');\n" +
            "IMP.request_pay({\n" +
            "    pg: 'html5_inicis',\n" +
            "    pay_method: '%s',\n" +
            "    merchant_uid: '%s',\n" +
            "    amount: %s,\n" +
            "    name: '%s',\n" +
            "    buyer_email: '%s',\n" +
            "    buyer_name: '%s',\n" +
            "    buyer_tel: '%s'\n" +
            "}, function(rsp) {\n" +
            "    if (rsp.success) {\n" +
            "        // 결제 성공 시 서버에 검증 요청\n" +
            "        fetch('/payments/verify', {\n" +
            "            method: 'POST',\n" +
            "            headers: {'Content-Type': 'application/json'},\n" +
            "            body: JSON.stringify({\n" +
            "                impUid: rsp.imp_uid,\n" +
            "                merchantUid: rsp.merchant_uid,\n" +
            "                status: 'success'\n" +
            "            })\n" +
            "        });\n" +
            "    } else {\n" +
            "        // 결제 실패 시 서버에 실패 요청\n" +
            "        fetch('/payments/verify', {\n" +
            "            method: 'POST',\n" +
            "            headers: {'Content-Type': 'application/json'},\n" +
            "            body: JSON.stringify({\n" +
            "                impUid: rsp.imp_uid,\n" +
            "                merchantUid: rsp.merchant_uid,\n" +
            "                status: 'failed'\n" +
            "            })\n" +
            "        });\n" +
            "    }\n" +
            "});",
            response.getPaymentMethod(),
            response.getMerchantUid(),
            response.getAmount(),
            response.getName(),
            response.getBuyerEmail(),
            response.getBuyerName(),
            response.getBuyerTel()
        );
    }

    /**
     * 결제 조회 (ID로)
     */
    @Operation(summary = "결제 조회", description = "결제 ID로 결제 정보를 조회합니다.")
    @ResponseBody
    @GetMapping("/{paymentId}")
    public BaseResponse<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return new BaseResponse<>(response);
    }

    /**
     * 사용자별 결제 내역 조회
     */
    @Operation(summary = "사용자 결제 내역", description = "사용자의 모든 결제 내역을 조회합니다.")
    @ResponseBody
    @GetMapping("/user/{userId}")
    public BaseResponse<List<PaymentResponse>> getUserPayments(@PathVariable Long userId) {
        List<PaymentResponse> responses = paymentService.getUserPayments(userId);
        return new BaseResponse<>(responses);
    }

    /**
     * 결제 상태별 조회 (관리자용)
     */
    @Operation(summary = "결제 상태별 조회", description = "결제 상태별로 결제 내역을 조회합니다.")
    @ResponseBody
    @GetMapping("/status/{status}")
    public BaseResponse<List<PaymentResponse>> getPaymentsByStatus(@PathVariable String status) {
        try {
            com.example.demo.src.payment.entity.Payment.PaymentStatus paymentStatus =
                com.example.demo.src.payment.entity.Payment.PaymentStatus.valueOf(status.toUpperCase());
            List<PaymentResponse> responses = paymentService.getPaymentsByStatus(paymentStatus);
            return new BaseResponse<>(responses);
        } catch (IllegalArgumentException e) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }
    }

    /**
     * 결제 취소
     */
    @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
    @ResponseBody
    @PostMapping("/{paymentId}/cancel")
    public BaseResponse<PaymentResponse> cancelPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.cancelPayment(paymentId);
        return new BaseResponse<>(response);
    }

    /**
     * PortOne 결제 검증 (콜백)
     */
    @Operation(summary = "결제 검증", description = "PortOne에서 결제 완료 후 호출되는 검증 API입니다.")
    @ResponseBody
    @PostMapping("/verify")
    public BaseResponse<PaymentResponse> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        // 기본 유효성 검사
        if (request.getImpUid() == null || request.getImpUid().trim().isEmpty()) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }
        if (request.getMerchantUid() == null || request.getMerchantUid().trim().isEmpty()) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            return new BaseResponse<>(BaseResponseStatus.RESPONSE_ERROR);
        }

        PaymentResponse response = paymentService.verifyPayment(request);
        return new BaseResponse<>(response);
    }


    /**
     * 결제 상태 확인
     */
    @Operation(summary = "결제 상태 확인", description = "결제의 현재 상태를 확인합니다.")
    @ResponseBody
    @GetMapping("/{paymentId}/status")
    public BaseResponse<String> getPaymentStatus(@PathVariable Long paymentId) {
        PaymentResponse payment = paymentService.getPayment(paymentId);
        return new BaseResponse<>(payment.getPaymentStatus().name());
    }
}

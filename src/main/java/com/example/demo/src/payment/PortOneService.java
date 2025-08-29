package com.example.demo.src.payment;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * PortOne 연동 서비스
 * 실제 PortOne API와 통신하여 결제 검증을 수행
 */
@Service
@RequiredArgsConstructor
public class PortOneService {

    private final RestTemplate restTemplate;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.secret-key}")
    private String secretKey;

    @Value("${portone.test-mode}")
    private boolean testMode;

    private static final String PORTONE_API_URL = "https://api.iamport.kr";

    /**
     * PortOne에서 결제 정보 조회
     */
    public Map<String, Object> getPaymentInfo(String impUid) {
        // 테스트 모드일 때는 가짜 데이터 반환
        if (testMode) {
            Map<String, Object> fakeResponse = new HashMap<>();
            Map<String, Object> response = new HashMap<>();
            response.put("amount", 9900);
            response.put("status", "paid");
            response.put("imp_uid", impUid);
            fakeResponse.put("response", response);
            return fakeResponse;
        }
        
        try {
            String url = PORTONE_API_URL + "/payments/" + impUid;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAccessToken());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
            }
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
        }
    }

    /**
     * PortOne 결제 취소
     */
    public Map<String, Object> cancelPayment(String impUid, String reason) {
        try {
            String url = PORTONE_API_URL + "/payments/" + impUid + "/cancel";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAccessToken());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("reason", reason);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
            }
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
        }
    }

    /**
     * PortOne Access Token 발급
     */
    private String getAccessToken() {
        try {
            String url = PORTONE_API_URL + "/users/getToken";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("imp_key", apiKey);
            requestBody.put("imp_secret", secretKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("response")) {
                    Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
                    return (String) responseData.get("access_token");
                }
            }
            throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
        }
    }

    /**
     * 결제 금액 검증
     */
    public boolean verifyAmount(String impUid, double expectedAmount) {
        // 테스트 모드일 때는 항상 true 반환
        if (testMode) {
            return true;
        }
        
        try {
            Map<String, Object> paymentInfo = getPaymentInfo(impUid);
            
            if (paymentInfo.containsKey("response")) {
                Map<String, Object> response = (Map<String, Object>) paymentInfo.get("response");
                double actualAmount = Double.parseDouble(response.get("amount").toString());
                return actualAmount == expectedAmount;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}

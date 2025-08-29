package com.example.demo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class PortOneApiClient {

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.secret-key}")
    private String secretKey;

    @Value("${portone.test-mode}")
    private boolean testMode;

    private final String BASE_URL = "https://api.iamport.kr";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 포트원 액세스 토큰 발급
     */
    public String getAccessToken() throws Exception {
        String url = BASE_URL + "/users/getToken";
        
        // Basic Auth 헤더 생성
        String credentials = apiKey + ":" + secretKey;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 요청 본문
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("imp_key", apiKey);
        requestBody.put("imp_secret", secretKey);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // API 호출
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();
        
        if (responseBody != null && responseBody.get("code").equals(0)) {
            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
            return (String) responseData.get("access_token");
        } else {
            throw new RuntimeException("포트원 액세스 토큰 발급 실패: " + (responseBody != null ? responseBody.get("message") : "Unknown error"));
        }
    }

    /**
     * 결제 정보 조회
     */
    public Map<String, Object> getPaymentInfo(String impUid) throws Exception {
        String accessToken = getAccessToken();
        String url = BASE_URL + "/payments/" + impUid;
        
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }

    /**
     * 결제 취소
     */
    public Map<String, Object> cancelPayment(String impUid, String reason) throws Exception {
        String accessToken = getAccessToken();
        String url = BASE_URL + "/payments/" + impUid + "/cancel";
        
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 요청 본문
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", reason);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // API 호출
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return response.getBody();
    }
} 
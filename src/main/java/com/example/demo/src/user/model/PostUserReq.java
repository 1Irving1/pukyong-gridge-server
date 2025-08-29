package com.example.demo.src.user.model;

import com.example.demo.src.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUserReq {
    private String email;
    private String password;
    private String name;
    private String username;
    private String phoneNumber;  // 휴대폰 번호 추가
    private LocalDate birthDate;  // 생년월일 추가
    private boolean isOAuth;
    
    // 약관 동의 필드 추가
    private Boolean termsOfService;      // 이용약관 (필수)
    private Boolean dataPolicy;          // 데이터 정책 (필수)
    private Boolean locationBasedService; // 위치 기반 기능 (필수)

    public User toEntity() {
        return User.builder()
                .username(this.username)
                .email(this.email)
                .passwordHash(this.password)
                .name(this.name)
                .phoneNumber(this.phoneNumber)  // phoneNumber 추가
                .birthDate(this.birthDate)      // birthDate 추가
                .isOauth(this.isOAuth)
                .termsOfService(this.termsOfService)      // 약관 동의 추가
                .dataPolicy(this.dataPolicy)              // 약관 동의 추가
                .locationBasedService(this.locationBasedService) // 약관 동의 추가
                .build();
    }
}

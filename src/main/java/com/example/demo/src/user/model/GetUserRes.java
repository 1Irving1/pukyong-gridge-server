package com.example.demo.src.user.model;

import com.example.demo.src.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetUserRes {
    private Long id;
    private String username;  // 사용자 ID
    private String name;      // 사용자 이름/별명
    private String email;
    private User.UserStatus status;  // 회원 상태
    private LocalDate registrationDate;  // 회원가입 날짜
    private LocalDateTime lastLoginTime;  // 마지막 로그인 시간
    private Boolean isOauth;  // OAuth 사용자 여부
    private LocalDateTime createdAt;  // 생성일
    private LocalDateTime updatedAt;  // 수정일

    public GetUserRes(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.status = user.getStatus();
        this.registrationDate = user.getRegistrationDate();
        this.lastLoginTime = user.getLastLoginTime();
        this.isOauth = user.getIsOauth();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}

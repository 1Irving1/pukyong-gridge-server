package com.example.demo.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostPasswordResetReq {
    private String phoneNumber;  // 휴대폰 번호
    private String newPassword;  // 새로운 비밀번호
} 
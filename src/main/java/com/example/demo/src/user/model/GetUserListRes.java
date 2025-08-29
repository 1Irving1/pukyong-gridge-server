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
public class GetUserListRes {
    private Long id;
    private String username;  // 사용자 ID
    private String name;      // 사용자 이름/별명
    private LocalDate registrationDate;  // 회원가입 날짜

    public GetUserListRes(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.registrationDate = user.getRegistrationDate();
    }
} 
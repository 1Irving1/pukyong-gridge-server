package com.example.demo.src.user.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.demo.utils.PersonalInfoEncryption;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@Table(name = "users") // Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.
public class User {

    @Id // PK를 의미하는 어노테이션
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "is_oauth")
    private Boolean isOauth = false;

    // 약관 동의 필드 추가
    @Column(name = "terms_of_service")
    private Boolean termsOfService = false;

    @Column(name = "data_policy")
    private Boolean dataPolicy = false;

    @Column(name = "location_based_service")
    private Boolean locationBasedService = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Builder
    public User(Long id, String username, String name, String email, String phoneNumber, LocalDate birthDate, String passwordHash, 
                UserStatus status, LocalDate registrationDate, LocalDateTime lastLoginTime, 
                Boolean isOauth, Boolean termsOfService, Boolean dataPolicy, Boolean locationBasedService) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.passwordHash = passwordHash;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
        this.lastLoginTime = lastLoginTime;
        this.isOauth = isOauth != null ? isOauth : false;
        this.termsOfService = termsOfService != null ? termsOfService : false;
        this.dataPolicy = dataPolicy != null ? dataPolicy : false;
        this.locationBasedService = locationBasedService != null ? locationBasedService : false;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public enum UserStatus {
        ACTIVE, SUSPENDED, WITHDRAWN
    }

    //개인정보 암호화
    public void encryptPersonalInfo() throws Exception{
        if(this.name != null){
            this.name = PersonalInfoEncryption.encrypt(this.name);
        }
        if(this.email != null){
            this.email = PersonalInfoEncryption.encrypt(this.email);
        }
        if(this.phoneNumber != null){
            this.phoneNumber = PersonalInfoEncryption.encrypt(this.phoneNumber);
        }
        // birthDate는 LocalDate 타입이므로 암호화하지 않음 (개인정보보호법상 생년월일은 암호화 대상이 아님)
    }

    public void decryptPersonalInfo() throws Exception{
        if(this.name != null){
            this.name = PersonalInfoEncryption.decrypt(this.name);
        }
        if(this.email != null){
            this.email = PersonalInfoEncryption.decrypt(this.email);
        }
        if(this.phoneNumber != null){
            this.phoneNumber = PersonalInfoEncryption.decrypt(this.phoneNumber);
        }
        // birthDate는 LocalDate 타입이므로 복호화하지 않음
    }
}

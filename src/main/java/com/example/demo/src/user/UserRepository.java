package com.example.demo.src.user;

import com.example.demo.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 기존 메서드들을 UserStatus로 변경
    Optional<User> findByIdAndStatus(Long id, User.UserStatus status);
    Optional<User> findByUsernameAndStatus(String username, User.UserStatus status);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailAndStatus(String email, User.UserStatus status);
    List<User> findAllByEmailAndStatus(String email, User.UserStatus status);
    List<User> findAllByStatus(User.UserStatus status);

    // 회원 조회 기능 추가
    List<User> findByNameContaining(String name);
    List<User> findByUsernameContaining(String username);
    List<User> findByRegistrationDateBetween(LocalDate startDate, LocalDate endDate);
    List<User> findAllByOrderByRegistrationDateDesc();
    
    // 전화번호 관련 (비밀번호 재설정에서 사용)
    List<User> findByPhoneNumber(String phoneNumber);
}

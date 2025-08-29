package com.example.demo.src.user;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.src.user.entity.User;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.JwtService;
import com.example.demo.utils.SHA256;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.demo.common.response.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Transactional
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * 사용자명 유효성 검사
     */
    private void validateUsername(String username) {
        // null 체크 및 길이 검사(1-20자)
        if(username == null || username.length() < 1 || username.length() > 20){
            throw new BaseException(USERNAME_TOO_LONG);
        }

        // 형식 검사 (소문자 영어, 숫자, 밑줄, 마침표만)
        if(!username.matches("^[a-z0-9._]+$")){
            throw new BaseException(INVALID_USERNAME_FORMAT);
        }

        // 중복 체크
        Optional<User> existingUser = userRepository.findByUsername(username);
        if(existingUser.isPresent()){
            throw new BaseException(USERNAME_ALREADY_EXISTS);
        }
    }

    /**
     * 비밀번호 유효성 검사
     */
    private void validatePassword(String password) {
        // null 체크
        if(password == null || password.length() < 6){
            throw new BaseException(POST_USERS_INVALID_PASSWORD);
        }

        // 최대 길이 체크 (20자)
        if(password.length() > 20){
            throw new BaseException(POST_USERS_INVALID_PASSWORD);
        }

        // 비밀번호 형식 검사 (영문, 숫자, 특수문자 조합 권장)
        // 현재는 기본적인 길이 검사만 수행
        // 필요시 더 엄격한 형식 검사 추가 가능
    }

    /**
     * 생일 유효성 검사
     */
    private void validateBirthDate(LocalDate birthDate) {
        // null 체크
        if(birthDate == null) {
            throw new BaseException(INVALID_BIRTH_DATE);
        }

        // 미래 날짜 체크
        if(birthDate.isAfter(LocalDate.now())) {
            throw new BaseException(FUTURE_BIRTH_DATE);
        }

        // 연령 제한 체크 (1919-2015년만 허용, 만 8세 이상)
        int currentYear = LocalDate.now().getYear();
        int birthYear = birthDate.getYear();
        
        // 2016-2021년 출생자는 제한 (디자인 명세에 따라)
        if(birthYear >= 2016 && birthYear <= 2021) {
            throw new BaseException(AGE_RESTRICTION);
        }
        
        // 1919년 이전 출생자도 제한
        if(birthYear < 1919) {
            throw new BaseException(AGE_RESTRICTION);
        }
    }

    /**
     * 약관 동의 유효성 검사
     */
    private void validateTermsAgreement(Boolean termsOfService, Boolean dataPolicy, Boolean locationBasedService) {
        // 모든 필수 약관에 동의했는지 확인
        if(termsOfService == null || !termsOfService) {
            throw new BaseException(TERMS_NOT_AGREED);
        }
        if(dataPolicy == null || !dataPolicy) {
            throw new BaseException(TERMS_NOT_AGREED);
        }
        if(locationBasedService == null || !locationBasedService) {
            throw new BaseException(TERMS_NOT_AGREED);
        }
    }

    //POST
    public PostUserRes createUser(PostUserReq postUserReq) {
        // 이메일 중복 체크
        Optional<User> checkUser = userRepository.findByEmailAndStatus(postUserReq.getEmail(), User.UserStatus.ACTIVE);
        if(checkUser.isPresent() == true){
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }

        // username 유효성 검사 추가
        validateUsername(postUserReq.getUsername());

        // 비밀번호 유효성 검사 추가
        validatePassword(postUserReq.getPassword());

        // 생일 유효성 검사 추가
        validateBirthDate(postUserReq.getBirthDate());

        // 휴대폰 번호 형식 유효성 검사 (선택 입력 시 형식 체크)
        if(postUserReq.getPhoneNumber() != null && !com.example.demo.utils.ValidationRegex.isRegexPhoneNumber(postUserReq.getPhoneNumber())){
            throw new BaseException(INVALID_PHONE_NUMBER);
        }

        // 약관 동의 유효성 검사 추가
        validateTermsAgreement(postUserReq.getTermsOfService(), postUserReq.getDataPolicy(), postUserReq.getLocationBasedService());

        String encryptPwd;
        try {
            encryptPwd = new SHA256().encrypt(postUserReq.getPassword());
            postUserReq.setPassword(encryptPwd);
        } catch (Exception exception) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        // 개인정보 암호화 후 저장
        User saveUser = postUserReq.toEntity();
        try {
            saveUser.encryptPersonalInfo(); // 개인정보 암호화
        } catch (Exception exception) {
            throw new BaseException(PERSONAL_INFO_ENCRYPTION_ERROR);
        }
        saveUser = userRepository.save(saveUser);
        return new PostUserRes(saveUser.getId());

    }

    public PostUserRes createOAuthUser(User user) {
        User saveUser = userRepository.save(user);

        // JWT 발급
        String jwtToken = jwtService.createJwt(saveUser.getId());
        return new PostUserRes(saveUser.getId(), jwtToken);

    }

    public void modifyUserName(Long userId, PatchUserReq patchUserReq) {
        User user = userRepository.findByIdAndStatus(userId, User.UserStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        user.updateName(patchUserReq.getName());
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndStatus(userId, User.UserStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        user.updateStatus(User.UserStatus.WITHDRAWN);
    }

    @Transactional(readOnly = true)
    public List<GetUserRes> getUsers() {
        List<GetUserRes> getUserResList = userRepository.findAllByStatus(User.UserStatus.ACTIVE).stream()
                .peek(user -> {
                    try {
                        user.decryptPersonalInfo();
                    } catch (Exception e) {
                        // 복호화 실패 시 로그 기록 (개별 사용자 실패는 전체 목록에 영향 주지 않음)
                    }
                })
                .map(GetUserRes::new)
                .collect(Collectors.toList());
        return getUserResList;
    }

    @Transactional(readOnly = true)
    public List<GetUserRes> getUsersByEmail(String email) {
        List<GetUserRes> getUserResList = userRepository.findAllByEmailAndStatus(email, User.UserStatus.ACTIVE).stream()
                .peek(user -> {
                    try {
                        user.decryptPersonalInfo();
                    } catch (Exception e) {
                        // 복호화 실패 시 로그 기록
                    }
                })
                .map(GetUserRes::new)
                .collect(Collectors.toList());
        return getUserResList;
    }


    @Transactional(readOnly = true)
    public GetUserRes getUser(Long userId) {
        User user = userRepository.findByIdAndStatus(userId, User.UserStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        
        // 개인정보 복호화
        try {
            user.decryptPersonalInfo();
        } catch (Exception exception) {
            throw new BaseException(PERSONAL_INFO_DECRYPTION_ERROR);
        }
        
        return new GetUserRes(user);
    }

    @Transactional(readOnly = true)
    public boolean checkUserByEmail(String email) {
        Optional<User> result = userRepository.findByEmailAndStatus(email, User.UserStatus.ACTIVE);
        if (result.isPresent()) return true;
        return false;
    }

    public PostLoginRes logIn(PostLoginReq postLoginReq) {
        // 비밀번호 유효성 검사 추가
        validatePassword(postLoginReq.getPassword());

        // username으로 사용자 조회 (email → username으로 변경)
        User user = userRepository.findByUsernameAndStatus(postLoginReq.getUsername(), User.UserStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(USER_NOT_FOUND));

        String encryptPwd;
        try {
            encryptPwd = new SHA256().encrypt(postLoginReq.getPassword());
        } catch (Exception exception) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        if(user.getPasswordHash().equals(encryptPwd)){
            Long userId = user.getId();
            String jwt = jwtService.createJwt(userId);
            return new PostLoginRes(userId,jwt);
        } else{
            throw new BaseException(INVALID_PASSWORD);  // FAILED_TO_LOGIN → INVALID_PASSWORD로 변경
        }

    }

    public GetUserRes getUserByEmail(String email) {
        User user = userRepository.findByEmailAndStatus(email, User.UserStatus.ACTIVE).orElseThrow(() -> new BaseException(NOT_FIND_USER));
        
        // 개인정보 복호화
        try {
            user.decryptPersonalInfo();
        } catch (Exception exception) {
            throw new BaseException(PERSONAL_INFO_DECRYPTION_ERROR);
        }
        
        return new GetUserRes(user);
    }

    // ========== 어드민 회원 관리 기능 ==========

    /**
     * 회원 목록 조회 (가입 최신순) - 목록용 정보만
     */
    @Transactional(readOnly = true)
    public List<GetUserListRes> getUsersForAdmin() {
        return userRepository.findAllByOrderByRegistrationDateDesc().stream()
                .peek(user -> {
                    try {
                        user.decryptPersonalInfo();
                    } catch (Exception e) {
                        // 복호화 실패 시 로그 기록
                    }
                })
                .map(GetUserListRes::new)
                .collect(Collectors.toList());
    }

                /**
             * 별명으로 회원 검색
             */
            @Transactional(readOnly = true)
            public List<GetUserRes> getUsersByNickname(String nickname) {
                return userRepository.findByNameContaining(nickname).stream()
                        .peek(user -> {
                            try {
                                user.decryptPersonalInfo();
                            } catch (Exception e) {
                                // 복호화 실패 시 로그 기록
                            }
                        })
                        .map(GetUserRes::new)
                        .collect(Collectors.toList());
            }

    /**
     * 아이디로 회원 검색
     */
    @Transactional(readOnly = true)
    public List<GetUserRes> getUsersByUsername(String username) {
        return userRepository.findByUsernameContaining(username).stream()
                .peek(user -> {
                    try {
                        user.decryptPersonalInfo();
                    } catch (Exception e) {
                        // 복호화 실패 시 로그 기록
                    }
                })
                .map(GetUserRes::new)
                .collect(Collectors.toList());
    }

    /**
     * 가입일자 범위로 회원 검색
     */
    @Transactional(readOnly = true)
    public List<GetUserRes> getUsersByRegistrationDate(LocalDate startDate, LocalDate endDate) {
        return userRepository.findByRegistrationDateBetween(startDate, endDate).stream()
                .peek(user -> {
                    try {
                        user.decryptPersonalInfo();
                    } catch (Exception e) {
                        // 복호화 실패 시 로그 기록
                    }
                })
                .map(GetUserRes::new)
                .collect(Collectors.toList());
    }

    /**
     * 상태별 회원 검색
     */
    @Transactional(readOnly = true)
    public List<GetUserRes> getUsersByStatus(User.UserStatus status) {
        return userRepository.findAllByStatus(status).stream()
                .peek(user -> {
                    try {
                        user.decryptPersonalInfo();
                    } catch (Exception e) {
                        // 복호화 실패 시 로그 기록
                    }
                })
                .map(GetUserRes::new)
                .collect(Collectors.toList());
    }

    /**
     * 회원 상태 변경 (정지/활성화)
     */
    public void updateUserStatus(Long userId, User.UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        user.updateStatus(status);
    }

    /**
     * 회원 상세 조회 (어드민용)
     */
    @Transactional(readOnly = true)
    public GetUserRes getUserForAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        
        // 개인정보 복호화
        try {
            user.decryptPersonalInfo();
        } catch (Exception exception) {
            throw new BaseException(PERSONAL_INFO_DECRYPTION_ERROR);
        }
        
        return new GetUserRes(user);
    }

    /**
     * 다중 조건으로 회원 검색 (AND 조건) - 목록용 정보만
     */
    @Transactional(readOnly = true)
    public List<GetUserListRes> searchUsers(String username, String nickname, LocalDate registrationDate, User.UserStatus status) {
        // 모든 회원을 가져와서 조건에 맞는 것만 필터링
        List<User> allUsers = userRepository.findAll();
        
        return allUsers.stream()
                .filter(user -> {
                    // 사용자 ID 조건
                    if (username != null && !username.trim().isEmpty()) {
                        if (!user.getUsername().contains(username)) {
                            return false;
                        }
                    }
                    
                    // 별명 조건
                    if (nickname != null && !nickname.trim().isEmpty()) {
                        if (!user.getName().contains(nickname)) {
                            return false;
                        }
                    }
                    
                    // 회원가입 날짜 조건
                    if (registrationDate != null) {
                        if (!user.getRegistrationDate().equals(registrationDate)) {
                            return false;
                        }
                    }
                    
                    // 상태 조건
                    if (status != null) {
                        if (!user.getStatus().equals(status)) {
                            return false;
                        }
                    }
                    
                    return true; // 모든 조건을 만족
                })
                .sorted((u1, u2) -> u2.getRegistrationDate().compareTo(u1.getRegistrationDate()))
                .map(GetUserListRes::new)
                .collect(Collectors.toList());
    }

    /**
     * 휴대폰 번호를 이용한 비밀번호 재설정
     */
    public void resetPasswordByPhoneNumber(PostPasswordResetReq postPasswordResetReq) {
        // 휴대폰 번호로 사용자 조회
        List<User> users = userRepository.findByPhoneNumber(postPasswordResetReq.getPhoneNumber());
        
        if(users.isEmpty()) {
            throw new BaseException(PHONE_NUMBER_NOT_FOUND);
        }
        
        // 첫 번째 사용자 선택 (휴대폰 번호는 unique하지 않을 수 있음)
        User user = users.get(0);
        
        // 새 비밀번호 유효성 검사
        validatePassword(postPasswordResetReq.getNewPassword());
        
        // 새 비밀번호 암호화
        String encryptPwd;
        try {
            encryptPwd = new SHA256().encrypt(postPasswordResetReq.getNewPassword());
        } catch (Exception exception) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        
        // 비밀번호 업데이트
        try {
            user.updatePasswordHash(encryptPwd);
            userRepository.save(user);
        } catch (Exception exception) {
            throw new BaseException(PASSWORD_RESET_FAILED);
        }
    }
}

package com.example.demo.src.user;


import com.example.demo.common.Constant.SocialLoginType;
import com.example.demo.common.oauth.OAuthService;
import com.example.demo.utils.JwtService;
import lombok.RequiredArgsConstructor;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.user.model.*;
import com.example.demo.src.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


import static com.example.demo.common.response.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/users")
public class UserController {


    private final UserService userService;

    private final OAuthService oAuthService;

    private final JwtService jwtService;


    /**
     * 회원가입 API
     * [POST] /app/users
     * @return BaseResponse<PostUserRes>
     */
    // Body
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq) {
        // TODO: email 관련한 짧은 validation 예시입니다. 그 외 더 부가적으로 추가해주세요!
        if(postUserReq.getUsername() == null){
            return new BaseResponse<>(USERS_EMPTY_USERNAME);
        }
        if(postUserReq.getName() == null){
            return new BaseResponse<>(USERS_EMPTY_NAME);
        }
        if(postUserReq.getPassword() == null){
            return new BaseResponse<>(USERS_EMPTY_PASSWORD);
        }
        if(postUserReq.getPassword().length() < 6 || postUserReq.getPassword().length() >20){  // 8 → 6으로 수정
            return new BaseResponse<>(POST_USERS_INVALID_PASSWORD);
        }
        if(postUserReq.getEmail() == null){
            return new BaseResponse<>(USERS_EMPTY_EMAIL);
        }
        //이메일 정규표현
        if(!isRegexEmail(postUserReq.getEmail())){
            return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
        }
        
        // 휴대폰 번호 형식 검증 추가
        if(postUserReq.getPhoneNumber() != null && !com.example.demo.utils.ValidationRegex.isRegexPhoneNumber(postUserReq.getPhoneNumber())){
            return new BaseResponse<>(INVALID_PHONE_NUMBER);
        }
        
        // 생일 검증
        if(postUserReq.getBirthDate() == null){
            return new BaseResponse<>(INVALID_BIRTH_DATE);
        }
        
        // 약관 동의 검증 추가
        if(postUserReq.getTermsOfService() == null || postUserReq.getDataPolicy() == null || postUserReq.getLocationBasedService() == null){
            return new BaseResponse<>(INVALID_TERMS_AGREEMENT);
        }
        
        PostUserRes postUserRes = userService.createUser(postUserReq);
        return new BaseResponse<>(postUserRes);
    }

    /**
     * 회원 목록 조회 API
     * [GET] /app/users
     * 회원 목록 조회 및 다중 조건 검색 API (목록용 정보만 반환)
     * [GET] /app/users?username=testuser1&nickname=홍길동&registrationDate=20240101&status=ACTIVE
     * @return BaseResponse<List<GetUserListRes>>
     */
    //Query String
    @ResponseBody
    @GetMapping("") // (GET) 127.0.0.1:9000/app/users
    public BaseResponse<List<GetUserListRes>> getUsers(
            @RequestParam(required = false) String username,  // 사용자 ID 검색
            @RequestParam(required = false) String nickname,  // 사용자 이름/별명 검색
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate registrationDate,  // 회원가입 날짜 (YYYYMMDD)
            @RequestParam(required = false) User.UserStatus status,  // 회원 상태
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 모든 파라미터가 null인 경우 전체 회원 목록 조회 (가입 최신순)
        if (username == null && nickname == null && registrationDate == null && status == null) {
            List<GetUserListRes> getUsersRes = userService.getUsersForAdmin();
            return new BaseResponse<>(getUsersRes);
        }
        
        // 조건별 검색
        List<GetUserListRes> getUsersRes = userService.searchUsers(username, nickname, registrationDate, status);
        return new BaseResponse<>(getUsersRes);
    }

    /**
     * 회원 상세 조회 API
     * [GET] /app/users/:userId
     * @return BaseResponse<GetUserRes>
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{userId}") // (GET) 127.0.0.1:9000/app/users/:userId
    public BaseResponse<GetUserRes> getUser(@PathVariable("userId") Long userId) {
        GetUserRes getUserRes = userService.getUserForAdmin(userId);  // 어드민용으로 변경
        return new BaseResponse<>(getUserRes);
    }



    /**
     * 유저정보변경 API
     * [PATCH] /app/users/:userId
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userId}")
    public BaseResponse<String> modifyUserName(@PathVariable("userId") Long userId, @RequestBody PatchUserReq patchUserReq){

        Long jwtUserId = jwtService.getUserId();

        userService.modifyUserName(userId, patchUserReq);

        String result = "수정 완료!!";
        return new BaseResponse<>(result);

    }

    /**
     * 회원 상태 변경 API (어드민용)
     * [PATCH] /app/users/:userId/status
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userId}/status")
    public BaseResponse<String> updateUserStatus(@PathVariable("userId") Long userId, @RequestBody UserStatusUpdateReq statusUpdateReq){
        // TODO: 어드민 권한 검증 추가 필요
        
        userService.updateUserStatus(userId, statusUpdateReq.getStatus());

        String result = "회원 상태 변경 완료!!";
        return new BaseResponse<>(result);
    }

    /**
     * 유저정보삭제 API
     * [DELETE] /app/users/:userId
     * @return BaseResponse<String>
     */
    @ResponseBody
    @DeleteMapping("/{userId}")
    public BaseResponse<String> deleteUser(@PathVariable("userId") Long userId){
        Long jwtUserId = jwtService.getUserId();

        userService.deleteUser(userId);

        String result = "삭제 완료!!";
        return new BaseResponse<>(result);
    }

    /**
     * 로그인 API
     * [POST] /app/users/logIn
     * @return BaseResponse<PostLoginRes>
     */
    @ResponseBody
    @PostMapping("/logIn")
    public BaseResponse<PostLoginRes> logIn(@RequestBody PostLoginReq postLoginReq){
        // TODO: 로그인 값들에 대한 형식적인 validatin 처리해주셔야합니다!
        // TODO: 유저의 status ex) 비활성화된 유저, 탈퇴한 유저 등을 관리해주고 있다면 해당 부분에 대한 validation 처리도 해주셔야합니다.
        PostLoginRes postLoginRes = userService.logIn(postLoginReq);
        return new BaseResponse<>(postLoginRes);
    }

    /**
     * 비밀번호 재설정 API
     * [POST] /app/users/password-reset
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/password-reset")
    public BaseResponse<String> resetPassword(@RequestBody PostPasswordResetReq postPasswordResetReq){
        // TODO: 휴대폰 번호 형식 검증 추가 필요
        // 휴대폰 번호 형식 검증 추가
        if(postPasswordResetReq.getPhoneNumber() == null || !com.example.demo.utils.ValidationRegex.isRegexPhoneNumber(postPasswordResetReq.getPhoneNumber())){
            return new BaseResponse<>(INVALID_PHONE_NUMBER);
        }
        
        userService.resetPasswordByPhoneNumber(postPasswordResetReq);
        
        String result = "비밀번호가 성공적으로 변경되었습니다.";
        return new BaseResponse<>(result);
    }


    /**
     * 유저 소셜 가입, 로그인 인증으로 리다이렉트 해주는 url
     * [GET] /app/users/auth/:socialLoginType/login
     * @return void
     */
    @GetMapping("/auth/{socialLoginType}/login")
    public void socialLoginRedirect(@PathVariable(name="socialLoginType") String SocialLoginPath) throws IOException {
        SocialLoginType socialLoginType= SocialLoginType.valueOf(SocialLoginPath.toUpperCase());
        oAuthService.accessRequest(socialLoginType);
    }


    /**
     * Social Login API Server 요청에 의한 callback 을 처리
     * @param socialLoginPath (GOOGLE, FACEBOOK, NAVER, KAKAO)
     * @param code API Server 로부터 넘어오는 code
     * @return SNS Login 요청 결과로 받은 Json 형태의 java 객체 (access_token, jwt_token, user_num 등)
     */
    @ResponseBody
    @GetMapping(value = "/auth/{socialLoginType}/login/callback")
    public BaseResponse<GetSocialOAuthRes> socialLoginCallback(
            @PathVariable(name = "socialLoginType") String socialLoginPath,
            @RequestParam(name = "code") String code) throws IOException, BaseException{
        log.info(">> 소셜 로그인 API 서버로부터 받은 code : {}", code);
        SocialLoginType socialLoginType = SocialLoginType.valueOf(socialLoginPath.toUpperCase());
        GetSocialOAuthRes getSocialOAuthRes = oAuthService.oAuthLoginOrJoin(socialLoginType,code);
        return new BaseResponse<>(getSocialOAuthRes);
    }


}

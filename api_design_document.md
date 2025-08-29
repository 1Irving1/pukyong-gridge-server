# 🔌 API 설계 문서 - Simulation SERVER Challenge

## 📋 목차
1. [API 개요](#api-개요)
2. [공통 응답 형식](#공통-응답-형식)
3. [사용자 관련 API](#사용자-관련-api)
4. [어드민 관련 API](#어드민-관련-api)
5. [에러 코드 정의](#에러-코드-정의)

---

## 🌐 API 개요

### 기본 정보
- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **인증 방식**: JWT Bearer Token
- **문서화**: Swagger UI (`/swagger-ui.html`)

### HTTP 메서드
- `GET`: 데이터 조회
- `POST`: 데이터 생성
- `PATCH`: 데이터 부분 수정
- `DELETE`: 데이터 삭제

---

## 📊 공통 응답 형식

### 성공 응답 형식
```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    // 실제 데이터
  }
}
```

### 실패 응답 형식
```json
{
  "success": false,
  "error": "ERROR_CODE",
  "message": "오류 메시지",
  "field": "오류가 발생한 필드 (선택사항)"
}
```

---

## 👤 사용자 관련 API

### 1. 회원가입 API

#### 엔드포인트
```
POST /api/signup
```

#### 요청 데이터 (Request Body)
```json
{
  "username": "happypuppy",
  "name": "행복한강아지",
  "email": "happy@example.com",
  "password": "dog123",
  "phoneNumber": "01090010123"
}
```

#### 필드 설명
| 필드 | 타입 | 필수 | 설명 | 제약조건 |
|------|------|------|------|----------|
| username | String | O | 사용자명 | 3-20자, 영문/숫자/특수문자 |
| name | String | O | 사용자 이름 | 1-20자 |
| email | String | O | 이메일 | 이메일 형식 |
| password | String | O | 비밀번호 | 6-20자 |
| phoneNumber | String | O | 전화번호 | 11자리 숫자 |

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 123,
    "username": "happypuppy",
    "name": "행복한강아지",
    "email": "happy@example.com",
    "registrationDate": "2024-01-20"
  }
}
```

#### 실패 응답 예시

**사용자명 중복 (400 Bad Request)**
```json
{
  "success": false,
  "error": "USERNAME_ALREADY_EXISTS",
  "message": "이미 사용 중인 사용자명입니다.",
  "field": "username"
}
```

**이메일 형식 오류 (400 Bad Request)**
```json
{
  "success": false,
  "error": "INVALID_EMAIL_FORMAT",
  "message": "올바른 이메일 형식이 아닙니다.",
  "field": "email"
}
```

### 2. 실시간 유효성 검사 API

#### 엔드포인트
```
POST /api/signup/validation
```

#### 요청 데이터
```json
{
  "field": "username",
  "value": "happypuppy"
}
```

#### 성공 응답
```json
{
  "success": true,
  "message": "사용 가능한 사용자명입니다.",
  "data": {
    "isValid": true,
    "field": "username"
  }
}
```

#### 실패 응답
```json
{
  "success": false,
  "error": "USERNAME_ALREADY_EXISTS",
  "message": "이미 사용 중인 사용자명입니다.",
  "data": {
    "isValid": false,
    "field": "username"
  }
}
```

### 3. 로그인 API

#### 엔드포인트
```
POST /api/login
```

#### 요청 데이터
```json
{
  "username": "happypuppy",
  "password": "dog123"
}
```

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "message": "로그인이 성공했습니다.",
  "data": {
    "userId": 123,
    "username": "happypuppy",
    "name": "행복한강아지",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

#### 실패 응답 예시

**존재하지 않는 사용자 (404 Not Found)**
```json
{
  "success": false,
  "error": "USER_NOT_FOUND",
  "message": "존재하지 않는 사용자입니다."
}
```

**잘못된 비밀번호 (401 Unauthorized)**
```json
{
  "success": false,
  "error": "INVALID_PASSWORD",
  "message": "비밀번호가 올바르지 않습니다."
}
```

### 4. 카카오 로그인 API

#### 엔드포인트
```
POST /api/oauth/kakao
```

#### 요청 데이터
```json
{
  "accessToken": "kakao_access_token_here"
}
```

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "message": "카카오 로그인이 성공했습니다.",
  "data": {
    "userId": 123,
    "username": "happypuppy",
    "name": "행복한강아지",
    "isNewUser": false,
    "accessToken": "jwt_access_token_here",
    "refreshToken": "jwt_refresh_token_here"
  }
}
```

### 5. 비밀번호 재설정 API

#### 엔드포인트
```
POST /api/password/reset
```

#### 요청 데이터
```json
{
  "username": "happypuppy",
  "phoneNumber": "01090010123",
  "newPassword": "newdog123"
}
```

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "message": "비밀번호가 성공적으로 변경되었습니다.",
  "data": {
    "userId": 123,
    "username": "happypuppy"
  }
}
```

---

## 👨‍💼 어드민 관련 API

### 1. 회원 목록 조회 API

#### 엔드포인트
```
GET /api/admin/users
```

#### 쿼리 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|----------|------|------|------|------|
| username | String | X | 사용자명 검색 | `?username=happypuppy` |
| name | String | X | 이름 검색 | `?name=행복한강아지` |
| registrationDate | String | X | 가입일 검색 | `?registrationDate=20240120` |
| status | String | X | 회원 상태 필터 | `?status=ACTIVE` |
| page | Integer | X | 페이지 번호 | `?page=1` |
| size | Integer | X | 페이지 크기 | `?size=10` |

#### 요청 예시
```
GET /api/admin/users?username=happypuppy&status=ACTIVE&page=1&size=10
```

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "message": "회원 목록을 성공적으로 조회했습니다.",
  "data": {
    "users": [
      {
        "id": 123,
        "username": "happypuppy",
        "name": "행복한강아지",
        "email": "happy@example.com",
        "status": "ACTIVE",
        "registrationDate": "2024-01-20",
        "lastLoginTime": "2024-01-20T14:30:25"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 5,
      "totalElements": 50,
      "pageSize": 10
    }
  }
}
```

### 2. 회원 상세 조회 API

#### 엔드포인트
```
GET /api/admin/users/{userId}
```

#### 경로 파라미터
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| userId | Long | 회원 ID |

#### 요청 예시
```
GET /api/admin/users/123
```

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "message": "회원 정보를 성공적으로 조회했습니다.",
  "data": {
    "id": 123,
    "username": "happypuppy",
    "name": "행복한강아지",
    "email": "happy@example.com",
    "status": "ACTIVE",
    "registrationDate": "2024-01-20",
    "lastLoginTime": "2024-01-20T14:30:25",
    "isOauth": false,
    "createdAt": "2024-01-20T10:00:00",
    "updatedAt": "2024-01-20T14:30:25"
  }
}
```

### 3. 회원 상태 변경 API

#### 엔드포인트
```
PATCH /api/admin/users/{userId}/status
```

#### 경로 파라미터
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| userId | Long | 회원 ID |

#### 요청 데이터
```json
{
  "status": "SUSPENDED"
}
```

#### 상태 값
- `ACTIVE`: 활성화
- `SUSPENDED`: 정지
- `WITHDRAWN`: 탈퇴

#### 성공 응답 (200 OK)
```json
{
  "success": true,
  "message": "회원 상태가 성공적으로 변경되었습니다.",
  "data": {
    "userId": 123,
    "previousStatus": "ACTIVE",
    "newStatus": "SUSPENDED",
    "updatedAt": "2024-01-20T15:00:00"
  }
}
```

---

## ❌ 에러 코드 정의

### 사용자 관련 에러
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| `USER_NOT_FOUND` | 404 | 사용자를 찾을 수 없음 |
| `USERNAME_ALREADY_EXISTS` | 400 | 사용자명 중복 |
| `EMAIL_ALREADY_EXISTS` | 400 | 이메일 중복 |
| `INVALID_PASSWORD` | 401 | 잘못된 비밀번호 |
| `INVALID_EMAIL_FORMAT` | 400 | 잘못된 이메일 형식 |
| `INVALID_USERNAME_FORMAT` | 400 | 잘못된 사용자명 형식 |
| `PASSWORD_TOO_SHORT` | 400 | 비밀번호가 너무 짧음 |
| `PASSWORD_TOO_LONG` | 400 | 비밀번호가 너무 김 |

### 인증 관련 에러
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| `UNAUTHORIZED` | 401 | 인증되지 않은 요청 |
| `FORBIDDEN` | 403 | 권한이 없음 |
| `TOKEN_EXPIRED` | 401 | 토큰이 만료됨 |
| `INVALID_TOKEN` | 401 | 잘못된 토큰 |

### 어드민 관련 에러
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| `ADMIN_NOT_FOUND` | 404 | 어드민을 찾을 수 없음 |
| `INSUFFICIENT_PERMISSIONS` | 403 | 권한 부족 |
| `USER_STATUS_UPDATE_FAILED` | 500 | 회원 상태 변경 실패 |

### 시스템 에러
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| `INTERNAL_SERVER_ERROR` | 500 | 서버 내부 오류 |
| `DATABASE_ERROR` | 500 | 데이터베이스 오류 |
| `VALIDATION_ERROR` | 400 | 데이터 검증 오류 |
| `RESOURCE_NOT_FOUND` | 404 | 리소스를 찾을 수 없음 |

---

## 🔧 API 테스트 방법

### 1. Swagger UI 사용
- URL: `http://localhost:8080/swagger-ui.html`
- 브라우저에서 직접 API 테스트 가능
- 문서와 테스트를 동시에 확인

### 2. Postman 사용
- API 요청을 파일로 저장
- Collection으로 그룹 관리
- 자동화된 테스트 가능

### 3. curl 명령어 사용
```bash
# 회원가입 예시
curl -X POST http://localhost:8080/api/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "happypuppy",
    "name": "행복한강아지",
    "email": "happy@example.com",
    "password": "dog123",
    "phoneNumber": "01090010123"
  }'
```

---

## 📝 다음 단계

이 API 설계 문서를 바탕으로:

1. **Spring Boot Controller 구현**
2. **Service Layer 구현**
3. **Repository Layer 구현**
4. **Swagger 설정**
5. **Postman Collection 작성**

어떤 부분부터 구현하고 싶으신가요? 🚀 
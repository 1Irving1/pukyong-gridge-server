# 🚀 Pukyong-Gridge-Server

부경대학교 그릿지 인턴십 프로젝트 - Spring Boot 백엔드 서버

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.5-brightgreen)
![Java](https://img.shields.io/badge/Java-11-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![AWS EC2](https://img.shields.io/badge/AWS-EC2-orange)

## 📋 프로젝트 개요

**PortOne(구 아임포트) 결제 게이트웨이**를 연동한 **Spring Boot 기반 백엔드 서버**입니다.  
사용자 관리, 결제 시스템, 구독 서비스를 포함한 RESTful API를 제공합니다.

### ✨ 주요 특징
- 🔐 **JWT 기반 인증 시스템**
- 💳 **PortOne 결제 연동**
- 🏗️ **3-Layer 아키텍처** (Controller-Service-Repository)
- 📚 **Swagger API 문서화**
- ☁️ **AWS EC2 배포**

## 🌐 배포된 서버

### 📡 서버 정보
- **도메인**: http://3.34.48.169:9000
- **Swagger UI**: http://3.34.48.169:9000/swagger-ui.html
- **서버 상태**: 🟢 운영 중

### 🔗 빠른 링크
- [Swagger API 문서](http://3.34.48.169:9000/swagger-ui.html)
- [GitHub Repository](https://github.com/1irving1/pukyong-gridge-server)

## 🛠 기술 스택

### 🖥️ Backend
- **Framework**: Spring Boot 2.7.5
- **Language**: Java 11
- **Build Tool**: Gradle 6.9.3
- **ORM**: Spring Data JPA
- **Documentation**: Swagger/OpenAPI 3.0

### 💾 Database
- **Database**: MySQL 8.0
- **Timezone**: KST (Asia/Seoul)
- **Connection Pool**: HikariCP

### 🔌 External APIs
- **Payment Gateway**: PortOne (포트원)
- **Authentication**: JWT

### ☁️ Infrastructure
- **Cloud Platform**: AWS EC2
- **OS**: Ubuntu 24.04 LTS
- **Server**: Apache Tomcat 9.0

## 📊 ERD (Entity Relationship Diagram)

![ERD Diagram](docs/images/pukyong-gridge-server_ERD.png)

### 🗄️ 주요 테이블
- **users**: 사용자 정보 (암호화된 개인정보 포함)
- **payments**: 결제 정보 (PortOne 연동)
- **subscriptions**: 구독 정보 (월 정기 결제)
- **memo, comment**: 테스트용 1:N 관계 테이블

## 🏗 프로젝트 구조

```
src/main/java/com/example/demo/
├── common/                     # 🔧 공통 설정
│   ├── config/                # 설정 클래스 (Swagger, CORS, RestTemplate)
│   ├── exceptions/            # 글로벌 예외 처리
│   ├── response/              # 공통 응답 포맷
│   └── oauth/                 # OAuth 소셜 로그인
├── src/
│   ├── payment/               # 💳 결제 도메인
│   │   ├── entity/           # Payment 엔티티
│   │   ├── model/            # DTO 클래스들
│   │   ├── PaymentController.java    # REST API
│   │   ├── PaymentService.java       # 비즈니스 로직
│   │   ├── PaymentRepository.java    # 데이터 액세스
│   │   ├── PortOneService.java       # 외부 API 연동
│   │   └── PaymentScheduler.java     # 스케줄 작업
│   ├── subscription/          # 📅 구독 도메인
│   │   ├── entity/           # Subscription 엔티티
│   │   ├── SubscriptionService.java
│   │   └── SubscriptionRepository.java
│   ├── user/                  # 👤 사용자 도메인
│   │   ├── entity/           # User 엔티티
│   │   ├── model/            # DTO 클래스들
│   │   ├── UserController.java       # REST API
│   │   ├── UserService.java          # 비즈니스 로직
│   │   └── UserRepository.java       # 데이터 액세스
│   └── test/                  # 🧪 테스트 도메인
│       ├── entity/           # Memo, Comment 엔티티
│       ├── TestController.java
│       └── TestService.java
└── utils/                     # 🛠️ 유틸리티
    ├── JwtService.java       # JWT 토큰 관리
    ├── SHA256.java           # 비밀번호 해싱
    └── PersonalInfoEncryption.java  # 개인정보 암호화
```

## 🔌 API 엔드포인트

### 👤 사용자 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/app/users` | 회원가입 |
| POST | `/app/users/login` | 로그인 |
| GET | `/app/users` | 사용자 목록 조회 |
| GET | `/app/users/{userId}` | 사용자 정보 조회 |

### 💳 결제 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/payments` | 결제 생성 |
| GET | `/payments/{paymentId}` | 결제 조회 |
| POST | `/payments/verify` | 결제 검증 |
| POST | `/payments/portone/payment` | PortOne 결제창 연동 |

### 🧪 테스트 API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/test/memos` | 메모 목록 조회 |
| POST | `/test/memos` | 메모 생성 |
| POST | `/test/comments` | 댓글 생성 |

## 🚀 로컬 실행 방법

### 1️⃣ 환경 요구사항
- ☕ Java 11 이상
- 🐬 MySQL 8.0
- 🏗️ Gradle 6.9.3 이상

### 2️⃣ 데이터베이스 설정
```sql
CREATE DATABASE admin_db;
```

### 3️⃣ 설정 파일 수정
`src/main/resources/application.yml`에서 DB 비밀번호 설정:
```yaml
spring:
  datasource:
    password: your_mysql_password  # MySQL 비밀번호 입력
```

### 4️⃣ 서버 실행
```bash
# 빌드
./gradlew build

# 실행 (개발 모드)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 5️⃣ API 확인
- 🌐 **Swagger UI**: http://localhost:9000/swagger-ui.html
- 📡 **서버 상태**: http://localhost:9000

## 🔧 주요 기능

### 🔐 보안 기능
- **JWT 인증**: 로그인 시 JWT 토큰 발급
- **개인정보 암호화**: AES-256 암호화 (이름, 이메일, 전화번호)
- **비밀번호 해싱**: SHA-256 해싱
- **입력 검증**: 이메일, 전화번호 정규식 검증

### 💳 결제 시스템
- **PortOne 연동**: 실시간 결제 처리
- **서버측 검증**: 결제 금액 이중 검증
- **결제 상태 관리**: READY → PAID → COMPLETED
- **중복 결제 방지**: merchant_uid 기반

### 📅 구독 관리
- **월 정기 결제**: 9,900원 고정 금액
- **구독 상태 추적**: ACTIVE, EXPIRED, CANCELLED
- **결제 방법 지원**: 카드, 계좌이체, 모바일

### ⏰ 스케줄러
- **결제 검증**: 매일 결제 금액 검증
- **구독 갱신**: 정기 결제 처리 (예정)

## 🧪 테스트 시나리오

### 📝 1. 회원가입 → 로그인
```json
// POST /app/users
{
  "username": "testuser001",
  "name": "테스트사용자",
  "password": "password123",
  "email": "test@example.com",
  "phoneNumber": "010-1234-5678",
  "birthDate": "1990-01-01",
  "termsOfService": true,
  "dataPolicy": true,
  "locationBasedService": true
}
```

### 💳 2. 결제 생성
```json
// POST /payments
{
  "userId": 1,
  "amount": 9900,
  "merchantUid": "order_test_20250829001"
}
```

### 🔍 3. 결제 조회
```http
GET /payments/1
Authorization: Bearer {jwt_token}
```

## 📁 제출 산출물

### ✅ 완료된 요구사항
1. **📡 EC2 서버 배포**: http://3.34.48.169:9000
2. **📋 Postman Collection**: API 테스트 컬렉션 제출
3. **📊 ERD 설계**: 데이터베이스 관계도 작성
4. **📚 Git README**: 프로젝트 문서화

### 📦 제출 파일
- `Pukyong-Gridge-Server.postman_collection.json`
- `docs/images/pukyong-gridge-server_ERD.png`
- `README.md` (현재 파일)

## 🏆 구현된 체크리스트

### ✅ 아키텍처
- [x] **3-Layer 구조**: Controller-Service-Repository
- [x] **Swagger 문서화**: API 명세서 자동 생성
- [x] **한국 표준시**: KST 타임존 설정

### ✅ 에러 처리
- [x] **Try-Catch 구문**: 서버 중단 방지
- [x] **적절한 에러 코드**: 상황별 에러 응답
- [x] **글로벌 예외 처리**: @RestControllerAdvice 적용

### ✅ 데이터 관리
- [x] **개인정보 암호화**: AES-256 암호화 적용
- [x] **소셜 로그인 구분**: OAuth 타입 필드 관리
- [x] **입력 검증**: 정규식 기반 데이터 검증

## 🚧 현재 한계 및 개선 과제

### ⚠️ 한계사항
- **PortOne 테스트 모드**: 실제 결제 미처리
- **기본 기능 위주**: MVP 수준 구현
- **Postman 일부 호환성**: 일부 API는 Swagger 권장

### 🔮 향후 개선 방향
1. **실제 결제 연동**: PortOne 운영 모드 적용
2. **웹훅 구현**: 비동기 결제 결과 처리
3. **고급 기능**: 쿠폰, 할인, 다중 상품 지원
4. **모니터링**: 로그 수집 및 성능 모니터링

## 👨‍💻 개발자 정보

| 항목 | 내용 |
|------|------|
| **이름** | 정상훈 |
| **학번** | 201930326 |
| **소속** | 부경대학교 |
| **프로젝트** | 그릿지 인턴십 |
| **기간** | 2025.08 |

---

<div align="center">

**🎯 부경대학교 그릿지 인턴십 프로젝트**  
*Spring Boot 백엔드 서버 개발*

</div>
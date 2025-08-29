# 🗺️ Simulation SERVER Challenge - 완전한 유저 Map

## 📋 목차
1. [시스템 전체 구조](#시스템-전체-구조)
2. [사용자 플로우 개요](#사용자-플로우-개요)
3. [상세 사용자 여정](#상세-사용자-여정)
4. [백엔드 API 설계](#백엔드-api-설계)
5. [데이터베이스 설계](#데이터베이스-설계)
6. [개발 우선순위](#개발-우선순위)

---

## 🏗️ 시스템 전체 구조

### 전체 아키텍처
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              Simulation SERVER Challenge                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │   프론트엔드     │    │     백엔드       │    │   데이터베이스   │            │
│  │                 │    │                 │    │                 │            │
│  │  📱 Tnovel      │◄──►│  🖥️ Spring Boot │◄──►│  🗄️ MySQL      │            │
│  │  Mobile App     │    │  Java 8         │    │  admin_db       │            │
│  │                 │    │                 │    │                 │            │
│  │  🌐 Admin Panel │    │  🔐 JWT         │    │  📊 Tables      │            │
│  │  Web Interface  │    │  🔒 OAuth2      │    │  📝 Logs        │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│         │                       │                       │                      │
│         ▼                       ▼                       ▼                      │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐            │
│  │   사용자 화면    │    │   API 엔드포인트 │    │   데이터 스키마  │            │
│  │   어드민 화면    │    │   비즈니스 로직 │    │   인덱스/제약조건 │            │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘            │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 기술 스택
```
Frontend:     Mobile App (Tnovel) + Admin Web Panel
Backend:      Spring Boot 2.7.5 + Java 8
Database:     MySQL 8.0
Authentication: JWT + OAuth2 (Google, Kakao)
Documentation: Swagger UI
Testing:      Postman Collection
```

---

## 🎯 사용자 플로우 개요

### 1. 일반 사용자 플로우
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   앱 시작    │───►│  회원가입    │───►│   로그인     │───►│   메인 화면  │
│   화면      │    │   플로우     │    │   플로우     │    │   (피드)    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       ▼                   ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   온보딩     │    │  기본정보    │    │  일반/소셜  │    │   콘텐츠    │
│   화면      │    │   입력      │    │   로그인     │    │   조회      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### 2. 어드민 사용자 플로우
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   어드민     │───►│   대시보드   │───►│   회원관리   │───►│   상세 조회  │
│   로그인     │    │   화면      │    │   화면      │    │   화면      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       ▼                   ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   권한       │    │   통계       │    │   검색/필터 │    │   상태       │
│   확인      │    │   확인      │    │   기능      │    │   변경      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

---

## 🔄 상세 사용자 여정

### A. 신규 사용자 회원가입 여정 (상세)
```
1. 앱 다운로드 및 실행
   ↓
2. 온보딩 화면 (Tnovel 소개)
   ↓
3. 회원가입 선택
   ├─ 일반 회원가입
   │  ↓
   │  4. 기본정보 입력 화면
   │     ├─ 전화번호 입력 (01090010123)
   │     ├─ 이름 입력 (행복한강아지)
   │     ├─ 사용자명 입력 (happypuppy)
   │     └─ 비밀번호 입력 (dog123)
   │     ↓
   │  5. 유효성 검사 (실시간)
   │     ├─ 전화번호 형식 확인
   │     ├─ 사용자명 중복 확인
   │     ├─ 비밀번호 복잡도 확인
   │     └─ 실시간 피드백 (✓/✗)
   │     ↓
   │  6. 생일 선택 화면
   │     ├─ 월 선택 (1-12월)
   │     ├─ 일 선택 (1-31일)
   │     └─ 년도 선택 (1919-2015년)
   │     ↓
   │  7. 이용약관 동의 화면
   │     ├─ 이용약관 (필수)
   │     ├─ 데이터 정책 (필수)
   │     └─ 위치 기반 기능 (필수)
   │     ↓
   │  8. 가입 완료 및 메인 화면 이동
   │
   └─ 카카오 로그인
      ↓
      4. 카카오 인증
      ↓
      5. 기존 회원 확인
      ├─ 기존 회원: 메인 화면 이동
      └─ 신규 회원: 추가정보 입력 (4-8단계)
```

### B. 기존 사용자 로그인 여정 (상세)
```
1. 로그인 화면 접속
   ↓
2. 로그인 방식 선택
   ├─ 일반 로그인
   │  ├─ 사용자명 입력 (happypuppy)
   │  ├─ 비밀번호 입력 (******)
   │  └─ 로그인 버튼 클릭
   │     ↓
   │  ├─ 성공: 메인 화면 이동
   │  ├─ 실패: 오류 메시지 표시
   │  └─ 비밀번호 찾기
   │     ↓
   │  ├─ 전화번호 입력
   │  ├─ 인증 코드 전송
   │  ├─ 새 비밀번호 설정
   │  └─ 로그인 완료
   │
   └─ 카카오 로그인
      ├─ 카카오 인증
      ├─ 기존 회원 확인
      ├─ 신규 회원: 추가정보 입력
      └─ 기존 회원: 메인 화면 이동
```

### C. 어드민 회원 관리 여정 (상세)
```
1. 어드민 로그인
   ↓
2. 대시보드 접속
   ↓
3. 회원관리 메뉴 선택
   ↓
4. 회원 목록 조회
   ├─ 기본: 가입 최신순 정렬
   ├─ 검색 조건 설정
   │  ├─ 사용자 ID 검색
   │  ├─ 사용자 이름 검색
   │  ├─ 회원가입 날짜 검색
   │  └─ 회원 상태 필터
   └─ 조회 결과 표시
   ↓
5. 회원 상세 조회
   ├─ 회원 목록에서 특정 회원 클릭
   ├─ 상세 정보 표시
   │  ├─ 기본 정보
   │  ├─ 가입 정보
   │  ├─ 마지막 로그인 시간
   │  └─ 회원 상태
   └─ 관리 액션
      ├─ 회원 상태 변경 (활성화/정지/탈퇴)
      └─ 변경 이력 로그 기록
```

---

## 🔌 백엔드 API 설계

### 1. 사용자 관련 API
```
POST   /api/signup                    # 회원가입
POST   /api/signup/validation         # 실시간 유효성 검사
POST   /api/signup/terms              # 약관 동의
POST   /api/login                     # 일반 로그인
POST   /api/oauth/kakao               # 카카오 로그인
POST   /api/password/reset            # 비밀번호 재설정
GET    /api/users/profile             # 사용자 프로필 조회
PATCH  /api/users/profile             # 사용자 프로필 수정
DELETE /api/users                     # 회원 탈퇴
```

### 2. 어드민 관련 API
```
# 회원 관리
GET    /api/admin/users               # 회원 목록 조회
GET    /api/admin/users/{id}          # 회원 상세 조회
PATCH  /api/admin/users/{id}/status   # 회원 상태 변경

# 피드 관리
GET    /api/admin/feeds               # 피드 목록 조회
GET    /api/admin/feeds/{id}          # 피드 상세 조회
DELETE /api/admin/feeds/{id}          # 피드 삭제

# 신고 관리
GET    /api/admin/reports             # 신고 목록 조회
GET    /api/admin/reports/{id}        # 신고 상세 조회
PATCH  /api/admin/reports/{id}/status # 신고 처리 상태 변경

# 로그 관리
GET    /api/admin/logs                # 로그 목록 조회
GET    /api/admin/logs/{type}         # 특정 타입 로그 조회
GET    /api/admin/logs/export         # 로그 내보내기
```

### 3. 공통 API
```
GET    /api/health                    # 서버 상태 확인
GET    /api/terms                     # 약관 조회
POST   /api/logout                    # 로그아웃
```

---

## 🗄️ 데이터베이스 설계

### 1. 주요 테이블 구조

#### users 테이블
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE','SUSPENDED','WITHDRAWN') DEFAULT 'ACTIVE',
    registration_date DATE NOT NULL,
    last_login_time DATETIME,
    is_oauth BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### feeds 테이블
```sql
CREATE TABLE feeds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    status ENUM('ACTIVE','DELETED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### reports 테이블
```sql
CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    target_type ENUM('FEED','COMMENT','USER') NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status ENUM('PENDING','PROCESSED','REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id)
);
```

#### activity_logs 테이블
```sql
CREATE TABLE activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 2. 인덱스 설계
```sql
-- users 테이블 인덱스
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_registration_date ON users(registration_date);

-- feeds 테이블 인덱스
CREATE INDEX idx_feeds_user_id ON feeds(user_id);
CREATE INDEX idx_feeds_status ON feeds(status);
CREATE INDEX idx_feeds_created_at ON feeds(created_at);

-- reports 테이블 인덱스
CREATE INDEX idx_reports_reporter_id ON reports(reporter_id);
CREATE INDEX idx_reports_target ON reports(target_type, target_id);
CREATE INDEX idx_reports_status ON reports(status);

-- activity_logs 테이블 인덱스
CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_action_type ON activity_logs(action_type);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at);
```

---

## 🎯 개발 우선순위

### Phase 1: 핵심 기능 (1-2주)
1. **사용자 인증 시스템**
   - 회원가입 API
   - 로그인 API
   - JWT 토큰 관리
   - 비밀번호 암호화

2. **기본 데이터베이스 설계**
   - users 테이블 생성
   - 기본 인덱스 설정
   - 데이터 검증 로직

### Phase 2: 어드민 기능 (2-3주)
1. **어드민 회원 관리**
   - 회원 목록 조회 API
   - 회원 상세 조회 API
   - 회원 상태 변경 API

2. **검색 및 필터링**
   - 다중 조건 검색
   - 페이지네이션
   - 정렬 기능

### Phase 3: 고급 기능 (3-4주)
1. **소셜 로그인**
   - 카카오 OAuth 연동
   - Google OAuth 연동

2. **로깅 시스템**
   - 활동 로그 기록
   - 로그 조회 API
   - 로그 내보내기

### Phase 4: 보안 및 최적화 (1-2주)
1. **보안 강화**
   - 입력값 검증
   - SQL 인젝션 방지
   - XSS 방지

2. **성능 최적화**
   - 쿼리 최적화
   - 캐싱 적용
   - 인덱스 튜닝

---

## 📊 성공 지표

### 기능 완성도
- [ ] 회원가입 플로우 완성
- [ ] 로그인 플로우 완성
- [ ] 어드민 회원 관리 완성
- [ ] 검색 및 필터링 완성
- [ ] 로깅 시스템 완성
- [ ] 소셜 로그인 완성

### 기술적 완성도
- [ ] API 문서화 (Swagger)
- [ ] Postman Collection 작성
- [ ] 에러 처리 완성
- [ ] 보안 검증 완성
- [ ] 성능 테스트 완료

### 사용자 경험
- [ ] 모든 화면 플로우 구현
- [ ] 실시간 유효성 검사
- [ ] 적절한 에러 메시지
- [ ] 반응형 UI/UX

이 완전한 유저 Map을 바탕으로 체계적인 백엔드 개발을 진행할 수 있습니다. 어떤 단계부터 시작하고 싶으신가요? 🚀 
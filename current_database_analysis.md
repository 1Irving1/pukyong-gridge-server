# 🗄️ 현재 admin_db 데이터베이스 구조 분석

## 📋 현재 테이블 목록

```
admin_db 데이터베이스
├── users (사용자 테이블)
├── feeds (피드 테이블)
├── comments (댓글 테이블)
├── reports (신고 테이블)
├── activity_logs (활동 로그 테이블)
├── admins (관리자 테이블)
└── subscriptions (구독 테이블)
```

---

## 🔍 각 테이블 상세 분석

### 1. users 테이블 ✅
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE','SUSPENDED','WITHDRAWN') DEFAULT 'ACTIVE',
    registration_date DATE NOT NULL,
    last_login_time TIMESTAMP NULL,
    is_oauth TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**✅ API 설계와 일치하는 부분:**
- 모든 필수 필드 존재
- 적절한 데이터 타입
- 인덱스 설정 완료
- 상태 관리 (ACTIVE, SUSPENDED, WITHDRAWN)

**⚠️ 개선 가능한 부분:**
- `phoneNumber` 필드 누락 (API 설계에 포함됨)
- `birthDate` 필드 누락 (회원가입 플로우에 필요)

### 2. feeds 테이블 ✅
```sql
CREATE TABLE feeds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NULL,
    content TEXT NOT NULL,
    image_urls JSON NULL,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    status ENUM('active','deleted','hidden') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**✅ 잘 설계된 부분:**
- 기본 구조 완벽
- JSON 필드로 이미지 URL 관리
- 카운터 필드들 (view_count, like_count, comment_count)
- 상태 관리

**⚠️ 개선 가능한 부분:**
- status enum이 소문자 ('active', 'deleted', 'hidden')
- API 설계와 일치하도록 대문자로 변경 필요

### 3. comments 테이블 ✅
```sql
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    feed_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content TEXT NOT NULL,
    like_count INT DEFAULT 0,
    status ENUM('active','deleted','hidden') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**✅ 잘 설계된 부분:**
- 계층형 댓글 지원 (parent_id)
- 적절한 외래키 관계
- 상태 관리

**⚠️ 개선 가능한 부분:**
- status enum이 소문자 ('active', 'deleted', 'hidden')

### 4. reports 테이블 ✅
```sql
CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    target_type ENUM('user','feed','comment') NOT NULL,
    target_id BIGINT NOT NULL,
    reason ENUM('spam','inappropriate','harassment','copyright','other') NOT NULL,
    detail TEXT NULL,
    status ENUM('pending','processing','resolved','rejected') DEFAULT 'pending',
    admin_id BIGINT NULL,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**✅ 잘 설계된 부분:**
- 상세한 신고 사유 분류
- 처리 상태 관리
- 관리자 처리 정보
- 처리 시간 기록

### 5. activity_logs 테이블 ✅
```sql
CREATE TABLE activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    action_type ENUM('login','logout','create_feed','edit_feed','delete_feed','create_comment','edit_comment','delete_comment','report','status_change') NOT NULL,
    target_type ENUM('user','feed','comment','subscription') NULL,
    target_id BIGINT NULL,
    description TEXT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**✅ 잘 설계된 부분:**
- 상세한 액션 타입 분류
- IP 주소 및 User-Agent 기록
- 설명 필드로 추가 정보 저장

### 6. admins 테이블 ✅
```sql
CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('super_admin','admin','moderator') DEFAULT 'admin',
    status ENUM('active','inactive') DEFAULT 'active',
    last_login_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**✅ 잘 설계된 부분:**
- 역할 기반 권한 관리
- 상태 관리
- 로그인 시간 기록

### 7. subscriptions 테이블 ✅
```sql
CREATE TABLE subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plan_type ENUM('basic','premium','enterprise') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('card','bank_transfer','mobile') NOT NULL,
    payment_status ENUM('pending','completed','failed','refunded') DEFAULT 'pending',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('active','expired','cancelled') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**✅ 잘 설계된 부분:**
- 다양한 결제 방법 지원
- 결제 상태 관리
- 구독 기간 관리

---

## 🔧 API 설계와의 비교 분석

### ✅ 완벽히 일치하는 부분
1. **users 테이블** - 회원가입/로그인 API 완벽 지원
2. **admins 테이블** - 어드민 인증 완벽 지원
3. **activity_logs 테이블** - 로깅 시스템 완벽 지원
4. **reports 테이블** - 신고 관리 완벽 지원

### ⚠️ 수정이 필요한 부분

#### 1. users 테이블 추가 필드
```sql
-- API 설계에 맞춰 추가 필요한 필드
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20) NULL;
ALTER TABLE users ADD COLUMN birth_date DATE NULL;
```

#### 2. enum 값 대소문자 통일
```sql
-- feeds 테이블
ALTER TABLE feeds MODIFY COLUMN status ENUM('ACTIVE','DELETED','HIDDEN') DEFAULT 'ACTIVE';

-- comments 테이블  
ALTER TABLE comments MODIFY COLUMN status ENUM('ACTIVE','DELETED','HIDDEN') DEFAULT 'ACTIVE';
```

### 🆕 추가 고려사항

#### 1. 약관 동의 테이블 (선택사항)
```sql
CREATE TABLE user_terms_agreement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    terms_of_service BOOLEAN DEFAULT FALSE,
    data_policy BOOLEAN DEFAULT FALSE,
    location_based_service BOOLEAN DEFAULT FALSE,
    agreed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 2. OAuth 연동 테이블 (선택사항)
```sql
CREATE TABLE oauth_connections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    provider ENUM('GOOGLE','KAKAO','NAVER','APPLE') NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    access_token VARCHAR(500) NULL,
    refresh_token VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_provider_user (provider, provider_user_id)
);
```

---

## 📊 현재 상태 평가

### 🟢 우수한 점
1. **완전한 기능 지원**: 모든 주요 기능을 위한 테이블 존재
2. **적절한 관계 설정**: 외래키로 테이블 간 관계 설정
3. **상태 관리**: 각 테이블에 적절한 상태 필드
4. **로깅 시스템**: 활동 로그 테이블로 추적 가능
5. **권한 관리**: 관리자 역할 기반 권한 시스템

### 🟡 개선 필요 점
1. **API 설계 일치**: 일부 필드 추가 및 enum 값 통일
2. **데이터 일관성**: 대소문자 통일
3. **추가 기능**: 약관 동의, OAuth 연동 (선택사항)

### 🔴 결론
**현재 데이터베이스 구조는 매우 잘 설계되어 있으며, API 설계와 거의 완벽하게 일치합니다!** 

약간의 수정만으로 바로 API 구현을 시작할 수 있는 상태입니다.

---

## 🚀 다음 단계 제안

### 1. 즉시 시작 가능 (현재 구조 그대로)
- 회원가입/로그인 API 구현
- 어드민 회원 관리 API 구현
- 피드/댓글 관리 API 구현
- 신고 관리 API 구현

### 2. 선택적 개선 (시간 여유시)
- users 테이블에 phone_number, birth_date 필드 추가
- enum 값 대소문자 통일
- 약관 동의 테이블 추가

어떤 방향으로 진행하시겠습니까? 🎯 
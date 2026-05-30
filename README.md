# Flown Backend

> 학습자의 성장을 시각화하는 LMS 백엔드 서버

## 프로젝트 소개

**Flown**은 학습자가 자신의 학습 활동을 시각적으로 확인하고, 성취감과 동기부여를 얻을 수 있도록 돕는 성장형 LMS 플랫폼입니다.

본 저장소는 **Hard-Click 팀**의 백엔드 서버로, REST API 기반으로 프론트엔드와 통신하며 인증/인가, 강의, 수강, 학습 활동, 커뮤니티, 리뷰, 공지 등의 핵심 기능을 제공합니다.

## 프로젝트 목표

Flown Backend는 단순한 기능 구현이 아니라, 향후 3개월 동안 기능이 확장될 수 있는 LMS 서비스를 목표로 설계되었습니다.

이를 위해 다음 목표를 중심으로 개발했습니다.

- 도메인별 책임 분리
- 유지보수 가능한 백엔드 구조 설계
- FE/BE 간 API 계약 명확화
- 인증/인가 및 예외 처리 일관성 확보
- Swagger와 Postman 기반 API 검증

## 팀 정보

| 역할 | 담당자 |
|---|---|
| PM | 윤종호, 이태연 |
| PL | 유강현 |
| 형상관리자 | 윤종호, 이태연 |
| DBA | 박종준 |

## 백엔드 담당 기능

| 팀원 | 담당 기능 |
|---|---|
| 박종준 | 강의 도메인 |
| 이태연 | 마이페이지, 학습 활동, 커뮤니티, 리뷰, 공지 |

## Repository

[Hard-Click Backend Repository](https://github.com/Hard-Click/Hard-Click-BackEnd)

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot |
| Security | Spring Security, JWT |
| ORM | Spring Data JPA, Hibernate |
| Database | MySQL |
| Build Tool | Gradle |
| API Docs | Swagger / OpenAPI |
| API Test | Postman |
| Version Control | Git, GitHub |

## 아키텍처

본 프로젝트는 유지보수성과 확장성을 고려하여 다음 구조를 기반으로 설계했습니다.

- Clean Architecture
- DDD, Domain-Driven Design
- Bounded Context
- Port & Adapter Pattern

### 패키지 구조

```text
backend
└── domain
    ├── community
    ├── course
    ├── enrollment_management
    ├── evolution_report
    ├── identity
    ├── learning_activity
    ├── notice
    ├── payment
    ├── report_moderation
    └── subject
```

### 계층 구조

```text
domain
├── presentation
│   └── api
│       ├── request
│       └── response
├── application
│   ├── command
│   └── usecase
├── domain
│   └── model
└── infrastructure
    └── persistence
```

### 계층별 역할

| Layer | 역할 |
|---|---|
| Presentation | Controller, Request/Response DTO, API 진입점 |
| Application | UseCase, Command, 트랜잭션 처리 |
| Domain | 핵심 비즈니스 규칙, 도메인 모델 |
| Infrastructure | JPA, DB 접근, 외부 기술 연동 구현 |

## 아키텍처 선택 이유

### Clean Architecture

비즈니스 로직과 기술 구현을 분리하기 위해 Clean Architecture를 적용했습니다.

DB, JPA, 외부 기술이 변경되더라도 핵심 도메인 로직에 영향을 최소화할 수 있도록 설계했습니다.

### DDD / Bounded Context

회원, 강의, 수강, 커뮤니티, 학습 활동 등 각 도메인의 책임을 명확히 분리하기 위해 DDD와 Bounded Context를 적용했습니다.

이를 통해 기능 간 결합도를 낮추고 유지보수성을 높이고자 했습니다.

### Port & Adapter Pattern

도메인이 JPA나 DB 같은 외부 기술에 직접 의존하지 않도록 Port & Adapter 구조를 사용했습니다.

이를 통해 도메인 로직을 독립적으로 유지하고, 외부 구현체가 바뀌더라도 핵심 로직이 흔들리지 않도록 했습니다.

## ERD

[ERDCloud 바로가기](https://www.erdcloud.com/d/omNHgysd3MrFNrrPM)

## 주요 기능

### 인증 / 인가

- 회원가입
- 로그인 / 로그아웃
- JWT 기반 Access Token 발급
- Refresh Token 기반 Access Token 재발급
- Spring Security 기반 권한 검증

### 이메일 인증

- 회원가입 이메일 인증번호 발송
- 이메일 인증번호 검증
- 이메일 인증 토큰 발급

### 비밀번호 재설정

- 비밀번호 재설정 인증번호 발송
- 인증번호 검증
- 비밀번호 변경 토큰 발급
- 비밀번호 재설정

### 계정 잠금

- 로그인 실패 5회 시 계정 잠금
- 잠긴 계정 이메일 인증
- 인증 후 비밀번호 변경
- 계정 잠금 해제

### 강의

- 강의 등록
- 강의 목록 조회
- 강의 상세 조회
- 강의 수정
- 강의 삭제
- 강의 공개 / 비공개 상태 관리

### 학습 활동

- 학습 활동 조회
- 수강 상태 확인
- 학습 기록 기반 통계 확장 예정

### 커뮤니티 / 리뷰 / 공지

- 커뮤니티 게시글 작성 / 조회 / 수정 / 삭제
- 댓글 작성 / 조회 / 수정 / 삭제
- 강의 리뷰 작성 / 조회 / 수정 / 삭제
- 공지사항 작성 / 조회 / 수정 / 삭제

## 주요 흐름

### 인증 흐름

```text
로그인 요청
→ 사용자 검증
→ JWT 발급
→ 인증 필요한 API 호출
→ JWT 검증
→ 요청 처리
```

### 이메일 인증 흐름

```text
이메일 발송
→ 인증 코드 입력
→ 코드 검증
→ 인증 토큰 발급
```

### 계정 잠금 해제 흐름

```text
로그인 5회 실패
→ 계정 잠금
→ 이메일 인증번호 발송
→ 인증번호 검증
→ 비밀번호 변경
→ 계정 잠금 해제
```

## REST API

상세한 Request/Response는 Swagger에서 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui/index.html
```

### Auth / Member

| Method | URL | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/signup` | 회원가입 | 비회원 |
| POST | `/api/auth/login` | 로그인 | 비회원 |
| POST | `/api/auth/logout` | 로그아웃 | 회원 |
| POST | `/api/auth/refresh` | Access Token 재발급 | 회원 |
| GET | `/api/auth/check-username` | 아이디 중복 확인 | 비회원 |
| GET | `/api/auth/check-email` | 이메일 중복 확인 | 비회원 |
| GET | `/api/members/me` | 내 프로필 조회 | 회원 |
| PATCH | `/api/members/me/password` | 비밀번호 변경 | 회원 |
| PATCH | `/api/members/me/profile-image` | 프로필 이미지 수정 | 회원 |
| DELETE | `/api/members/me` | 회원 탈퇴 | 회원 |

### Email / Password / Account Lock

| Method | URL | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/email/send` | 회원가입 이메일 인증번호 발송 | 비회원 |
| POST | `/api/auth/email/verify` | 회원가입 이메일 인증번호 검증 | 비회원 |
| POST | `/api/auth/password-reset/email` | 비밀번호 재설정 인증번호 발송 | 비회원 |
| POST | `/api/auth/password-reset/verify` | 비밀번호 재설정 인증번호 검증 | 비회원 |
| PATCH | `/api/auth/password-reset` | 비밀번호 재설정 | 비회원 |
| POST | `/api/auth/account-locks/email` | 계정 잠금 인증번호 발송 | 비회원 |
| POST | `/api/auth/account-locks/verify` | 계정 잠금 인증번호 검증 | 비회원 |
| PATCH | `/api/auth/account-locks/password` | 비밀번호 변경 및 계정 잠금 해제 | 비회원 |

### Course

| Method | URL | Description | Auth |
|---|---|---|---|
| POST | `/api/courses` | 강의 등록 | 강사 |
| GET | `/api/courses` | 강의 목록 조회 | 비회원 |
| GET | `/api/courses/{courseId}` | 강의 상세 조회 | 비회원 |
| PATCH | `/api/courses/{courseId}` | 강의 수정 | 강사 |
| DELETE | `/api/courses/{courseId}` | 강의 삭제 | 강사 |
| PATCH | `/api/courses/{courseId}/status` | 강의 공개 / 비공개 상태 변경 | 강사, 관리자 |
| GET | `/api/instructor/courses` | 강사 내 강의 목록 조회 | 강사 |

### Community / Review / Notice

| Method | URL | Description | Auth |
|---|---|---|---|
| POST | `/api/posts` | 커뮤니티 게시글 작성 | 회원 |
| GET | `/api/posts/{postId}` | 게시글 상세 조회 | 회원 |
| PATCH | `/api/posts/{postId}` | 게시글 수정 | 회원 |
| DELETE | `/api/posts/{postId}` | 게시글 삭제 | 회원 |
| POST | `/api/comments` | 댓글 / 대댓글 작성 | 회원 |
| GET | `/api/posts/{postId}/comments` | 댓글 목록 조회 | 회원 |
| PATCH | `/api/comments/{commentId}` | 댓글 수정 | 회원 |
| DELETE | `/api/comments/{commentId}` | 댓글 삭제 | 회원 |
| POST | `/api/courses/{courseId}/reviews` | 수강 리뷰 작성 | 수강생 |
| GET | `/api/courses/{courseId}/reviews` | 강의 리뷰 목록 조회 | 회원 |
| PATCH | `/api/courses/{courseId}/reviews/{reviewId}` | 리뷰 수정 | 수강생 |
| DELETE | `/api/courses/{courseId}/reviews/{reviewId}` | 리뷰 삭제 | 수강생 |
| POST | `/api/notices` | 공지사항 작성 | 관리자 |
| GET | `/api/notices` | 공지사항 목록 조회 | 회원 |
| GET | `/api/notices/{noticeId}` | 공지사항 상세 조회 | 회원 |
| PATCH | `/api/notices/{noticeId}` | 공지사항 수정 | 관리자, 강사 |
| DELETE | `/api/notices/{noticeId}` | 공지사항 삭제 | 관리자, 강사 |

## API 응답 형식

API 응답은 공통 응답 형식을 사용합니다.

```json
{
  "httpStatus": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

예외 상황에서는 `BusinessException`과 `ErrorCode`를 기반으로 일관된 에러 응답을 반환합니다.

```json
{
  "httpStatus": 400,
  "message": "잘못된 요청입니다.",
  "data": null
}
```

## 실행 방법

```bash
./gradlew clean build
./gradlew bootRun
```

또는 테스트를 제외하고 빌드 후 실행할 수 있습니다.

```bash
./gradlew clean build -x test
java -jar build/libs/*.jar
```

## 환경 설정

프로젝트 실행을 위해 DB, JWT, Mail 관련 설정이 필요합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/DB_NAME
    username: DB_USERNAME
    password: DB_PASSWORD

jwt:
  secret: JWT_SECRET
  access-expiration: JWT_ACCESS_EXPIRATION
  refresh-expiration: JWT_REFRESH_EXPIRATION

mail:
  host: MAIL_HOST
  port: MAIL_PORT
  username: MAIL_USERNAME
  password: MAIL_PASSWORD
```

## FE/BE 협업 및 API 계약 관리

프론트엔드와 백엔드는 하나의 제품을 만들기 위해 API 계약을 기준으로 개발했습니다.

API 명세와 검증은 다음 도구를 사용했습니다.

- Notion RestAPI 설계 문서
- Swagger
- Postman
- Slack 단톡방

개발 과정에서 UI 요구사항과 데이터 구조가 변경되면서 요청값, 응답값, URL, enum, 문자 타입 등이 바뀌는 경우가 있었습니다.

이를 해결하기 위해 다음 방식으로 협업 프로세스를 개선했습니다.

- UI에 API 관련 코멘트 작성
- 요청값 / 응답값 / enum / URL 명시
- 변경 사항은 PO가 정리하여 전체 공유
- 확인한 팀원은 체크 표시
- Swagger와 Postman으로 실제 동작 검증
- FE/BE가 크로스 체크하여 불일치 여부 확인

## 통합 검증

백엔드 팀은 Postman을 사용하여 API를 검증했습니다.

검증한 요청은 성공, 실패, 예외 처리 케이스별로 저장하고 팀원끼리 공유했습니다.

### 검증한 케이스

- 회원가입 성공
- 이메일 인증번호 발송
- 인증번호 검증
- 로그인 성공
- 로그인 실패
- 로그인 5회 실패 후 계정 잠금
- 계정 잠금 인증번호 발송
- 계정 잠금 인증번호 검증
- 비밀번호 변경 후 잠금 해제
- 권한이 필요한 API 접근
- 잘못된 요청값에 대한 예외 응답

## 트러블슈팅

### 1. FE가 필요한 데이터와 BE 응답 필드가 맞지 않는 문제

#### 문제 상황

프론트엔드 화면에서 필요한 데이터와 백엔드 API 응답 필드가 일치하지 않는 문제가 발생했습니다.

UI가 변경되거나 필요한 데이터가 추가될 때 FE와 BE가 서로 변경 사항을 즉시 인지하지 못하는 경우가 있었습니다.

#### 원인

초기 기획 단계에서 API 명세를 정했지만, 실제 개발 과정에서 화면 요구사항과 데이터 구조가 계속 변경되었습니다.

또한 Notion, Swagger, Postman, Slack 등 여러 채널을 통해 소통하다 보니 변경 사항이 분산되고 일부 누락되는 문제가 있었습니다.

#### 해결 방법

FE와 BE는 UI 기준으로 필요한 API와 데이터 필드를 다시 정리했습니다.

- UI에 API 코멘트 작성
- 요청값 / 응답값 / enum / URL 명시
- 변경 사항은 PO가 정리하여 전체 공유
- 확인한 팀원은 체크 표시
- Swagger와 Postman으로 동작 검증

#### 결과

FE와 BE가 같은 API 계약을 기준으로 개발할 수 있게 되었고, 요청/응답 불일치 문제를 더 빠르게 발견할 수 있었습니다.

#### 한계

여러 명이 동시에 개발하기 때문에 모든 변경 사항을 즉시 파악하기는 어렵습니다.

향후에는 API 변경 이력 관리와 계약 테스트를 더 강화할 필요가 있습니다.

### 2. Swagger / Notion 명세와 실제 API 경로 불일치 문제

#### 문제 상황

Notion에 작성된 RestAPI 설계 문서와 실제 백엔드 구현 경로가 일부 맞지 않는 문제가 발생했습니다.

프론트엔드가 문서를 기준으로 API를 호출했을 때 실제 백엔드 경로와 달라 연동 문제가 생길 수 있었습니다.

#### 원인

개발 중 API URL이나 요청/응답 구조가 변경되었지만, 문서가 즉시 최신화되지 않았기 때문입니다.

#### 해결 방법

Swagger와 Postman을 기준으로 실제 동작하는 API를 검증하고, Notion API 문서도 함께 갱신하는 방식으로 개선했습니다.

#### 결과

문서와 실제 구현 사이의 차이를 줄였고, FE/BE 연동 시 혼동을 줄일 수 있었습니다.

#### 한계

API 변경이 발생할 때마다 문서와 Swagger, Postman을 모두 최신 상태로 유지해야 하므로 지속적인 관리가 필요합니다.

### 3. 계정 잠금 이메일 인증 API 접근 문제

#### 문제 상황

계정 잠금 상태의 사용자는 정상 로그인 상태가 아니므로 `/api/auth/account-locks/email` API에 접근할 수 있어야 했습니다.

하지만 인증이 필요한 API로 처리될 경우 잠긴 사용자가 인증번호를 발급받지 못하는 문제가 발생할 수 있었습니다.

#### 해결 방법

Spring Security 설정에서 계정 잠금 인증 관련 API를 `permitAll`로 등록했습니다.

```java
.requestMatchers(
    "/api/auth/account-locks/email",
    "/api/auth/account-locks/verify",
    "/api/auth/account-locks/password"
).permitAll()
```

#### 결과

잠긴 계정도 이메일 인증번호를 발급받고, 인증 후 비밀번호를 변경하여 계정 잠금을 해제할 수 있게 되었습니다.

## 향후 개선 방향

### 기능 확장

향후 프로젝트 기간 동안 다음 기능을 확장할 예정입니다.

- 랭킹 기능
- 채팅방
- 알림
- 관리자 기능 고도화
- 잔디표 기반 학습량 시각화
- 학습 통계 고도화

### 협업 개선

API 변경 사항을 더 안정적으로 관리하기 위해 다음 방식을 개선할 수 있습니다.

- API 변경 이력 관리
- 계약 테스트 도입
- Swagger / Notion / Postman 동기화 기준 강화
- 변경 사항 공유 채널 단일화
- FE/BE 통합 검증 체크리스트 작성

### 구조적 개선

3개월 동안 기능이 계속 확장될 예정이므로, Clean Architecture와 DDD 기반 구조를 유지하면서 도메인별 책임을 더 명확히 분리할 예정입니다.

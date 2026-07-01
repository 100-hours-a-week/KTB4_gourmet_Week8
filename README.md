# KTB4_gourmet_Week8

---

## 과제 개요

이번 주차 과제에서는 기존 커뮤니티 프로젝트에 **Spring Security 기반 인증/인가 구조**를 적용하고, **JWT를 이용한 로그인 상태 유지 방식**을 구현하였다. 또한 **유저 도메인에 대한 JUnit 테스트 코드**를 작성하여 유저 관련 핵심 동작을 검증하였다.

---

## 주요 구현 내용

### 1. Spring Security 적용

Spring Security를 적용하여 API별 접근 권한을 설정하였다.

#### 공개 API

- 게시글 목록 조회
- 게시글 상세 조회
- 댓글 목록 조회
- 댓글 상세 조회
- 회원가입
- 로그인
- 토큰 재발급
- 로그아웃
- H2 Console
- 업로드 이미지 조회

#### 인증 필요 API

- 회원정보 조회
- 회원정보 수정
- 비밀번호 수정
- 회원 탈퇴
- 게시글 작성
- 게시글 수정
- 게시글 삭제
- 댓글 작성
- 댓글 수정
- 댓글 삭제
- 좋아요 조회
- 좋아요 토글

JWT 기반 인증 방식을 사용하기 위해 서버 세션을 사용하지 않는 구조로 설정하였다.

```java
.sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

---

### 2. JWT 인증 구현

로그인 성공 시 **AccessToken**과 **RefreshToken**을 발급하도록 구현하였다.

#### AccessToken

- 로그인 응답 body로 전달
- 프론트엔드 localStorage에 저장
- 인증이 필요한 요청의 `Authorization` 헤더에 사용

#### RefreshToken

- HttpOnly Cookie로 전달
- DB의 `refresh_tokens` 테이블에 저장
- 토큰 재발급 및 로그아웃에 사용

로그인 응답 예시는 다음과 같다.

```json
{
  "user": {
    "id": 1,
    "email": "test@test.com",
    "nickname": "tester",
    "profileImage": "/uploads/profile/..."
  },
  "token": {
    "accessToken": "jwt-access-token",
    "expiresIn": 1800000
  }
}
```

---

### 3. JWT 관련 클래스

| 클래스 | 역할 |
| --- | --- |
| `JwtProperties` | `application.yml`의 JWT 설정값 관리 |
| `JwtProvider` | AccessToken, RefreshToken 생성 및 검증 |
| `JwtAuthenticationFilter` | 요청의 `Authorization` 헤더에서 AccessToken 검증 |
| `RefreshToken` | RefreshToken 저장 엔티티 |
| `RefreshTokenRepository` | RefreshToken 조회 및 관리 |

---

### 4. JWT 인증 필터 구현

`JwtAuthenticationFilter`는 `OncePerRequestFilter`를 상속하여 구현하였다.

요청에 다음과 같은 헤더가 포함되어 있으면 AccessToken을 검증한다.

```http
Authorization: Bearer {accessToken}
```

토큰이 유효하면 토큰에서 `userId`를 추출하고, DB에서 사용자를 조회한 뒤 `SecurityContextHolder`에 인증 정보를 저장한다.

잘못된 토큰이거나 탈퇴한 사용자의 토큰인 경우에는 401 응답을 반환하도록 처리하였다.

---

### 5. RefreshToken 관리

RefreshToken은 DB에 저장하고, 사용자당 하나만 유지하도록 구성하였다.

처음에는 로그인 시 기존 RefreshToken을 삭제하고 새로 저장하는 방식으로 구현하였다. 하지만 기존 RefreshToken row가 남아 있을 경우 `UNIQUE(user_id)` 제약 조건 충돌이 발생할 수 있었다.

이를 해결하기 위해 기존 RefreshToken이 있으면 새로 INSERT하지 않고, token 값과 만료 시간을 갱신하도록 수정하였다.

```text
기존 RefreshToken 있음
→ token, expiresAt 갱신

기존 RefreshToken 없음
→ 새 RefreshToken 저장
```

---

### 6. 로그아웃 및 토큰 재발급 구현

#### 로그아웃

```http
POST /users/logout
```

로그아웃 시 DB에 저장된 RefreshToken을 삭제하고, 브라우저의 RefreshToken 쿠키를 만료시킨다.

#### 토큰 재발급

```http
POST /users/token/refresh
```

RefreshToken이 유효하면 새로운 AccessToken과 RefreshToken을 발급한다. 새 RefreshToken은 기존 row를 갱신하는 방식으로 저장한다.

---

### 7. 프론트엔드 JWT 연동

백엔드에 JWT 인증이 적용되면서, 인증이 필요한 API 요청에는 AccessToken을 포함해야 했다.

이를 위해 프론트엔드의 `api.js`에 `apiFetch()` 공통 요청 함수를 작성하였다.

`apiFetch()`는 localStorage에 저장된 AccessToken이 있으면 자동으로 `Authorization` 헤더를 추가한다. 또한 RefreshToken 쿠키 전달을 위해 `credentials: "include"`를 공통으로 적용하였다.

```javascript
headers.set("Authorization", `Bearer ${accessToken}`);
```

#### 적용한 기능

- 회원정보 조회
- 회원정보 수정
- 비밀번호 수정
- 게시글 작성
- 게시글 수정
- 게시글 삭제
- 댓글 작성
- 댓글 수정
- 댓글 삭제
- 좋아요 조회
- 좋아요 토글
- 로그아웃

---

## 유저 도메인 테스트

JUnit5를 사용하여 `User` 도메인 테스트 코드를 작성하였다.

테스트 대상은 `User` 엔티티의 핵심 동작으로 잡았다.

| 테스트 | 검증 내용 |
| --- | --- |
| 회원 객체 생성 테스트 | email, password, nickname, profileImage 값 저장 확인 |
| 닉네임 수정 테스트 | nickname 변경 확인 |
| 프로필 이미지 수정 테스트 | profileImage 변경 확인 |
| 비밀번호 수정 테스트 | password 변경 확인 |
| 회원 탈퇴 테스트 | deletedAt 기록 및 탈퇴 회원 상태 값 변경 확인 |

---

## 테스트 실행 결과

총 5개의 유저 도메인 테스트가 모두 통과하였다.

```text
유저 도메인 테스트
✓ 회원 객체를 생성하면 이메일, 비밀번호, 닉네임, 프로필 이미지가 저장된다
✓ 회원 닉네임을 수정할 수 있다
✓ 회원 프로필 이미지를 수정할 수 있다
✓ 회원 비밀번호를 수정할 수 있다
✓ 회원 탈퇴 시 삭제 시간이 기록되고 개인정보가 탈퇴 회원 상태로 변경된다
```

---

## 실행 환경

- Java 21
- Spring Boot 4.0.6
- Spring Security
- Spring Data JPA
- H2 Database
- JWT
- JUnit5
- Vanilla JavaScript

---

## 최종 결과

이번 주차 과제를 통해 다음 내용을 구현하였다.

- Spring Security 기반 인증/인가 설정
- JWT AccessToken / RefreshToken 발급
- JWT 인증 필터 구현
- RefreshToken DB 저장 및 갱신
- 로그아웃 API 구현
- 토큰 재발급 API 구현
- 프론트엔드 JWT 요청 연결
- 유저 도메인 JUnit 테스트 코드 작성
- 유저 도메인 테스트 5개 통과

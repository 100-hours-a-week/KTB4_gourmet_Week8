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

- 회원 목록 조회
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

- HttpOnly Cookie로 전달
- 인증이 필요한 요청에서 쿠키로 자동 전송
- `JwtAuthenticationFilter`에서 쿠키 값을 읽어 검증
- 서버 DB에는 저장하지 않음

#### RefreshToken

- HttpOnly Cookie로 전달
- DB의 `refresh_tokens` 테이블에 저장
- 토큰 재발급 및 로그아웃에 사용

로그인 응답 body에는 사용자 정보를 반환하고, AccessToken과 RefreshToken은 HttpOnly Cookie로 전달하도록 구성하였다.

```json
{
  "user": {
    "id": 1,
    "email": "test@test.com",
    "nickname": "tester",
    "profileImage": "/uploads/profile/..."
  }
}
```

---

### 3. JWT 관련 클래스

| 클래스 | 역할 |
| --- | --- |
| `JwtProperties` | `application.yml`의 JWT 설정값 관리 |
| `JwtProvider` | AccessToken, RefreshToken 생성 및 검증 |
| `JwtAuthenticationFilter` | 요청 쿠키의 AccessToken 검증 |
| `RefreshToken` | RefreshToken 저장 엔티티 |
| `RefreshTokenRepository` | RefreshToken 조회 및 관리 |

---

### 4. JWT 인증 필터 구현

`JwtAuthenticationFilter`는 `OncePerRequestFilter`를 상속하여 구현하였다.

요청에 `accessToken` 쿠키가 포함되어 있으면 해당 토큰을 검증한다.

```text
Cookie: accessToken={accessToken}
```

토큰이 유효하면 토큰에서 `userId`를 추출하고, DB에서 사용자를 조회한 뒤 `SecurityContextHolder`에 인증 정보를 저장한다.

잘못된 토큰이거나 탈퇴한 사용자의 토큰인 경우에는 401 응답을 반환하도록 처리하였다.

---

### 5. RefreshToken 관리

RefreshToken은 DB에 저장하고, 사용자당 하나만 유지하도록 구성하였다.

기존 RefreshToken이 있으면 새로 INSERT하지 않고, token 값과 만료 시간을 갱신하도록 구현하였다.

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

로그아웃 시 DB에 저장된 RefreshToken을 삭제하고, 브라우저의 AccessToken 쿠키와 RefreshToken 쿠키를 모두 만료시킨다.

#### 토큰 재발급

```http
POST /users/token/refresh
```

RefreshToken이 유효하면 새로운 AccessToken과 RefreshToken을 발급한다. 새 AccessToken과 RefreshToken은 HttpOnly Cookie로 다시 전달하고, RefreshToken은 기존 row를 갱신하는 방식으로 저장한다.

---

### 7. 프론트엔드 JWT 연동

프론트엔드에서는 `api.js`에 `apiFetch()` 공통 요청 함수를 작성하였다.

`apiFetch()`는 모든 API 요청에 `credentials: "include"`를 적용하여 HttpOnly Cookie에 저장된 AccessToken과 RefreshToken이 요청에 포함되도록 처리하였다.

```javascript
credentials: "include"
```

또한 AccessToken이 만료되거나 삭제되어 401 응답이 발생하면, RefreshToken 쿠키를 이용해 토큰 재발급 API를 호출하고 원래 요청을 한 번 다시 시도하도록 구현하였다.

localStorage에는 AccessToken을 저장하지 않고, 화면 표시를 위한 사용자 정보만 저장하였다.

```text
localStorage 저장 값
- userId
- email
- nickname
- profileImage
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

## 최종 결과

이번 주차 과제를 통해 다음 내용을 구현하였다.

- Spring Security 기반 인증/인가 설정
- JWT AccessToken / RefreshToken 발급
- AccessToken HttpOnly Cookie 저장
- RefreshToken HttpOnly Cookie 저장 및 DB 관리
- JWT 인증 필터 구현
- RefreshToken DB 저장 및 갱신
- 로그아웃 API 구현
- 토큰 재발급 API 구현
- AccessToken 만료 시 자동 재발급 및 원래 요청 재시도
- 프론트엔드 쿠키 기반 JWT 요청 연결
- 유저 도메인 JUnit 테스트 코드 작성
- 유저 도메인 테스트 5개 통과

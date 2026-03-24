# 2026-03-24 Auth Service Open Items

## 목적

이 문서는 gateway 연동 기준 중 현재 코드로 확정된 사항과, 별도 회신 또는 추가 구현이 필요한 사항을 분리해서 정리한 내부 확인용 문서입니다.

## 현재 코드로 확인된 사항

### 1. `/auth/internal/session/validate` 응답 형태

현재 구현은 아래 JSON body를 직접 반환합니다.

- `authenticated`
- `userId`
- `role`
- `sessionId`

비인증 시:

- HTTP `401`
- body는 동일 필드 구조를 유지하며 빈 문자열과 `authenticated=false` 를 사용

확인 근거:

- `SsoResponse.InternalSessionValidationResponse`
- `SsoAuthService.validateInternalSession`

### 2. `/auth/me` 인증 방식

현재 구현은 `Authorization` 헤더가 아니라 `sso_session` 쿠키를 사용합니다.

동작:

- 세션 쿠키 존재 + 저장소 세션 유효 -> 사용자 정보 반환
- 세션 쿠키 없음 또는 만료 -> `401 NEED_LOGIN`
- `page=admin` 이고 IP 정책 위반 -> `403 FORBIDDEN`

### 3. 원본 IP 판별 기준

현재 admin SSO IP 검사는 아래 순서로 동작합니다.

1. `X-Forwarded-For` 첫 번째 값
2. 없으면 `request.getRemoteAddr()`

즉, gateway가 유지해야 하는 source of truth는 현재 구현상 `X-Forwarded-For` 첫 번째 값입니다.

### 4. 운영 SSO 쿠키 기본값

현재 운영 기본값:

- `sso_session`
  - name: `sso_session`
  - secure: `true`
  - httpOnly: `true`
  - sameSite: `None`
  - path: `/`
- `sso_oauth_state`
  - name: `sso_oauth_state`
  - secure: 세션 쿠키 설정 따름
  - httpOnly: `true`
  - sameSite: 세션 쿠키 설정 따름
  - path: `/`

### 5. 오류 응답 포맷

현재 예외 기반 오류는 `GlobalResponse` 로 내려갑니다.

대상:

- `GlobalException`
- `AuthException`
- `IllegalArgumentException`

예외:

- `/auth/internal/session/validate` 의 비인증 분기는 예외가 아니라 controller/service에서 raw JSON response를 직접 반환합니다.

## 추가 회신이 필요한 사항

### 1. `validate` 응답 계약 고정 여부

현재 코드상 응답 필드는 확인되지만, 아래는 아직 계약 확정이 필요합니다.

- 필드명 변경 가능성 여부
- header fallback 필요 여부
- 비인증 시 schema 고정 여부

### 2. refresh cookie 실제 계약

현재 설정 파일 기준 기본값은 아래입니다.

- cookie name: `refresh_token`
- secure: `true`
- httpOnly: `true`
- sameSite: `Lax`
- path: `/`

하지만 실제 최종 발급/삭제 형식은 외부 auth 라이브러리 동작까지 포함해 확인이 필요합니다.

### 3. callback / origin 운영 실제값

현재 운영 프로필은 아래 항목을 환경변수로 받습니다.

- `SSO_EXPLAIN_ORIGIN`
- `SSO_EDITOR_ORIGIN`
- `SSO_ADMIN_ORIGIN`
- `SSO_EXPLAIN_CALLBACK_URI`
- `SSO_EDITOR_CALLBACK_URI`
- `SSO_ADMIN_CALLBACK_URI`

저장소만으로는 환경별 실제 등록값을 확정할 수 없습니다.

### 4. `auth/me` 와 `validate`의 대외 계약 수준

현재 구현은 존재하지만, gateway가 장기 의존해도 되는 공식 계약인지 별도 확인이 필요합니다.

확인 대상:

- `sso_session` 쿠키 기반 인증 방식 유지 여부
- `validate` endpoint schema 유지 여부
- `401` / `403` 응답 규칙 유지 여부

## 수정이 필요한지 검토할 항목

### 1. `validate` 응답 표준화

현재 `/auth/internal/session/validate` 는 비인증 시 `GlobalResponse` 가 아니라 raw JSON schema를 반환합니다.

선택지:

- 현행 유지: gateway가 현재 schema에 직접 의존
- 표준화: 모든 실패 응답을 `GlobalResponse` 로 통일

현재 gateway가 이미 body schema에 의존 중이면, 무작정 바꾸면 연동이 깨질 수 있습니다.

### 2. 계약 문서화

아래는 코드에는 있지만 별도 API 계약 문서가 없습니다.

- `validate` response schema
- `sso_session` cookie contract
- callback URI validation rule

이 부분은 문서로 먼저 고정한 뒤 구현을 맞추는 편이 안전합니다.

## 결론

gateway 연동의 핵심 경로와 기본 동작은 현재 코드와 대체로 일치합니다.
다만 `validate` 응답 계약, refresh cookie 최종 계약, 운영 callback 값은 별도 확정이 필요합니다.

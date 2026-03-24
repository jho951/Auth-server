# 2026-03-24 Auth Service Team Request

## 목적

이 문서는 현재 `api-gateway` 반영 상태를 기준으로, `auth-service` 팀과 추가로 맞춰야 하는 사항을 정리한 요청 문서입니다.

게이트웨이는 현재 아래 구조로 동작합니다.

- 외부 인증 진입점은 `/auth/**`, `/oauth2/**`, `/login/oauth2/**`
- auth 관련 경로는 rewrite 없이 그대로 프록시
- `Set-Cookie`, `Cookie`, `Location`, `302`, `204` 응답은 그대로 전달
- 보호 경로의 gateway 인증은 `POST /auth/internal/session/validate` 결과에 의존

즉, 게이트웨이는 라우팅과 전달을 담당하고, 실제 세션/토큰 인증 결과의 source of truth는 `auth-service`가 갖는 구조입니다.

## 현재 Gateway 반영 상태

현재 gateway는 아래를 이미 반영했습니다.

- `POST /auth/login` -> `auth-service`
- `POST /auth/refresh` -> `auth-service`
- `GET /auth/sso/start` -> `auth-service`
- `GET /auth/oauth2/authorize/{provider}` -> `auth-service`
- `POST /auth/exchange` -> `auth-service`
- `POST /auth/internal/session/validate` -> `auth-service`
- `GET /auth/me` -> `auth-service`
- `POST /auth/logout` -> `auth-service`
- `GET /oauth2/**` -> `auth-service`
- `GET /login/oauth2/**` -> `auth-service`
- query string 원본 전달
- request/response body 재구성 없음
- `Set-Cookie`, `Cookie`, `Location` 보존
- `302`, `204` 응답 변형 없음
- `X-Forwarded-For`, `X-Forwarded-Proto` 보존 우선

## Auth Service 팀에 확인이 필요한 항목

### 1. `/auth/internal/session/validate` 응답 계약

게이트웨이는 현재 `POST /auth/internal/session/validate` 를 호출해 인증 결과를 판별합니다.

현재 gateway가 기대하는 최소 응답 정보는 아래입니다.

- `authenticated`
- `userId`
- `role`
- `sessionId`

현재 코드상 body 기준 응답은 위 4개 필드로 내려오고 있습니다.

추가 확인 요청:

- 이 응답 schema를 대외 계약으로 고정할 수 있는지
- 향후 일부 값이 header로도 내려갈 계획이 있는지
- 비인증 시에도 동일 필드 구조를 유지할 것인지

주의:

- 현재 `auth-service` 구현은 body 기반 응답만 확인되며, header fallback 계약은 코드상 확인되지 않았습니다.

### 2. `/auth/me` 인증 방식

현재 구현 기준으로 `/auth/me` 는 `sso_session` 쿠키 기반 호출이 가능합니다.

추가 확인 요청:

- `Authorization` 없이 `sso_session` 쿠키만으로 조회하는 방식을 계약으로 고정할 수 있는지
- `GET /auth/me` 가 브라우저 `credentials: include` 호출을 전제로 운영되는지
- 실패 응답을 항상 `GlobalResponse` 형식으로 유지할 것인지

주의:

- 현재 코드상 일반 미인증 케이스는 `401 NEED_LOGIN` 이고, `admin` 페이지 접근 제약 시에는 `403 FORBIDDEN` 이 발생할 수 있습니다.

### 3. SSO 쿠키 계약

gateway는 auth-service가 내려주는 쿠키를 그대로 전달합니다.

현재 코드상 확인되는 쿠키는 아래와 같습니다.

- `sso_session`
- `sso_oauth_state`

추가 확인 요청:

- 실제 운영 refresh cookie 이름
- 쿠키 `Path`, `Domain`, `Secure`, `SameSite`, `HttpOnly` 정책
- 환경별로 값이 달라질 수 있는지

주의:

- `sso_session` 운영 기본값은 `sso_session`, `Secure=true`, `HttpOnly=true`, `SameSite=None`, `Path=/` 입니다.
- `sso_oauth_state` 는 이름이 고정이며 `Secure`, `SameSite` 는 세션 쿠키 설정을 따릅니다.
- refresh cookie는 설정 파일상 기본 이름이 `refresh_token` 이지만, 실제 최종 동작은 외부 auth 라이브러리 계약까지 확인이 필요합니다.

### 4. OAuth callback / redirect 등록값

gateway는 `302 Location`을 그대로 전달하지만, 최종 callback URI는 auth-service 설정값과 정확히 일치해야 합니다.

확인 요청:

- `SSO_EXPLAIN_ORIGIN`
- `SSO_EDITOR_ORIGIN`
- `SSO_ADMIN_ORIGIN`
- `SSO_EXPLAIN_CALLBACK_URI`
- `SSO_EDITOR_CALLBACK_URI`
- `SSO_ADMIN_CALLBACK_URI`

또 아래를 확인 부탁드립니다.

- 환경별 callback URI 목록
- 허용되지 않은 callback 요청 시 응답 코드/메시지
- `INVALID_REQUEST` 발생 기준

주의:

- 현재 저장소에는 운영 기본값이 비어 있고, 실제 값은 환경변수 주입 기준입니다.

### 5. Gateway가 의존하는 원본 IP 규칙

현재 auth-service는 `X-Forwarded-For` 첫 번째 값을 우선 사용한다는 전제로 gateway를 맞췄습니다.

확인 요청:

- 실제 source of truth가 `X-Forwarded-For` 첫 번째 값이 맞는지
- admin SSO IP whitelist가 이 값을 기준으로 계속 동작할 것인지
- 다중 프록시 환경에서 추가 제약이 있는지

### 6. 에러 응답 형식 고정 여부

gateway는 auth-service 오류 응답을 가능한 그대로 전달합니다.

확인 요청:

- `400 INVALID_REQUEST`
- `401 UNAUTHORIZED`
- `401 NEED_LOGIN`
- `403 FORBIDDEN`

위 상태들이 항상 동일 `GlobalResponse` 형식인지 확인이 필요합니다.

주의:

- 현재 `GlobalException`, `AuthException`, `IllegalArgumentException` 은 `GlobalResponse` 로 매핑됩니다.
- `/auth/internal/session/validate` 는 예외를 던지지 않는 비인증 케이스에서 `401` 과 raw JSON body를 직접 반환합니다.

## Gateway 쪽에서 이미 하지 않는 것

아래 항목은 현재 gateway가 대신하지 않습니다.

- refresh token / session cookie 의미 판단
- SSO state 쿠키 생성 규칙 결정
- OAuth callback URI 유효성 검증
- admin 허용 IP 목록의 source of truth 관리
- auth-service 에러 포맷 재정의

이 부분은 현재 구조상 `auth-service`가 최종 책임을 가져야 합니다.

## Auth Service 팀 회신 요청

아래 항목에 대해 회신이 필요합니다.

1. `/auth/internal/session/validate`의 고정 응답 schema
2. `/auth/me`의 쿠키 기반 인증 동작 보장 여부
3. SSO 쿠키 이름과 속성의 운영값
4. callback URI 등록값과 환경별 목록
5. 주요 에러 응답이 항상 `GlobalResponse`인지 여부

## 한 줄 요약

게이트웨이는 `auth-service` 라우팅/쿠키/리다이렉트 전달 구조는 맞춰두었습니다.
이제 `auth-service` 팀이 validate 응답 schema, 쿠키 계약, callback 등록값을 확정해주면 연동 기준을 고정할 수 있습니다.

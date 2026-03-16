## API

### 대상 서버

- ExplainPage 인증 서버
- GitHub OAuth를 처리하는 Spring Boot SSO 서버

### 핵심 원칙

- GitHub OAuth callback은 프론트가 아니라 SSO 서버가 처리한다.
- 프론트는 GitHub access token을 직접 저장하거나 사용하지 않는다.
- 보호 경로의 최종 인증 판정은 항상 `GET /auth/me` 결과를 기준으로 한다.
- 모든 인증 요청은 `credentials: "include"` 기준으로 동작한다.

## Required Flow

1. 프론트가 `GET /auth/sso/start`로 이동한다.
2. 서버가 OAuth state를 저장하고 GitHub 로그인 시작 엔드포인트로 `302` 리다이렉트한다.
3. GitHub OAuth callback은 서버가 처리한다.
4. 서버가 사용자 검증 후 1회용 `ticket`을 발급한다.
5. 서버가 프론트 callback URL로 `302` 리다이렉트한다.
6. 프론트가 `POST /auth/exchange`로 `ticket`을 교환한다.
7. 서버가 세션 쿠키를 발급한다.
8. 프론트가 `GET /auth/me`를 호출한다.
9. `200 OK`이면 로그인 상태, `401 Unauthorized`이면 비로그인 상태다.

## Endpoint Summary

| Method | Path                | Purpose                            |
| ------ | ------------------- | ---------------------------------- |
| `GET`  | `/auth/sso/start`   | GitHub 로그인 시작                 |
| `POST` | `/auth/exchange`    | 1회용 ticket을 세션으로 교환       |
| `GET`  | `/auth/me`          | 현재 로그인 사용자 조회            |
| `POST` | `/auth/logout`      | 세션 종료                          |
| `GET`  | `/oauth2/**`        | OAuth2 authorization 시작 경로     |
| `GET`  | `/login/oauth2/**`  | OAuth2 callback 처리 경로          |

`/oauth2/**`, `/login/oauth2/**`는 Spring Security OAuth2 Client가 내부적으로 처리한다.

## 1. Start Login

### Request

`GET /auth/sso/start?page={page}`

또는

`GET /auth/sso/start?redirect_uri={encoded_callback_url}`

또는

`GET /auth/sso/start?page={page}&redirect_uri={encoded_callback_url}`

예시:

```http
GET /auth/sso/start?page=editor
```

```http
GET /auth/sso/start?redirect_uri=http%3A%2F%2Flocalhost%3A5173%2Fauth%2Fcallback
```

### Query Parameters

| Name           | Required | Description |
| -------------- | -------- | ----------- |
| `page`         | No       | 대상 프론트 식별자. `explain`, `editor`, `admin` 중 하나 |
| `redirect_uri` | No       | 인증 완료 후 복귀할 프론트 callback URL |

### Resolution Rules

- `page`가 있으면 서버에 등록된 해당 페이지의 callback URL을 사용한다.
- `page`와 `redirect_uri`를 함께 보내면 `redirect_uri`는 해당 `page`에 등록된 URL과 정확히 일치해야 한다.
- `page`가 없으면 `redirect_uri`는 서버 allowlist에 등록된 URL과 일치해야 한다.
- `page`와 `redirect_uri`가 모두 없으면 요청은 실패한다.
- `admin` 페이지 로그인 시작은 IP allowlist 검증을 통과해야 한다.

### Server Behavior

- OAuth state를 생성하고 서버 저장소에 TTL과 함께 저장한다.
- 응답에 OAuth state 쿠키를 내려준다.
- `/oauth2/authorization/github`로 `302 Found` 리다이렉트한다.

### Success Response

- `302 Found`

예시:

```http
Set-Cookie: sso_oauth_state=...; HttpOnly; Path=/; SameSite=Lax
Location: /oauth2/authorization/github
```

### Error Response

- `400 Bad Request`: `page`/`redirect_uri` 누락, 형식 오류, allowlist 불일치
- `403 Forbidden`: admin 페이지 접근이 IP 정책에 의해 차단됨

## 2. GitHub OAuth Callback

### Callback Endpoint

GitHub OAuth callback은 애플리케이션의 OAuth2 login processing 경로에서 처리된다.

기본 설정:

```text
/login/oauth2/code/github
```

### Server Behavior

- GitHub authorization code를 검증한다.
- GitHub 사용자 정보를 조회한다.
- 내부 사용자 식별 및 권한 매핑을 수행한다.
- 비활성 사용자면 인증을 거부한다.
- 프론트에 GitHub access token을 전달하지 않는다.
- 짧은 TTL의 1회용 `ticket`을 생성한다.
- OAuth state 쿠키를 제거한다.
- 최종적으로 프론트 callback URL로 리다이렉트한다.

### Redirect Result

성공 예시:

```text
http://localhost:5173/auth/callback?ticket=one_time_ticket
```

실패 예시:

```text
http://localhost:5173/auth/callback?error=oauth_failed
```

### Notes

- GitHub callback 처리는 반드시 SSO 서버가 담당한다.
- 프론트는 GitHub provider callback을 직접 처리하지 않는다.
- GitHub OAuth 앱에 등록된 callback URL은 프론트 URL이 아니라 서버의 OAuth callback URL이어야 한다.

## 3. Exchange Ticket

### Request

`POST /auth/exchange`

Headers:

```http
Content-Type: application/json
```

Body:

```json
{
  "ticket": "one-time-ticket"
}
```

### Server Behavior

- `ticket` 유효성을 검증한다.
- `ticket`은 1회만 사용 가능해야 한다.
- `ticket` TTL은 짧게 유지한다.
- 검증 성공 시 로그인 세션을 생성한다.
- `Set-Cookie` 헤더로 세션 쿠키를 내려준다.
- admin 페이지에서 발급된 ticket이면 교환 시점에도 IP allowlist를 다시 검증한다.

### Success Response

- `204 No Content`

예시:

```http
Set-Cookie: sso_session=...; HttpOnly; Path=/; SameSite=Lax
```

### Error Response

- `400 Bad Request`: body 형식 오류 또는 validation 실패
- `401 Unauthorized`: 만료되었거나 유효하지 않은 ticket
- `403 Forbidden`: admin 페이지 접근이 IP 정책에 의해 차단됨

## 4. Get Current User

### Request

`GET /auth/me`

선택적으로 admin 페이지 판정을 위해 `page`를 함께 전달할 수 있다.

예시:

```http
GET /auth/me?page=admin
```

### Success Response

- `200 OK`

```json
{
  "id": "user_123",
  "email": "user@example.com",
  "name": "John",
  "avatarUrl": "https://...",
  "roles": ["USER"]
}
```

### Response Fields

| Field       | Type       | Required | Description |
| ----------- | ---------- | -------- | ----------- |
| `id`        | `string`   | Yes      | 내부 사용자 식별자 |
| `email`     | `string`   | Yes      | 사용자 이메일 |
| `name`      | `string`   | Yes      | 사용자 표시 이름 |
| `avatarUrl` | `string`   | No       | 프로필 이미지 URL |
| `roles`     | `string[]` | Yes      | 권한 목록 |

### Unauthenticated Response

- `401 Unauthorized`

### Rules

- 보호 경로의 최종 인증 판정은 항상 `/auth/me`로 수행한다.
- 단순 쿠키 존재만으로 로그인 성공으로 간주하지 않는다.
- `page=admin`이면 IP allowlist 검증을 추가로 수행한다.

## 5. Logout

### Request

`POST /auth/logout`

### Server Behavior

- 현재 세션이 있으면 무효화한다.
- 세션 쿠키를 삭제한다.

### Success Response

- `204 No Content`

예시:

```http
Set-Cookie: sso_session=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax
```

### Unauthenticated Response

- `204 No Content`

## Error Response Format

예외 응답은 공통 응답 구조를 사용한다.

```json
{
  "httpStatus": "BAD_REQUEST",
  "isSuccess": false,
  "message": "잘못된 요청입니다.",
  "code": 9015,
  "data": null
}
```

주요 에러 코드:

| HTTP Status | Code  | Meaning |
| ----------- | ----- | ------- |
| `400`       | `9015` | 잘못된 요청 |
| `401`       | `9010` | 인증 정보 없음 |
| `401`       | `9013` | 로그인 필요 |
| `403`       | `9014` | 권한 없음 |

## Cookie Requirements

세션 쿠키 기본 설정은 아래와 같다.

- Cookie name: `sso_session`
- `HttpOnly`
- `Path=/`
- `SameSite=Lax`
- `Secure` 여부는 환경 설정으로 제어
- 기본 TTL: `604800`초

OAuth state 쿠키:

- Cookie name: `sso_oauth_state`
- `HttpOnly`
- `Path=/`
- `SameSite`는 세션 쿠키와 동일 정책 사용
- 기본 TTL: `300`초

## CORS Requirements

프론트는 `fetch(..., { credentials: 'include' })`로 호출한다.

따라서 서버는 다음을 만족해야 한다.

- `Access-Control-Allow-Credentials: true`
- 프론트 origin을 명시적으로 허용
- `*`와 credentials 조합 사용 금지
- 허용 메서드: `GET`, `POST`, `OPTIONS`

로컬 개발 기준 허용 origin:

- `http://localhost:3000`
- `http://localhost:5173`

## Backend Environment Variables

백엔드 설정은 단일 프론트가 아니라 페이지별 origin/callback URL을 사용한다.

### GitHub OAuth

| Name | Example | Purpose |
| ---- | ------- | ------- |
| `SSO_GITHUB_CLIENT_ID` | `github-client-id` | GitHub OAuth client id |
| `SSO_GITHUB_CLIENT_SECRET` | `github-client-secret` | GitHub OAuth client secret |
| `SSO_GITHUB_CALLBACK_URI` | `http://localhost:8080/auth/github/callback` | GitHub token 교환 시 사용하는 redirect URI 설정값 |

### Frontend Origins / Redirect URIs

| Name | Example | Purpose |
| ---- | ------- | ------- |
| `SSO_EXPLAIN_ORIGIN` | `http://localhost:3000` | Explain 프론트 origin |
| `SSO_EXPLAIN_CALLBACK_URI` | `http://localhost:3000/auth/callback` | Explain callback URL |
| `SSO_EDITOR_ORIGIN` | `http://localhost:5173` | Editor 프론트 origin |
| `SSO_EDITOR_CALLBACK_URI` | `http://localhost:5173/auth/callback` | Editor callback URL |
| `SSO_ADMIN_ORIGIN` | `http://localhost:5173` | Admin 프론트 origin |
| `SSO_ADMIN_CALLBACK_URI` | `http://localhost:5173/admin/auth/callback` | Admin callback URL |

### Session / Security

| Name | Example | Purpose |
| ---- | ------- | ------- |
| `SSO_STATE_TTL_SECONDS` | `300` | OAuth state TTL |
| `SSO_TICKET_TTL_SECONDS` | `120` | ticket TTL |
| `SSO_SESSION_COOKIE_NAME` | `sso_session` | 세션 쿠키 이름 |
| `SSO_SESSION_COOKIE_SECURE` | `false` | 세션 쿠키 `Secure` 여부 |
| `SSO_SESSION_COOKIE_SAME_SITE` | `Lax` | 세션 쿠키 SameSite 정책 |
| `SSO_SESSION_TTL_SECONDS` | `604800` | 세션 TTL |
| `SSO_ADMIN_IP_GUARD_ENABLED` | `true` | admin IP 가드 활성화 여부 |
| `SSO_ADMIN_IP_GUARD_DEFAULT_ALLOW` | `false` | admin IP 기본 허용 정책 |
| `SSO_ADMIN_IP_RULES` | `127.0.0.1,::1` | admin 허용 IP/CIDR 규칙 |

## Redirect Allowlist

서버는 아래 callback URL을 allowlist로 관리한다.

로컬 개발 기본값:

- `http://localhost:3000/auth/callback`
- `http://localhost:5173/auth/callback`
- `http://localhost:5173/admin/auth/callback`

`page` 파라미터가 있으면 위 목록에서 해당 페이지의 URL만 허용한다.

## Security Requirements

- GitHub access token과 refresh token은 서버 내부에서만 사용한다.
- 프론트로 GitHub provider token을 전달하지 않는다.
- `ticket`은 1회용이어야 한다.
- `ticket`은 짧은 TTL을 가져야 한다.
- `/auth/me`는 세션 유효성을 서버 기준으로 검증해야 한다.
- admin 페이지는 로그인 시작, ticket 교환, 사용자 조회 시점에 IP 정책 검증을 수행한다.

## Frontend Expectations

프론트는 아래 계약을 전제로 구현되어야 한다.

- 로그인 시작 URL: `GET /auth/sso/start?page=editor` 또는 `GET /auth/sso/start?redirect_uri=...`
- callback 이후: `POST /auth/exchange`
- 최종 인증 확인: `GET /auth/me`
- 로그아웃: `POST /auth/logout`
- 모든 인증 요청은 `credentials: "include"`
- GitHub callback 처리 주체: SSO 서버
- 보호 경로 최종 판정 주체: `/auth/me`

## Local Development Reference

로컬 개발 기본 callback URL:

- Explain: `http://localhost:3000/auth/callback`
- Editor: `http://localhost:5173/auth/callback`
- Admin: `http://localhost:5173/admin/auth/callback`

Editor 기준 전체 흐름 예시:

1. 프론트가 `GET /auth/sso/start?page=editor`로 이동한다.
2. 서버가 `/oauth2/authorization/github`로 리다이렉트한다.
3. GitHub 인증 후 서버 callback에서 인증을 완료한다.
4. 서버가 `http://localhost:5173/auth/callback?ticket=...`로 리다이렉트한다.
5. 프론트가 `POST /auth/exchange`를 호출한다.
6. 프론트가 `GET /auth/me`를 호출한다.
7. `/auth/me`가 `200`이면 보호 경로 접근을 허용한다.

# Platform Security 적용 기준

이 문서는 Auth-server가 `platform-security`를 소비하는 구현 기준을 정리합니다.
MSA 인증 표준, 서비스별 책임, 공통 identity header, 외부 credential 허용 정책은 contract 레포를 기준으로 합니다.

기준 버전: `1.0.4`

## 모듈 역할

`platform-security`의 주요 소비 모듈 역할은 아래와 같습니다.

| 모듈 | 역할 |
| --- | --- |
| `platform-security-bom` | platform-security artifact 버전 정렬 |
| `platform-security-edge-starter` | Gateway/edge 역할 서비스 진입점 |
| `platform-security-issuer-starter` | token/session issuer 역할 서비스 진입점 |
| `platform-security-resource-server-starter` | 일반 resource API 역할 서비스 진입점 |
| `platform-security-internal-service-starter` | 내부 호출 전용 서비스 진입점 |
| `platform-security-starter` | preset을 직접 설정할 때 쓰는 일반 starter |
| `platform-security-test-support` | 테스트 fixture |

역할별 starter는 서비스의 모든 endpoint 종류가 아니라 주 역할(primary role)을 기준으로 하나만 선택합니다.
Auth-server는 일부 internal endpoint가 있어도 token/session issuer가 주 역할이므로 `platform-security-issuer-starter`만 사용합니다.
Internal API는 별도 starter 추가가 아니라 `platform.security.boundary.internal-paths`와 internal CIDR 정책으로 분류합니다.

## Auth-server 소비 기준

Auth-server가 직접 사용하는 dependency:

```gradle
implementation platform(libs.platform.security.bom)
implementation libs.platform.security.issuer.starter
testImplementation libs.platform.security.test.support
```

Auth-server는 1계층 auth 구현체/provider를 직접 조립하지 않습니다.
다만 `platform-security` 1.0.4의 `PlatformSecurityContextResolvers.hybrid(...)` 계약에 맞추기 위한 adapter config에서는 1계층 경계 interface/model을 사용합니다.

도메인/컨트롤러/service 코드에서 직접 import하지 않는 타입 예:

- `com.auth.api.model.Principal`
- `com.auth.api.model.Tokens`
- `com.auth.core.service.AuthService`
- `com.auth.spi.TokenService`
- `com.auth.spi.UserFinder`
- `com.auth.spi.PasswordVerifier`
- `com.auth.spi.RefreshTokenStore`
- `com.auth.session.SessionStore`

Auth-server가 직접 소비하는 platform-security 타입은 2계층 경계 타입으로 제한합니다.

현재 직접 사용하는 타입:

| 용도 | 타입 |
| --- | --- |
| platform resolver factory | `io.github.jho951.platform.security.auth.PlatformSecurityContextResolvers` |
| 인증 context 해석 | `io.github.jho951.platform.security.api.SecurityContextResolver` |
| token adapter contract | `com.auth.spi.TokenService` |
| session adapter contract | `com.auth.session.SessionStore`, `com.auth.session.SessionPrincipalMapper` |
| principal adapter model | `com.auth.api.model.Principal` |
| servlet filter | `io.github.jho951.platform.security.web.PlatformSecurityServletFilter` |

## 현재 구현 방향

Auth-server의 로그인, refresh, logout, SSO 세션 저장, token 발급은 auth-service 도메인 코드가 소유합니다.
`platform-security`는 HTTP 요청에서 들어온 credential을 공통 security context로 정규화하는 경계에 사용합니다.

현재 연결 방식:

- `SecurityConfig`는 1.0.4 auto-config가 만든 `PlatformSecurityServletFilter`를 Spring Security filter chain에 연결합니다.
- `PlatformSecurityAuthConfig`는 Auth-server 도메인 token/session 저장소를 platform-security `TokenService`/`SessionStore` adapter로 연결합니다.
- `SecurityContextResolver`는 `PlatformSecurityContextResolvers.hybrid(...)`로 생성합니다.
- access token은 `AuthJwtTokenService`가 검증합니다.
- SSO session id는 `SsoSessionStore`에서 조회합니다.
- 반환 타입은 platform-security의 `SecurityContext`입니다.

## OAuth2 로그인 기준

Auth-server는 provider OAuth2 로그인 flow를 유지합니다.

Auth-server가 담당하는 부분:

- GitHub 등 provider authorization/callback 처리
- provider user info 검증
- user-service 사용자 조회/생성/연결
- 계정 상태와 도메인 정책 확인
- auth access/refresh token 발급
- SSO session 저장
- redirect와 cookie 정책

`platform-security`가 담당하는 부분:

- 요청 credential을 platform security request로 변환
- 인증 결과를 `SecurityContext`로 정규화
- downstream identity propagation에 필요한 공통 context 제공
- audit/security hook 연결점 제공

## OIDC mode 기준

OIDC mode는 외부 `id_token` 자체를 API credential로 받을 때 사용하는 경로입니다.
Auth-server의 기본 OAuth2 login flow와는 목적이 다릅니다.

OIDC mode가 적합한 경우:

- gateway가 외부 IdP의 `id_token`을 직접 받는 경우
- B2B partner 접근이 OIDC federation을 사용하는 경우
- 특정 API가 platform token 발급 없이 외부 identity를 직접 검증해야 하는 경우
- service mesh 또는 workload identity가 OIDC/JWT 형태로 들어오는 경우

Auth-server는 기존 OAuth2 login만 처리한다면 `OidcTokenVerifier`를 추가하지 않습니다.
외부 OIDC token을 직접 API credential로 받기로 한 경우에만 해당 verifier를 제공합니다.

예시:

```java
@Bean
OidcTokenVerifier oidcTokenVerifier(GatewayOidcVerifier verifier) {
    return request -> verifier.verify(request.idToken(), request.nonce());
}
```

요청 예:

```http
GET /auth/me HTTP/1.1
X-Auth-Oidc-Id-Token: <id_token>
X-Auth-Oidc-Nonce: <nonce>
```

## 경계 규칙

2계층이 제공해야 하는 것:

- JWT/SESSION/HYBRID/API_KEY/HMAC/OIDC/SERVICE_ACCOUNT 인증 수단 capability
- auth mode resolver
- capability resolver
- downstream identity propagation
- audit hook
- servlet/webflux adapter

2계층이 소유하지 않는 것:

- provider별 OAuth2 login endpoint
- authorization code exchange
- user provisioning
- domain role 결정
- tenant/org 매핑
- provider profile API 호출
- 서비스별 cookie/redirect 정책

## 구현 체크리스트

1. `platform-security-bom:1.0.4`와 `platform-security-issuer-starter:1.0.4`를 사용합니다.
2. Auth-server 도메인/컨트롤러/service 코드에서 `com.auth.*` 1계층 타입을 직접 import하지 않습니다.
3. `PlatformSecurityAuthConfig`에서만 `TokenService`/`SessionStore` adapter를 제공하고, provider 조립은 `PlatformSecurityContextResolvers.hybrid(...)`에 맡깁니다.
4. OAuth2 login 결과는 Auth-server 도메인 principal/token/session으로 처리합니다.
5. OIDC verifier는 Auth-server가 외부 OIDC ID token을 직접 받을 때만 추가합니다.
6. 역할별 starter는 issuer starter 하나만 사용하고, internal endpoint는 boundary 설정으로 분류합니다.

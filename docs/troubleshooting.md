# Troubleshooting

## GitHub Packages 인증 실패

증상:

- Gradle dependency resolve 실패
- Docker dev build에서 private package pull 실패

점검:

1. `GH_TOKEN`이 설정되어 있는지 확인합니다.
2. Gradle actor는 `GH_ACTOR`를 쓰는지 확인합니다.
3. Docker dev build를 쓰면 `GITHUB_ACTOR`도 같은 값으로 맞춥니다.
4. 권한이 `packages:read`인지 확인합니다.

## Local Run 시 `.env.local` 없음

증상:

- `Source env file not found: .env.local`

해결:

1. `.env.example`을 참고해 `.env.local`을 만듭니다.
2. `AUTH_JWT_SECRET`, `MYSQL_*`, `REDIS_*` 최소값을 채웁니다.

## Swagger UI가 보이지 않음

증상:

- `/swagger-ui.html` 404

원인:

- prod profile에서는 Swagger가 비활성입니다.

해결:

- dev profile로 실행했는지 확인합니다.

## SSO Callback 실패

증상:

- GitHub 로그인 후 state not found
- callback redirect mismatch

점검:

1. `SSO_GITHUB_CALLBACK_URI`와 GitHub App 설정이 같은지 확인합니다.
2. Gateway public callback과 upstream callback rewrite가 맞는지 확인합니다.
3. Redis 연결 상태와 `SSO_STATE_TTL_SECONDS`를 확인합니다.

## Internal API Key 관련 기동 실패

증상:

- prod 기동 시 internal API key 검증 실패

점검:

1. `INTERNAL_API_KEY`가 비어 있지 않은지 확인합니다.
2. prod에서 dev 기본값을 쓰고 있지 않은지 확인합니다.

## Redis 장애로 로그인/세션 이상

증상:

- refresh/session/SSO가 간헐적으로 실패
- Redis warning log 증가

해석:

- 일부 저장소는 fail-soft로 동작하므로 즉시 500이 나지 않아도 기능 품질은 저하될 수 있습니다.

점검:

1. Redis host/port/password/SSL 설정
2. shared network 연결 상태
3. key TTL과 clock skew

## MySQL 연결 실패

점검:

1. `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`
2. Docker에서는 `auth-private` network와 `auth-mysql` 컨테이너 상태
3. schema baseline이 적용되었는지 확인

## Route 401/403 혼동

해석:

- `auth-service`는 인증을 소유합니다.
- 최종 인가는 authz-service 또는 downstream boundary가 담당합니다.
- internal route는 public route와 별도 trust boundary입니다.

문제가 생기면 먼저 route가 public/auth/internal 중 어디에 속하는지 확인합니다.

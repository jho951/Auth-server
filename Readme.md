# Auth Service

Spring Boot 기반 인증 서버입니다. 현재 프로젝트는 MSA에서 독립 배포되는 `auth-service`를 목표로 하며, 로그인, 토큰 발급/재발급, 로그아웃, 인증 필터 연동을 담당합니다.

## Architecture

- `auth-service`
  - login
  - refresh rotation
  - logout
  - access token authentication
- `app stack`
  - `mysql`
  - `redis`
- `modules`
  - `app`: 실행 모듈
  - `common`: 공통 설정/응답/로깅 모듈

애플리케이션 이름은 `auth-service`입니다.

## Run

기본 환경은 `dev`입니다.

```bash
./docker/start.sh
```

스택별 실행:

```bash
./docker/start.sh dev app
./docker/start.sh dev all
```

종료:

```bash
./docker/shutdown.sh dev app
./docker/shutdown.sh dev all
```

## Notes

- 이 레포는 포트폴리오용 단일 저장소지만 배포 단위는 `auth-service`로 분리하는 방향에 맞춰 구성했습니다.
- 현재 Gradle 루트는 멀티모듈 집계 프로젝트이며, `app`과 `common` 모듈로 구성됩니다.
- Docker 환경 값의 단일 소스는 `gradle.properties`이고, `docker/*.sh`가 실행 시 `.generated/.env.*`를 생성합니다.
- 의존성 및 플러그인 버전은 `gradle/libs.versions.toml`에서 중앙 관리합니다.
- 자세한 협업 방식은 [CONTRIBUTING](./.github/CONTRIBUTING.md) 문서를 참고하면 됩니다.

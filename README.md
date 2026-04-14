# Auth-server

## 기준

- 프로젝트는 Gradle 멀티모듈로 실행 애플리케이션은 `app`, 내부 공통 코드는 `common`에 둡니다.
- Java 기준 버전은 17입니다.
- 인증 공통 기능은 `platform-security` `1.0.3` 기준으로 연동합니다.
- Docker Compose는 환경별로 `docker/dev/compose.yml`, `docker/prod/compose.yml`에서 관리합니다.
- Redis는 중앙 Redis를 사용합니다.

## 빠른 시작

Private GitHub Packages 접근 권한이 필요합니다.

```bash
export GITHUB_ACTOR=your-github-id
export GITHUB_TOKEN=your-package-read-token
```

### Docker

```bash
./scripts/run.docker.sh up dev app
```

### 로컬

```bash
./scripts/run.local.sh dev
```

### 종료

```bash
./scripts/run.docker.sh down dev app
```

## [문서](./docs/README.md)
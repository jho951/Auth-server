# Platform Governance 적용 기준

이 문서는 Auth-server가 `platform-governance`를 소비하는 구현 기준을 정리합니다.

기준 버전: `1.0.0`

## 모듈 역할

| 모듈 | 역할 |
| --- | --- |
| `platform-governance-api` | audit, policy, config 경계 타입 |
| `platform-governance-audit` | audit-log 기반 recorder adapter |
| `platform-governance-config` | policy-config 기반 config source adapter |
| `platform-governance-engine` | plugin-policy-engine 기반 평가 엔진 |
| `platform-governance-spring` | Spring Boot auto-configuration |
| `platform-governance-spring-boot-starter` | 소비 서비스 진입점 |

## Auth-server 소비 기준

Auth-server는 1계층 governance 구현 타입을 직접 소비하지 않습니다.

직접 import하지 않는 타입 예:

- `com.auditlog.api.AuditLogger`
- `com.auditlog.api.AuditEvent`
- `com.auditlog.core.*`
- `com.policyconfig.*`
- `com.pluginpolicyengine.*`

Auth-server가 직접 소비하는 platform-governance 타입은 2계층 경계 타입으로 제한합니다.

현재 직접 사용하는 타입:

| 용도 | 타입 |
| --- | --- |
| 감사 이벤트 기록 | `io.github.jho951.platform.governance.api.AuditLogRecorder` |
| 감사 이벤트 envelope | `io.github.jho951.platform.governance.api.AuditEntry` |

## 현재 구현 방향

Auth-server의 감사 이벤트 생성 시점과 domain-specific event name은 auth-service 도메인 코드가 소유합니다.
감사 이벤트의 기록 경계는 `platform-governance`가 제공합니다.

현재 연결 방식:

- `app/build.gradle`은 `platform-governance-bom`과 `platform-governance-spring-boot-starter`를 소비합니다.
- `AuthAuditLogService`는 `AuditLogRecorder`만 주입받습니다.
- `AuthAuditLogService`는 login/logout/internal account 이벤트를 `AuditEntry`로 변환합니다.
- `platform.governance.*` 설정은 `application.yml`에서 관리합니다.

## 설정

```yaml
platform:
  governance:
    enabled: true
    audit:
      enabled: true
    plugin-policy-engine:
      store: MEMORY
      cache-ttl-millis: 3000
    engine:
      strict: false
```

환경변수:

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `PLATFORM_GOVERNANCE_ENABLED` | `true` | platform-governance auto-configuration 활성화 |
| `PLATFORM_GOVERNANCE_AUDIT_ENABLED` | `true` | audit recorder 활성화 |
| `PLATFORM_GOVERNANCE_POLICY_ENGINE_STORE` | `MEMORY` | policy engine store |
| `PLATFORM_GOVERNANCE_POLICY_ENGINE_FILE_PATH` | 빈 값 | file store path |
| `PLATFORM_GOVERNANCE_POLICY_ENGINE_CACHE_TTL_MILLIS` | `3000` | policy engine cache TTL |
| `PLATFORM_GOVERNANCE_ENGINE_STRICT` | `false` | strict evaluation |

## 이벤트

| Event | Category | 주요 attributes |
| --- | --- | --- |
| `AUTH_LOGIN_PASSWORD` | `auth` | `eventType`, `result`, `actorId`, `channel` |
| `AUTH_LOGIN_SSO` | `auth` | `eventType`, `result`, `actorId`, `provider` |
| `AUTH_LOGOUT` | `auth` | `eventType`, `result`, `actorId`, `channel` |
| `AUTH_INTERNAL_ACCOUNT_CREATE` | `auth` | `eventType`, `actorType`, `resourceId`, `loginId` |
| `AUTH_INTERNAL_ACCOUNT_DELETE` | `auth` | `eventType`, `actorType`, `resourceId` |

## 경계

`platform-governance`는 감사 기록과 governance policy 경계를 제공합니다.
Auth-server는 business decision을 platform-governance에 숨기지 않고, 필요한 시점에 명시적으로 이벤트와 attributes를 넘깁니다.

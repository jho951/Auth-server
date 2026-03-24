# Database Schema

## Overview

이 서비스의 DB 스키마는 현재 JPA 엔티티 기준으로 관리됩니다.

- `dev` 프로필: [application-dev_db.yml](/Users/jhons/Downloads/BE/Auth-server/app/src/main/resources/dev/application-dev_db.yml) 에서 `spring.jpa.hibernate.ddl-auto: create`
- `prod` 프로필: [application-prod_db.yml](/Users/jhons/Downloads/BE/Auth-server/app/src/main/resources/prod/application-prod_db.yml) 에서 `jpa.hibernate.ddl-auto: none`
- 개발용 MySQL 초기화 스크립트는 DB와 계정만 생성하며, 테이블 생성은 애플리케이션(JPA)이 담당합니다.

초기 DB 생성 스크립트:
- dev: [docker/services/mysql/dev/init.sql](/Users/jhons/Downloads/BE/Auth-server/docker/services/mysql/dev/init.sql)
- prod: [docker/services/mysql/prod/init.sql](/Users/jhons/Downloads/BE/Auth-server/docker/services/mysql/prod/init.sql)

## Tables

### `auth_accounts`

인증 계정의 기본 정보를 저장합니다.

엔티티: [Auth.java](/Users/jhons/Downloads/BE/Auth-server/app/src/main/java/com/authservice/app/domain/auth/entity/Auth.java)

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | `BINARY(16)` | No | PK, UUID |
| `user_id` | `BINARY(16)` | No | 사용자 식별자, Unique |
| `login_id` | `VARCHAR` | No | 로그인 ID, Unique |
| `password_hash` | `VARCHAR` | No | 비밀번호 해시 |
| `account_locked` | `BOOLEAN` | No | 계정 잠금 여부 |
| `failed_login_count` | `INT` | No | 로그인 실패 횟수 |
| `password_updated_at` | `DATETIME` | No | 비밀번호 갱신 시각 |
| `last_login_at` | `DATETIME` | Yes | 마지막 로그인 시각 |
| `created_at` | `DATETIME` | No | 생성 시각 |
| `updated_at` | `DATETIME` | No | 수정 시각 |

비고:
- `user_id`, `login_id` 는 엔티티에서 unique 제약을 가집니다.
- 이메일은 `user-service`의 사용자 마스터 데이터로 관리하고, `auth_accounts`에는 중복 저장하지 않습니다.
- `created_at`, `updated_at`, `password_updated_at` 는 엔티티 라이프사이클 콜백으로 세팅됩니다.

### `auth_login_attempts`

로그인 시도 이력을 저장합니다.

엔티티: [AuthLoginAttempt.java](/Users/jhons/Downloads/BE/Auth-server/app/src/main/java/com/authservice/app/domain/auth/entity/AuthLoginAttempt.java)

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | `BINARY(16)` | No | PK, UUID |
| `login_id` | `VARCHAR` | No | 로그인 시도 대상 ID |
| `ip` | `VARCHAR` | Yes | 요청 IP |
| `result` | `VARCHAR` | No | `SUCCESS` 또는 `FAILURE` |
| `attempted_at` | `DATETIME` | No | 시도 시각 |

비고:
- 현재 엔티티에는 별도 인덱스 정의가 없습니다.

### `auth_social_accounts`

소셜 로그인 연동 계정을 저장합니다.

엔티티: [AuthSocialAccount.java](/Users/jhons/Downloads/BE/Auth-server/app/src/main/java/com/authservice/app/domain/auth/entity/AuthSocialAccount.java)

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | `BINARY(16)` | No | PK, UUID |
| `user_id` | `BINARY(16)` | No | 내부 사용자 식별자 |
| `provider` | `VARCHAR` | No | 소셜 제공자 |
| `provider_user_key` | `VARCHAR` | No | 제공자별 사용자 키 |
| `provider_email` | `VARCHAR` | Yes | 제공자 이메일 |
| `linked_at` | `DATETIME` | No | 마지막 연동 시각 |
| `created_at` | `DATETIME` | No | 생성 시각 |

비고:
- 현재 엔티티에는 `provider + provider_user_key` 유니크 제약이 선언되어 있지 않습니다.

### `mfa_factors`

MFA 수단 정보를 저장합니다.

엔티티: [MfaFactor.java](/Users/jhons/Downloads/BE/Auth-server/app/src/main/java/com/authservice/app/domain/auth/entity/MfaFactor.java)

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | `BINARY(16)` | No | PK, UUID |
| `user_id` | `BINARY(16)` | No | 내부 사용자 식별자 |
| `factor_type` | `VARCHAR` | No | MFA 수단 타입 |
| `secret_ref` | `VARCHAR` | No | 시크릿 참조값 |
| `enabled` | `BOOLEAN` | No | 활성화 여부 |
| `created_at` | `DATETIME` | No | 생성 시각 |
| `updated_at` | `DATETIME` | No | 수정 시각 |

## Removed Table

### `auth_audit_logs`

감사 로그 기능 제거와 함께 더 이상 사용하지 않습니다.

- 제거 대상에는 `AuthAuditLog`, `AuthAuditService`, `AuthAuditLogRepository` 구현이 포함됩니다.

이미 운영 DB에 테이블이 있다면 현재는 미사용 상태이며, 실제 삭제는 별도 마이그레이션으로 관리하는 것이 안전합니다.

## Relationships

- `auth_accounts.user_id` 는 외부 `user-service` 의 사용자 식별자를 참조하는 개념적 키입니다.
- `auth_social_accounts.user_id` 와 `mfa_factors.user_id` 도 같은 사용자 식별자를 사용합니다.
- 현재 JPA 엔티티에는 `@ManyToOne`, `@OneToMany`, FK 제약이 선언되어 있지 않습니다.

## Operational Notes

- 개발 환경 기본값은 H2 MySQL 호환 모드입니다.
- MySQL을 사용할 때도 UUID는 엔티티 정의상 `BINARY(16)` 기준입니다.
- 운영 반영 시에는 JPA 자동 생성에 의존하지 말고 명시적 마이그레이션 스크립트로 관리하는 편이 안전합니다.

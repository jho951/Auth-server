# Auth DB

이 문서는 DB 스키마를 변경할 때 확인해야 하는 구현 기준입니다.

## 테이블 종류

현재 애플리케이션이 직접 관리하는 테이블은 아래 3개입니다.

| 테이블 | 엔티티 | 용도 |
| --- | --- | --- |
| `auth_accounts` | `Auth` | 로그인 계정, password hash, 계정 잠금/실패 횟수 |
| `auth_login_attempts` | `AuthLoginAttempt` | 로그인 성공/실패 이력 |
| `mfa_factors` | `MfaFactor` | 사용자 MFA factor 메타데이터 |

`auth_audit_logs`는 이 서비스의 JPA entity가 관리하지 않습니다.
감사 로그는 `audit-log` 설정을 통해 파일 또는 외부 수집기로 보냅니다.

## UUID 바인딩

애플리케이션 코드와 MySQL 스키마에서 UUID 식별자를 `CHAR(36)`으로 바인딩합니다.
UUID 바인딩의 기준은 엔티티 annotation입니다.

```java
@Column(columnDefinition = "char(36)")
@JdbcTypeCode(SqlTypes.CHAR)
private UUID id;
```

구현 기준:

- Java type: `java.util.UUID`
- Hibernate JDBC type: `SqlTypes.CHAR`
- MySQL column type: `CHAR(36)`
- 문자열 형식: 표준 UUID 문자열. 예: `550e8400-e29b-41d4-a716-446655440000`
- UUID 생성: entity id는 `@GeneratedValue(strategy = GenerationType.UUID)`를 사용합니다.

전역 Hibernate 설정도 `CHAR`를 기본 UUID JDBC type으로 둡니다.

```yaml
spring:
  jpa:
    properties:
      hibernate:
        type:
          preferred_uuid_jdbc_type: CHAR
```

프로필별 위치:

- `app/src/main/resources/dev/application-dev_db.yml`
- `app/src/main/resources/prod/application-prod_db.yml`

## UUID 컬럼

| 테이블 | 컬럼 | Java 필드 | 비고 |
| --- | --- | --- | --- |
| `auth_accounts` | `id` | `Auth.id` | auth account primary key |
| `auth_accounts` | `user_id` | `Auth.userId` | user-service user id, unique |
| `auth_login_attempts` | `id` | `AuthLoginAttempt.id` | login attempt primary key |
| `mfa_factors` | `id` | `MfaFactor.id` | MFA factor primary key |
| `mfa_factors` | `user_id` | `MfaFactor.userId` | user-service user id |

새 UUID 컬럼을 추가할 때는 아래를 같이 적용합니다.

```java
@Column(name = "new_uuid_column", nullable = false, columnDefinition = "char(36)")
@JdbcTypeCode(SqlTypes.CHAR)
private UUID newUuidColumn;
```

그리고 migration에는 `CHAR(36)`을 명시합니다.

```sql
ALTER TABLE some_table
	ADD COLUMN new_uuid_column CHAR(36) NOT NULL;
```

## 프로필별 DDL 정책

| 프로필 | DDL 정책 | 용도 |
| --- | --- | --- |
| `dev` | `spring.jpa.hibernate.ddl-auto: create` | 로컬 개발에서 entity 기준으로 스키마 재생성 |
| `prod` | `spring.jpa.hibernate.ddl-auto: none` | 운영 DB는 migration으로만 변경 |

운영에서는 애플리케이션 시작으로 스키마가 자동 변경되지 않습니다. 운영 변경은 `db/migrations`의 SQL과 `scripts/db`의 적용 스크립트를 기준으로 합니다.

## Migration

현재 UUID 바인딩 migration:

- 적용 SQL: `db/migrations/2026-03-31_bind_uuid_columns_char36.sql`
- 롤백 SQL: `db/migrations/2026-03-31_bind_uuid_columns_char36__rollback.sql`
- 운영 적용 wrapper: `scripts/db/apply-bind-uuid-char36-prod.sh`

적용:

```bash
./scripts/db/apply-bind-uuid-char36-prod.sh
```

다른 env 파일을 사용해야 하면 첫 번째 인자로 넘깁니다.

```bash
./scripts/db/apply-bind-uuid-char36-prod.sh /path/to/.env.prod
```

롤백:

```bash
mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DB" \
  < db/migrations/2026-03-31_bind_uuid_columns_char36__rollback.sql
```

## Binary UUID migration 주의사항

기존 운영 DB가 실제 `BINARY(16)` UUID 바이트 값을 가지고 있다면 단순히 컬럼 타입만 `CHAR(36)`으로 바꾸면 안 됩니다.

주의할 점:

- `ALTER TABLE ... MODIFY COLUMN id CHAR(36)`은 `BINARY(16)` 값을 표준 UUID 문자열로 안전하게 변환하지 않습니다.
- 실제 binary UUID 데이터는 `BIN_TO_UUID(column)` 방식의 데이터 변환 절차가 필요합니다.
- 현재 `2026-03-31_bind_uuid_columns_char36.sql`은 타입 바인딩 기준을 맞추는 스크립트입니다. 운영 데이터가 이미 비어 있거나, UUID 문자열로 저장되어 있는 상태에서 적용하는 것을 기준으로 합니다.

운영 DB가 기존 `BINARY(16)`이고 데이터가 존재한다면 아래 순서를 먼저 따릅니다.

1. 운영 backup을 생성합니다.
2. 실제 컬럼 타입과 row count를 확인합니다.
3. 샘플 데이터를 `BIN_TO_UUID`로 읽어 UUID 문자열이 정상인지 확인합니다.
4. temp `CHAR(36)` 컬럼을 추가하고 `BIN_TO_UUID` 결과를 채웁니다.
5. 제약조건과 인덱스를 재생성한 뒤 컬럼을 교체합니다.
6. 애플리케이션을 `CHAR(36)` entity mapping 버전으로 배포합니다.

## 스키마 확인

현재 UUID 컬럼 타입 확인:

```sql
SELECT
	table_name,
	column_name,
	column_type,
	is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('auth_accounts', 'auth_login_attempts', 'mfa_factors')
  AND column_name IN ('id', 'user_id')
ORDER BY table_name, column_name;
```

기대 결과는 `column_type = char(36)`입니다.

UUID 문자열 형식 검증:

```sql
SELECT id
FROM auth_accounts
WHERE id IS NOT NULL
  AND id NOT REGEXP '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'
LIMIT 20;
```

`user_id`도 같은 방식으로 확인합니다.

```sql
SELECT user_id
FROM auth_accounts
WHERE user_id IS NOT NULL
  AND user_id NOT REGEXP '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'
LIMIT 20;
```

결과가 없어야 정상입니다.

## Repository 계약

Repository의 ID type은 entity UUID 바인딩과 맞춰야 합니다.

```java
public interface AuthRepository extends JpaRepository<Auth, UUID> {
	Optional<Auth> findByUserId(UUID userId);
}
```

구현 규칙:

- Repository ID generic은 `UUID`를 사용합니다.
- DTO와 controller path variable도 UUID 의미를 가진 값은 가능하면 `UUID`로 받습니다.
- 외부 API/OpenAPI에서는 UUID string format을 노출합니다.
- DB 쿼리에서 UUID를 직접 비교할 때는 표준 UUID 문자열을 사용합니다.

## 변경 규칙

DB 변경 시 지켜야 할 기준:

- 운영 테이블 변경은 `ddl-auto`에 기대지 않고 migration SQL로 남깁니다.
- UUID 컬럼은 entity와 SQL migration 양쪽에 `CHAR(36)`을 명시합니다.
- `@JdbcTypeCode(SqlTypes.CHAR)`를 빼지 않습니다.
- `user_id`는 user-service의 사용자 ID이므로 auth-service에서 값을 새로 생성하지 않습니다.
- 새 public API가 UUID를 주고받으면 `docs/openapi/auth-service.yml`도 같이 갱신합니다.
- 대량 데이터가 있는 운영 테이블의 UUID 타입을 바꿀 때는 `MODIFY COLUMN`만 사용하지 말고 데이터 변환 절차를 별도로 작성합니다.
- 과거 소셜 계정 제거용 migration이 필요하면 `db/migrations/2026-03-27_drop_auth_social_accounts.sql`과 `scripts/db/apply-drop-auth-social-accounts-prod.sh`를 참고합니다.

# Auth Database

`auth-service`는 UUID 식별자를 `CHAR(36)`로 바인딩합니다.

## Managed Tables

- `auth_accounts`
- `auth_login_attempts`
- `mfa_factors`

## UUID Binding

- Java entity ID type: `java.util.UUID`
- JPA/Hibernate JDBC type: `CHAR`
- MySQL column type: `CHAR(36)`

## Migration

운영 DB가 과거 `BINARY(16)` 스키마라면 아래를 적용합니다.

```bash
./scripts/db/apply-bind-uuid-char36-prod.sh
```

롤백이 필요하면:

```bash
mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DB" \
  < db/migrations/2026-03-31_bind_uuid_columns_char36__rollback.sql
```

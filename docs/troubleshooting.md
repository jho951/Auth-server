# 문제 해결

## `db/migrations` 유지

이유:

- `auth-service`가 `auth_accounts`, `auth_login_attempts`, `mfa_factors`를 직접 소유합니다.
- `prod` profile은 `spring.jpa.hibernate.ddl-auto: none`입니다.
- 운영 DB schema는 애플리케이션 시작으로 자동 변경되지 않습니다.
- 따라서 운영 DB 변경 SQL의 기준 위치가 필요합니다.

현재 역할 분리는 아래와 같습니다.

```text
app/src/main/java/.../entity
  현재 애플리케이션의 DB 모델

app/src/main/resources/*_db.yml
  dev/prod DB 연결과 Hibernate DDL 정책

db/migrations
  운영 DB에 적용할 schema migration SQL

scripts/db
  migration SQL 실행 wrapper

docker/{dev,prod}/services/mysql/init.sql
  MySQL 컨테이너 최초 bootstrap
```

`db/migrations`와 Docker `init.sql`은 역할이 다릅니다. 운영 중인 DB schema 변경은 Docker init SQL이 아니라 `db/migrations`에 남깁니다.

## MSA에서 DB migration 위치

MSA에서는 보통 서비스가 자기 데이터베이스 schema를 소유합니다.
따라서 `auth-service`가 소유하는 테이블의 migration은 `auth-service` repo 안에 두는 것이 실무적으로 안전합니다.

좋은 PR 단위:

```text
entity 변경
repository/service 변경
db/migrations SQL
rollback SQL
docs/database.md 변경
OpenAPI 변경이 있으면 docs/openapi 변경
```

피해야 할 방식:

```text
entity만 변경하고 운영 migration 없음
운영 SQL을 개인 문서나 별도 위치에만 보관
Docker init.sql에 운영 migration 섞기
다른 서비스가 auth DB schema를 직접 변경
```

## `db/migrations`와 Flyway

현재처럼 직접 SQL을 실행하는 방식:

```text
db/migrations
scripts/db
docs/database.md
```

장점:

- 단순합니다.
- 현재 repo 구조와 잘 맞습니다.
- 어떤 SQL이 운영에 적용되는지 파일로 바로 확인할 수 있습니다.
- rollback SQL을 별도 파일로 둘 수 있습니다.

한계:

- DB 내부에 migration 적용 이력이 자동으로 남지 않습니다.
- 어느 환경에 어디까지 적용됐는지 수동 확인이 필요합니다.
- CI/CD 자동화 수준이 낮습니다.

Flyway를 도입하는 방식:

```text
app/src/main/resources/db/migration
└── V20260331_001__bind_uuid_columns_char36.sql
```

장점:

- `flyway_schema_history`로 적용 이력이 DB에 남습니다.
- 배포 파이프라인에서 migration 적용을 자동화하기 쉽습니다.
- 여러 환경의 schema drift를 줄일 수 있습니다.

주의점:

- 운영에서 애플리케이션 또는 배포 작업이 DB 변경 권한을 가져야 합니다.
- destructive migration은 더 엄격한 리뷰와 백업 절차가 필요합니다.
- rollback 전략은 별도로 설계해야 합니다.

현재 권장:

```text
지금은 db/migrations 유지
배포 자동화와 schema history가 필요해지면 Flyway 도입 검토
```

## `db/migrations`를 제거할 수 있는 경우

아래 중 하나가 명확히 정해진 경우에만 제거를 검토합니다.

- Flyway/Liquibase로 migration 위치를 완전히 이전합니다.
- 운영 DB schema를 별도 infra repo에서 소유하고, 서비스 repo는 entity만 관리합니다.
- 이 서비스가 더 이상 DB schema를 직접 소유하지 않습니다.

그 외에는 `db/migrations`를 유지합니다.

## UUID schema migration 주의사항

UUID 타입 변경은 단순 컬럼 타입 변경으로 처리하면 안 되는 경우가 있습니다.

특히 기존 운영 DB가 실제 `BINARY(16)` UUID 바이트 값을 가지고 있다면:

```sql
ALTER TABLE auth_accounts
	MODIFY COLUMN id CHAR(36) NOT NULL;
```

이런 단순 변경은 표준 UUID 문자열 변환을 보장하지 않습니다.

이 경우에는 [database.md](./database.md)의 `Binary UUID migration 주의사항`을 먼저 확인하고, `BIN_TO_UUID` 기반 데이터 변환 절차를 별도로 작성합니다.

## 빠른 판단 기준

```text
서비스가 테이블을 소유한다
  -> migration은 서비스 repo에 둔다

prod ddl-auto가 none이다
  -> 운영 변경 SQL은 반드시 남긴다

Docker init.sql을 수정하려 한다
  -> 최초 bootstrap인지 운영 migration인지 먼저 구분한다

여러 환경의 적용 이력 추적이 필요하다
  -> Flyway/Liquibase 도입을 검토한다

DB 변경과 코드 변경이 다른 PR로 갈라진다
  -> 배포 순서와 rollback 위험을 다시 검토한다
```

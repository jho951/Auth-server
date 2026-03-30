-- auth-service UUID columns -> CHAR(36)
-- 목적: JPA UUID 식별자를 MySQL CHAR(36) 컬럼으로 통일

ALTER TABLE auth_accounts
	MODIFY COLUMN id CHAR(36) NOT NULL,
	MODIFY COLUMN user_id CHAR(36) NOT NULL;

ALTER TABLE auth_login_attempts
	MODIFY COLUMN id CHAR(36) NOT NULL;

ALTER TABLE mfa_factors
	MODIFY COLUMN id CHAR(36) NOT NULL,
	MODIFY COLUMN user_id CHAR(36) NOT NULL;

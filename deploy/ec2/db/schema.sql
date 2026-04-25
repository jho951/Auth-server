-- Auth-service baseline schema.
-- UUID identifiers are stored as canonical CHAR(36) strings with hyphens.

CREATE TABLE IF NOT EXISTS auth_accounts (
	id CHAR(36) NOT NULL,
	user_id CHAR(36) NOT NULL,
	login_id VARCHAR(255) NOT NULL,
	password_hash VARCHAR(255) NOT NULL,
	account_locked BIT NOT NULL,
	failed_login_count INT NOT NULL,
	password_updated_at DATETIME(6) NOT NULL,
	last_login_at DATETIME(6) NULL,
	version BIGINT NOT NULL DEFAULT 0,
	created_at DATETIME(6) NOT NULL,
	modified_at DATETIME(6) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_auth_accounts_user_id (user_id),
	UNIQUE KEY uk_auth_accounts_login_id (login_id)
);

CREATE TABLE IF NOT EXISTS auth_login_attempts (
	id CHAR(36) NOT NULL,
	login_id VARCHAR(255) NOT NULL,
	ip VARCHAR(255) NULL,
	result VARCHAR(255) NOT NULL,
	attempted_at DATETIME(6) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS mfa_factors (
	id CHAR(36) NOT NULL,
	user_id CHAR(36) NOT NULL,
	factor_type VARCHAR(255) NOT NULL,
	secret_ref VARCHAR(255) NOT NULL,
	enabled BIT NOT NULL,
	version BIGINT NOT NULL DEFAULT 0,
	created_at DATETIME(6) NOT NULL,
	modified_at DATETIME(6) NOT NULL,
	PRIMARY KEY (id),
	KEY ix_mfa_factors_user_id (user_id)
);

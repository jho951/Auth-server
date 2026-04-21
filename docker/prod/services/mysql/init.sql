CREATE DATABASE IF NOT EXISTS auth_service_db;
CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth_password';
GRANT ALL PRIVILEGES ON `auth_service_db`.* TO 'auth_user'@'%';
FLUSH PRIVILEGES;

-- 보안 관련 설정
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow.log';

USE auth_service_db;

-- 테이블 schema는 repo 공통 baseline을 단일 source로 사용
SOURCE /schema/auth-schema.sql;

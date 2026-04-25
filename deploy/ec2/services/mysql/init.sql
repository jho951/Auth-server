CREATE DATABASE IF NOT EXISTS auth_service_db;
CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth_password';
GRANT ALL PRIVILEGES ON `auth_service_db`.* TO 'auth_user'@'%';
FLUSH PRIVILEGES;

SET GLOBAL slow_query_log = 'ON';
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow.log';

USE auth_service_db;
SOURCE /schema/auth-schema.sql;

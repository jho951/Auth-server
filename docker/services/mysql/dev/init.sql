-- root 계정 외부 접속 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- DB 생성
CREATE DATABASE IF NOT EXISTS dev;

-- 생성된 DB 사용
USE dev;

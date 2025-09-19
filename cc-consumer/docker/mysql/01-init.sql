-- Consumer Database Initialization Script
-- PoC 환경을 위한 초기 스키마 및 테이블 생성

-- Database 생성 (이미 존재하는 경우 스킵)
CREATE DATABASE IF NOT EXISTS consumer_db;

-- 전용 사용자 생성 (호스트 DB 툴 접근용)
CREATE USER IF NOT EXISTS 'consumer_user'@'%' IDENTIFIED BY 'consumer123!';
GRANT ALL PRIVILEGES ON consumer_db.* TO 'consumer_user'@'%';
FLUSH PRIVILEGES;

USE consumer_db;

-- 마이데이터 파기 대상자 테이블
CREATE TABLE IF NOT EXISTS MYDATA_TERMINATE_USER (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pay_account_id BIGINT NOT NULL COMMENT '페이계정 ID',
    terminate_status ENUM('PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING' COMMENT '파기 상태',
    reason VARCHAR(255) DEFAULT NULL COMMENT '파기 사유',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uq_mydata_pay_account_terminate_status (pay_account_id, terminate_status),
    INDEX idx_mydata_pay_account_id (pay_account_id),
    INDEX idx_mydata_terminate_status (terminate_status),
    INDEX idx_mydata_created_at (created_at)
) COMMENT '마이데이터 파기 대상자';

-- 페이데이터 파기 대상자 테이블
CREATE TABLE IF NOT EXISTS PAY_TERMINATE_USER (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pay_account_id BIGINT NOT NULL COMMENT '페이계정 ID',
    terminate_status ENUM('PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING' COMMENT '파기 상태',
    reason VARCHAR(255) DEFAULT NULL COMMENT '파기 사유',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uq_pay_pay_account_terminate_status (pay_account_id, terminate_status),
    INDEX idx_pay_pay_account_id (pay_account_id),
    INDEX idx_pay_terminate_status (terminate_status),
    INDEX idx_pay_created_at (created_at)
) COMMENT '페이데이터 파기 대상자';

-- 초기 데이터 확인을 위한 쿼리 (로그 출력)
SELECT 'MYDATA_TERMINATE_USER table created successfully' as status;
SELECT 'PAY_TERMINATE_USER table created successfully' as status;

-- 테이블 구조 확인
DESCRIBE MYDATA_TERMINATE_USER;
DESCRIBE PAY_TERMINATE_USER;
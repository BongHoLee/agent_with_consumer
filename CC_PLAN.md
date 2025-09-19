# 프로젝트 개요
서로다른 Kafka Topic 두 개로부터 메시지를 컨슘하여 MySQL 데이터베이스 내 두 개 테이블에 멱등적으로 upsert를 하는 로직을 구현한다.

## 목적
두 개 토픽으로부터 전달되는 이벤트들 중 '파기 대상자'를 필터링 하여 각각 테이블에 멱등적으로 upsert 한다.
이후 별도의 batch application을 통해 파기 대상 테이블에 대한 실제 파기가 수행된다.
본 프로젝트의 목적은 Kafka 메시지로부터 파기 대상자를 정확히 추출하여 테이블에 기록하는 PoC를 수행하는 것이다.


## 기술 스택
- kafka, mysql 등의 인프라는 Docker Compose로 구성한다. 구성 방식은 PoC 성격에 걸맞게 가장 간단한 형태로 구성한다.
- Spring Boot 3.x
- jdk 21
- Spring Data JPA
- Spring for Apache Kafka
- Gradle
- kotlin

## Kafka Topic
### mydata.consent.v1
- 메시지 형식: JSON
- 메시지 샘플
```json
{
  "data": {
    "delete_event_type": "PFM_SERVICE_CLOSED_BY_USER",
    "pay_account_id": 46123695,
    "is_remove": true,
    "is_force": false
  },
  "type": "WITHDRAW"
}
```
- `is_remove = true` 인 대상자에 대해서만 파기 대상자로 간주한다.

### pay-account.payaccount-deleted.v2
- 메시지 형식: Avro
- Avro 스키마 샘플
```
record PayAccountDeletedEnvelop {
  string uuid;
  long occurred_at;
  long payAccountId;
  string reason;
}
```
- 해당 토픽의 모든 메시지의 `payAccountId`를 파기 대상자로 간주한다.

## MySQL 테이블
### 테이블명: MYDATA_TERMINATE_USER
- 테이블 스키마
```sql
CREATE TABLE MYDATA_TERMINATE_USER (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pay_account_id BIGINT NOT NULL COMMENT '페이계정 ID',
    terminate_status ENUM('PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING' COMMENT '파기 상태',
    reason VARCHAR(255) DEFAULT NULL COMMENT '파기 사유',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uq_pay_account_id (pay_account_id, status)
) COMMENT '마이데이터 파기 대상자';
```
- 어제 파기한 대상이 오늘 다시 파기 대상이 될 수 있다. 따라서 `pay_account_id`에 대해 유니크 제약조건을 걸 수 없다.
- `pay_account_id`와 `status` 컬럼에 대해 복합 유니크 제약조건을 건다.
- `terminate_status` 컬럼은 파기 상태를 나타낸다. 기본값은 'PENDING'이며, 파기가 완료되면 'COMPLETED'로 변경된다.
- 멱등성 보장을 위해 `pay_account_id`와 `status` 컬럼에 대해 복합 유니크 제약조건을 걸었다.
- `terminate_status`가 'PENDING'인 상태에서만 upsert가 가능하다. 즉, 동일 `pay_account_id`에 대해 'PENDING' 상태의 레코드가 존재하는 경우에는 중복 삽입이 불가능하다.
- `terminate_status`가 'COMPLETED'인 상태의 레코드는 파기가 완료된 상태이므로, 동일 `pay_account_id`에 대해 다시 'PENDING' 상태의 레코드를 삽입할 수 있다.


### 테이블명: PAY_TERMINATE_USER
- 테이블 스키마
```sql
CREATE TABLE PAY_TERMINATE_USER (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pay_account_id BIGINT NOT NULL COMMENT '페이계정 ID',
    terminate_status ENUM('PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING' COMMENT '파기 상태',
    reason VARCHAR(255) DEFAULT NULL COMMENT '파기 사유',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uq_pay_account_id (pay_account_id, status)
) COMMENT '페이데이터 파기 대상자';
```
- 어제 파기한 대상이 오늘 다시 파기 대상이 될 수 있다. 따라서 `pay_account_id`에 대해 유니크 제약조건을 걸 수 없다.
- `pay_account_id`와 `status` 컬럼에 대해 복합 유니크 제약조건을 건다.
- `terminate_status` 컬럼은 파기 상태를 나타낸다. 기본값은 'PENDING'이며, 파기가 완료되면 'COMPLETED'로 변경된다.
- 멱등성 보장을 위해 `pay_account_id`와 `status` 컬럼에 대해 복합 유니크 제약조건을 걸었다.
- `terminate_status`가 'PENDING'인 상태에서만 upsert가 가능하다. 즉, 동일 `pay_account_id`에 대해 'PENDING' 상태의 레코드가 존재하는 경우에는 중복 삽입이 불가능하다.
- `terminate_status`가 'COMPLETED'인 상태의 레코드는 파기가 완료된 상태이므로, 동일 `pay_account_id`에 대해 다시 'PENDING' 상태의 레코드를 삽입할 수 있다.


### 프로젝트 구성
- 멀티모듈로 진행한다.
- 멀티모듈의 이름은 cc-consumer로 한다.
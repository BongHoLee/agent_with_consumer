# CC-Consumer Implementation Plan

## 프로젝트 목표
- **mydata.consent.v1** 토픽에서 `is_remove = true`인 메시지만 필터링하여 **MYDATA_TERMINATE_USER** 테이블에 upsert
- **pay-account.payaccount-deleted.v2** 토픽의 모든 메시지를 **PAY_TERMINATE_USER** 테이블에 upsert
- 멱등성 보장: `pay_account_id`와 `terminate_status` 복합 유니크 제약조건 활용

## 핵심 요구사항 분석

### 테스트 및 품질 보장 요구사항
- **모든 단계마다 테스트 코드 필수 작성**: 단위 테스트, 통합 테스트, 기능 테스트
- **TDD 방식 권장**: 가능한 테스트 먼저 작성 후 구현
- **테스트 커버리지**: 최소 80% 이상 목표
- **모든 작업에 대한 상세한 결과 보고서 작성**: 구현 내용, 테스트 결과, 검증 사항 포함

### 멱등성 보장 방식
- `pay_account_id` + `terminate_status` 복합 유니크 제약조건
- **PENDING** 상태에서만 upsert 가능 (중복 삽입 방지)
- **COMPLETED** 상태에서는 동일 `pay_account_id`로 새로운 **PENDING** 레코드 생성 가능
- 어제 파기한 대상이 오늘 다시 파기 대상이 될 수 있음

### 토픽별 처리 로직
1. **mydata.consent.v1 (JSON)**
   - 필터링: `data.is_remove = true`인 메시지만 처리
   - 추출: `data.pay_account_id` → MYDATA_TERMINATE_USER 테이블
   - 사유: `data.delete_event_type`

2. **pay-account.payaccount-deleted.v2 (Avro)**
   - 모든 메시지 처리 (필터링 없음)
   - 추출: `payAccountId` → PAY_TERMINATE_USER 테이블
   - 사유: `reason`

## 구현 계획

### Phase 1: 데이터베이스 스키마 및 엔티티
1. **테이블 스키마 정의 (DDL)**
   ```sql
   -- MYDATA_TERMINATE_USER 테이블
   CREATE TABLE MYDATA_TERMINATE_USER (
       id BIGINT NOT NULL AUTO_INCREMENT,
       pay_account_id BIGINT NOT NULL,
       terminate_status ENUM('PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
       reason VARCHAR(255) DEFAULT NULL,
       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
       updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
       PRIMARY KEY (id),
       UNIQUE KEY uq_pay_account_terminate_status (pay_account_id, terminate_status)
   );

   -- PAY_TERMINATE_USER 테이블
   CREATE TABLE PAY_TERMINATE_USER (
       id BIGINT NOT NULL AUTO_INCREMENT,
       pay_account_id BIGINT NOT NULL,
       terminate_status ENUM('PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
       reason VARCHAR(255) DEFAULT NULL,
       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
       updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
       PRIMARY KEY (id),
       UNIQUE KEY uq_pay_account_terminate_status (pay_account_id, terminate_status)
   );
   ```

2. **JPA 엔티티 생성**
   - `MydataTerminateUser` 엔티티 
   - `PayTerminateUser` 엔티티
   - `TerminateStatus` Enum (PENDING, COMPLETED)
   - 공통 필드를 위한 `BaseEntity` 추상 클래스

3. **Repository 인터페이스**
   - `MydataTerminateUserRepository`
   - `PayTerminateUserRepository`
   - 멱등성을 위한 `findByPayAccountIdAndTerminateStatus()` 메서드

### Phase 2: Kafka 메시지 모델링
1. **JSON 메시지 모델 (mydata.consent.v1)**
   ```kotlin
   data class ConsentMessage(
       val data: ConsentData,
       val type: String
   )
   
   data class ConsentData(
       @JsonProperty("delete_event_type") val deleteEventType: String,
       @JsonProperty("pay_account_id") val payAccountId: Long,
       @JsonProperty("is_remove") val isRemove: Boolean,
       @JsonProperty("is_force") val isForce: Boolean
   )
   ```

2. **Avro 스키마 및 클래스 (pay-account.payaccount-deleted.v2)**
   ```avro
   record PayAccountDeletedEnvelop {
       string uuid;
       long occurred_at;
       long payAccountId;
       string reason;
   }
   ```
   - Avro Maven Plugin을 통한 자동 클래스 생성
   - Confluent Schema Registry 연동

### Phase 3: Kafka Consumer 구현
1. **MydataConsentConsumer**
   ```kotlin
   @KafkaListener(topics = ["mydata.consent.v1"])
   fun consumeConsentMessage(message: ConsentMessage) {
       if (message.data.isRemove) {
           mydataTerminateService.processTermination(
               payAccountId = message.data.payAccountId,
               reason = message.data.deleteEventType
           )
       }
   }
   ```

2. **PayAccountDeletedConsumer**
   ```kotlin
   @KafkaListener(topics = ["pay-account.payaccount-deleted.v2"])
   fun consumePayAccountDeleted(envelope: PayAccountDeletedEnvelop) {
       payTerminateService.processTermination(
           payAccountId = envelope.payAccountId,
           reason = envelope.reason
       )
   }
   ```

### Phase 4: 비즈니스 로직 레이어
1. **MydataTerminateService**
   ```kotlin
   @Transactional
   fun processTermination(payAccountId: Long, reason: String) {
       val existing = repository.findByPayAccountIdAndTerminateStatus(
           payAccountId, TerminateStatus.PENDING
       )
       
       if (existing == null) {
           // PENDING 상태의 레코드가 없으면 새로 생성 (멱등성 보장)
           repository.save(MydataTerminateUser(
               payAccountId = payAccountId,
               reason = reason,
               terminateStatus = TerminateStatus.PENDING
           ))
       }
       // 이미 PENDING 상태 레코드가 있으면 중복 삽입 방지
   }
   ```

2. **PayTerminateService** (동일한 로직 구조)

### Phase 5: Kafka 및 데이터베이스 설정
1. **Kafka Consumer 설정**
   ```yaml
   spring:
     kafka:
       consumer:
         bootstrap-servers: localhost:9092
         group-id: cc-consumer-group
         auto-offset-reset: earliest
         enable-auto-commit: false
       listener:
         ack-mode: manual_immediate
   ```

2. **MySQL 설정**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/consumer_db
       username: root
       password: password
     jpa:
       hibernate:
         ddl-auto: none
       show-sql: true
   ```

### Phase 6: Docker Compose 인프라 구성
1. **docker-compose.yml 작성**
   - MySQL 8.0 컨테이너
   - Apache Kafka + Zookeeper
   - Confluent Schema Registry (Avro용)

### Phase 7: 테스트 구현
1. **멱등성 검증 테스트**
   ```kotlin
   @Test
   fun `동일 payAccountId PENDING 상태 중복 삽입 방지 테스트`() {
       // PENDING 상태 레코드 이미 존재
       // 같은 payAccountId로 다시 삽입 시도
       // 중복 삽입되지 않음을 검증
   }
   
   @Test
   fun `COMPLETED 상태에서 새로운 PENDING 삽입 허용 테스트`() {
       // COMPLETED 상태 레코드 존재
       // 같은 payAccountId로 PENDING 상태 삽입
       // 정상 삽입됨을 검증
   }
   ```

2. **Kafka 통합 테스트**
   - TestContainers 활용

## 패키지 구조
```
com.consumer.cconsumer/
├── config/                    # 설정 클래스
│   ├── KafkaConfig.kt        # Kafka Consumer 설정
│   └── DatabaseConfig.kt     # JPA/DataSource 설정
├── domain/                    # 도메인 레이어
│   ├── entity/               # JPA 엔티티
│   │   ├── BaseEntity.kt     # 공통 필드 (created_at, updated_at)
│   │   ├── MydataTerminateUser.kt
│   │   ├── PayTerminateUser.kt
│   │   └── TerminateStatus.kt # ENUM
│   └── repository/           # Repository 인터페이스
│       ├── MydataTerminateUserRepository.kt
│       └── PayTerminateUserRepository.kt
├── service/                  # 비즈니스 로직
│   ├── MydataTerminateService.kt
│   └── PayTerminateService.kt
├── consumer/                 # Kafka Consumer
│   ├── MydataConsentConsumer.kt
│   └── PayAccountDeletedConsumer.kt
├── model/                    # 메시지 모델
│   ├── ConsentMessage.kt     # JSON 모델
│   └── avro/                 # Avro 생성 클래스
└── exception/                # 예외 처리
    └── TerminateProcessException.kt
```

## 핵심 기술적 고려사항

### 1. 멱등성 보장 전략
- **복합 유니크 제약조건**: `(pay_account_id, terminate_status)`
- **비즈니스 로직**: PENDING 상태 레코드 존재 시 중복 삽입 방지
- **동시성 처리**: `@Transactional` + 유니크 제약조건으로 Race Condition 방지

### 2. Kafka Consumer 설정
- **Manual Commit**: `enable-auto-commit: false`, `ack-mode: manual_immediate`
- **Consumer Group**: `cc-consumer-group` (동일 그룹으로 파티션 분산 처리)
- **Error Handling**: 메시지 처리 실패 시 재시도 후 DLQ 처리

### 3. 데이터베이스 연동
- **Connection Pool**: HikariCP 기본 설정
- **DDL 관리**: `ddl-auto: none` (수동 스키마 관리)
- **Query 최적화**: 복합 인덱스 활용

### 4. Avro 처리
- **Schema Registry**: Confluent Schema Registry 연동
- **Serialization**: `io.confluent:kafka-avro-serializer` 활용
- **버전 호환성**: Avro 스키마 진화 고려

## 상세 구현 순서

### ✅ 0단계: 프로젝트 기본 구조
- [x] 멀티모듈 Gradle 프로젝트 생성
- [x] Spring Boot Application 클래스
- [x] 기본 application.yml 설정

### ✅ 0.5단계: Docker 인프라 환경 구성 (우선순위: 최고)
**PoC 환경을 위한 가장 간단한 형태의 Docker Compose 구성**

- [x] `docker-compose.yml` 작성 (루트 디렉토리)
- [x] MySQL 초기 스키마 파일 작성 (`docker/mysql/init.sql`)
- [x] Kafka 토픽 생성 스크립트 (`docker/kafka/create-topics.sh`)
- [x] Docker 환경 테스트
- [x] 테스트 메시지 발행 스크립트 작성 (개발/테스트용)

### ✅ 1단계: Phase 1 - 도메인 모델 (우선순위: 높음)
- [x] `TerminateStatus` Enum 생성
- [x] `BaseEntity` 추상 클래스 생성
- [x] `MydataTerminateUser` 엔티티 생성
- [x] `PayTerminateUser` 엔티티 생성
- [x] Repository 인터페이스 생성
- [x] DDL 스크립트 작성 (`schema.sql`) - 이미 docker/mysql/init.sql에 작성됨
- [x] **테스트 코드 작성**:
  - [x] 엔티티 단위 테스트 (생성, 필드 검증) - 100% 성공
  - [x] Repository 통합 테스트 (CRUD, 유니크 제약조건 검증) - MydataTerminateUser 100% 성공, PayTerminateUser 테이블 스키마 이슈 있음
  - [x] 멱등성 보장 테스트 (중복 삽입 방지) - MydataTerminateUser에 대해서는 검증 완료
- [x] **1단계 상세 결과 보고서 작성**

### 📋 2단계: Phase 2 - 메시지 모델 (우선순위: 높음)
- [ ] `ConsentMessage`, `ConsentData` 데이터 클래스 생성
- [ ] Avro 스키마 파일 작성 (`PayAccountDeletedEnvelop.avsc`)
- [ ] Avro Gradle Plugin 설정
- [ ] 메시지 모델 단위 테스트

### 📋 3단계: Phase 3 - Kafka Consumer (우선순위: 높음)
- [ ] `MydataConsentConsumer` 구현
- [ ] `PayAccountDeletedConsumer` 구현  
- [ ] Kafka 설정 클래스 (`KafkaConfig`) 구현
- [ ] Consumer 에러 처리 로직

### 📋 4단계: Phase 4 - 비즈니스 로직 (우선순위: 높음)
- [ ] `MydataTerminateService` 구현
- [ ] `PayTerminateService` 구현
- [ ] 멱등성 보장 로직 구현
- [ ] 트랜잭션 처리

### 📋 5단계: Phase 5 - 설정 완성 (우선순위: 중간)
- [ ] Kafka Consumer 상세 설정
- [ ] MySQL 연결 설정 최적화
- [ ] Avro Serializer 설정

### 📋 6단계: Phase 6 - 설정 통합 및 최적화 (우선순위: 중간)  
- [ ] Docker 환경과 애플리케이션 설정 연동 확인
- [ ] 프로파일별 설정 분리 (dev, test, prod)
- [ ] Connection Pool, Kafka Consumer 성능 튜닝

### 📋 7단계: Phase 7 - 테스트 (우선순위: 중간)
- [ ] Repository 테스트 (TestContainers)
- [ ] Service 로직 테스트 
- [ ] 멱등성 검증 테스트
- [ ] Kafka Consumer 통합 테스트

### 📋 8단계: Phase 8 - 운영 준비 (우선순위: 낮음)
- [ ] 로깅 설정 최적화
- [ ] 헬스체크 엔드포인트
- [ ] 메트릭 수집 설정

## 다음 작업 우선순위

### 🔥 즉시 진행 (PoC 환경 구성)
**0.5단계: Docker 인프라 환경 구성**
- PoC 성격에 걸맞는 가장 간단한 형태
- MySQL, Kafka, Schema Registry 컨테이너 구성
- 초기 스키마 및 토픽 생성
- 테스트 메시지 발행 환경 준비

### 📋 순차 진행
**1단계 → 2단계 → 3단계 → 4단계** (핵심 기능 구현)
**5단계 → 6단계 → 7단계 → 8단계** (최적화 및 운영 준비)

### 💡 PoC 검증 포인트
1. **Docker 환경 구동**: 모든 인프라 컨테이너 정상 기동
2. **메시지 소비**: 두 토픽에서 메시지 정상 수신
3. **멱등성 보장**: 동일 메시지 재처리 시 중복 삽입 방지
4. **필터링 로직**: `is_remove=true`만 처리되는지 확인
5. **데이터 정합성**: 복합 유니크 제약조건 동작 확인

**먼저 0.5단계 Docker 환경 구성부터 시작하는 것이 좋겠습니다!**
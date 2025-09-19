# Docker 인프라 테스트 튜토리얼

## 📋 개요
이 문서는 PoC 환경의 Docker 인프라(MySQL, Kafka, Schema Registry)를 기동하고 테스트하는 방법을 안내합니다.

## 🚀 1단계: 인프라 기동

### 1.1 Docker Compose 실행
```bash
# 프로젝트 루트 디렉토리에서 실행
cd /Users/bongholee/dev/consumer

# 백그라운드에서 모든 컨테이너 시작
docker compose up -d
```

### 1.2 컨테이너 상태 확인
```bash
# 실행 중인 컨테이너 확인
docker compose ps

# 예상 출력:
# NAME                       IMAGE                    STATUS              PORTS
# consumer-kafka             apache/kafka:3.7.0       Up                  0.0.0.0:9092->9092/tcp
# consumer-mysql             mysql:8.0                Up                  0.0.0.0:3306->3306/tcp
# consumer-schema-registry   confluentinc/cp-schema-registry:7.4.0   Up   0.0.0.0:8081->8081/tcp
```

### 1.3 컨테이너 로그 확인 (문제 발생 시)
```bash
# 전체 로그 확인
docker compose logs

# 특정 서비스 로그 확인
docker compose logs kafka
docker compose logs mysql
docker compose logs schema-registry
```

## 🗄️ 2단계: MySQL 테스트

### 2.1 MySQL 연결 테스트
```bash
# MySQL 컨테이너에 접속
docker exec -it consumer-mysql mysql -u root -ppassword

# 또는 외부에서 접속 (mysql 클라이언트가 설치된 경우)
mysql -h localhost -P 3306 -u root -ppassword
```

### 2.2 데이터베이스 및 테이블 확인
```sql
-- 데이터베이스 목록 확인
SHOW DATABASES;

-- consumer_db 사용
USE consumer_db;

-- 테이블 목록 확인
SHOW TABLES;

-- 테이블 구조 확인
DESCRIBE MYDATA_TERMINATE_USER;
DESCRIBE PAY_TERMINATE_USER;

-- 인덱스 확인
SHOW INDEX FROM MYDATA_TERMINATE_USER;
SHOW INDEX FROM PAY_TERMINATE_USER;
```

### 2.3 테스트 데이터 삽입
```sql
-- 테스트 데이터 삽입
INSERT INTO MYDATA_TERMINATE_USER (pay_account_id, reason) 
VALUES (12345, 'PFM_SERVICE_CLOSED_BY_USER');

-- 데이터 확인
SELECT * FROM MYDATA_TERMINATE_USER;

-- 멱등성 테스트 (동일 pay_account_id, PENDING 상태로 중복 삽입 시도)
INSERT INTO MYDATA_TERMINATE_USER (pay_account_id, reason) 
VALUES (12345, 'DUPLICATE_TEST');
-- 에러 발생 예상: Duplicate entry '12345-PENDING'

-- 연결 종료
EXIT;
```

## 📨 3단계: Kafka 기본 테스트

### 3.1 Kafka 컨테이너 접속
```bash
# Kafka 컨테이너에 접속
docker exec -it consumer-kafka bash
```

### 3.2 토픽 관리
```bash
# 컨테이너 내부에서 실행

# 토픽 목록 확인
kafka-topics.sh --list --bootstrap-server localhost:9092

# 토픽 생성 (자동 생성이 활성화되어 있지만 명시적 생성)
kafka-topics.sh --create --topic mydata.consent.v1 \
  --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1

kafka-topics.sh --create --topic pay-account.payaccount-deleted.v2 \
  --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1

# 토픽 상세 정보 확인
kafka-topics.sh --describe --topic mydata.consent.v1 --bootstrap-server localhost:9092
kafka-topics.sh --describe --topic pay-account.payaccount-deleted.v2 --bootstrap-server localhost:9092
```

### 3.3 JSON 메시지 테스트 (mydata.consent.v1)

#### Producer 실행 (새 터미널 1)
```bash
# 새 터미널에서 실행
docker exec -it consumer-kafka kafka-console-producer.sh \
  --topic mydata.consent.v1 \
  --bootstrap-server localhost:9092
```

#### Consumer 실행 (새 터미널 2)
```bash
# 새 터미널에서 실행
docker exec -it consumer-kafka kafka-console-consumer.sh \
  --topic mydata.consent.v1 \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

#### 테스트 메시지 발송 (터미널 1에서)
```json
{"data":{"delete_event_type":"PFM_SERVICE_CLOSED_BY_USER","pay_account_id":46123695,"is_remove":true,"is_force":false},"type":"WITHDRAW"}
```
```json
{"data":{"delete_event_type":"USER_CONSENT_WITHDRAW","pay_account_id":12345678,"is_remove":false,"is_force":false},"type":"WITHDRAW"}
```
```json
{"data":{"delete_event_type":"SERVICE_TERMINATION","pay_account_id":87654321,"is_remove":true,"is_force":true},"type":"WITHDRAW"}
```

Consumer에서 메시지가 수신되는지 확인합니다.

### 3.4 스크립트를 통한 자동 테스트
```bash
# 프로젝트 루트에서 실행
./scripts/test-json-producer.sh
```

## 🔗 4단계: Schema Registry 및 Avro 테스트

### 4.1 Schema Registry 상태 확인
```bash
# Schema Registry 연결 테스트
curl http://localhost:8081/subjects

# 스키마 목록 확인
curl http://localhost:8081/subjects
```

### 4.2 Avro 스키마 등록
```bash
# 자동 스키마 등록 스크립트 실행
./scripts/test-avro-schema.sh

# 수동으로 스키마 등록
curl -X POST http://localhost:8081/subjects/pay-account.payaccount-deleted.v2-value/versions \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{
    "schema": "{\"type\":\"record\",\"name\":\"PayAccountDeletedEnvelop\",\"fields\":[{\"name\":\"uuid\",\"type\":\"string\"},{\"name\":\"occurred_at\",\"type\":\"long\"},{\"name\":\"payAccountId\",\"type\":\"long\"},{\"name\":\"reason\",\"type\":\"string\"}]}"
  }'
```

### 4.3 등록된 스키마 확인
```bash
# 특정 스키마 조회
curl http://localhost:8081/subjects/pay-account.payaccount-deleted.v2-value/versions/latest

# 모든 등록된 스키마 확인
curl http://localhost:8081/subjects
```

## 🛠️ 5단계: 문제 해결

### 5.1 컨테이너 재시작
```bash
# 전체 재시작
docker compose down
docker compose up -d

# 특정 컨테이너만 재시작
docker compose restart kafka
docker compose restart mysql
docker compose restart schema-registry
```

### 5.2 데이터 초기화
```bash
# 모든 컨테이너와 볼륨 삭제 (주의: 데이터 손실)
docker compose down -v

# 다시 시작
docker compose up -d
```

### 5.3 로그 확인
```bash
# 실시간 로그 모니터링
docker compose logs -f

# 특정 서비스 로그만 확인
docker compose logs -f kafka
```

### 5.4 포트 충돌 확인
```bash
# 포트 사용 확인
netstat -an | grep :3306  # MySQL
netstat -an | grep :9092  # Kafka
netstat -an | grep :8081  # Schema Registry

# 포트를 사용하는 프로세스 확인 (macOS)
lsof -i :3306
lsof -i :9092
lsof -i :8081
```

## 📊 6단계: 종합 테스트

### 6.1 전체 파이프라인 테스트
```bash
# 1. 인프라 상태 확인
docker compose ps

# 2. MySQL 연결 확인
docker exec consumer-mysql mysqladmin ping -h localhost -u root -ppassword

# 3. Kafka 토픽 확인
docker exec consumer-kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# 4. Schema Registry 확인
curl -s http://localhost:8081/subjects

# 5. 테스트 메시지 발송
./scripts/test-json-producer.sh

# 6. MySQL에서 데이터 확인 (애플리케이션 구현 후)
# docker exec -it consumer-mysql mysql -u root -ppassword -e "SELECT * FROM consumer_db.MYDATA_TERMINATE_USER;"
```

## 🔍 7단계: 모니터링

### 7.1 리소스 사용량 확인
```bash
# 컨테이너 리소스 사용량
docker stats

# 디스크 사용량
docker system df
```

### 7.2 네트워크 확인
```bash
# Docker 네트워크 목록
docker network ls

# 네트워크 상세 정보
docker network inspect consumer_default
```

## 🚨 주의사항

1. **포트 충돌**: 3306(MySQL), 9092(Kafka), 8081(Schema Registry) 포트가 이미 사용 중이지 않은지 확인
2. **메모리**: Docker Desktop에 충분한 메모리가 할당되어 있는지 확인 (최소 4GB 권장)
3. **방화벽**: 필요시 로컬 방화벽에서 해당 포트들을 허용
4. **데이터 영속성**: 현재 MySQL만 볼륨을 사용하므로 컨테이너 재시작 시 Kafka 데이터는 초기화됨

## ✅ 성공 기준

✅ 모든 컨테이너가 `Up` 상태  
✅ MySQL 연결 및 테이블 생성 확인  
✅ Kafka 토픽 생성 및 메시지 송수신 가능  
✅ Schema Registry에 Avro 스키마 등록 성공  
✅ 테스트 스크립트 정상 실행  

이제 Spring Boot 애플리케이션 개발을 시작할 준비가 완료되었습니다!
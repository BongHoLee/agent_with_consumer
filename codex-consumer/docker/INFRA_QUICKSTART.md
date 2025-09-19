# Docker 기반 Kafka/MySQL 테스트 튜토리얼

PoC용 인프라(단일 Kafka + MySQL)를 빠르게 구동하고, 간단한 메시지 생산/소비와 테이블 확인을 해보는 과정을 단계별로 정리했습니다.

## 사전 준비
- Docker Desktop 또는 Docker Engine + Docker Compose v2
- 프로젝트 루트(`/Users/bongholee/dev/consumer`) 기준으로 진행

## 1. 인프라 구동
1. 작업 디렉터리를 이동합니다.
   ```bash
   cd codex-consumer/docker
   ```
2. 컨테이너를 백그라운드로 실행합니다.
   ```bash
   docker compose up -d
   ```
3. 컨테이너 상태를 확인합니다.
   ```bash
   docker compose ps
   ```
   `codex-mysql`, `codex-kafka`, `codex-kafdrop`이 모두 `Up` 상태이면 정상입니다.
> 호스트에서 접근할 때는 MySQL `13306`, Kafka `19092`, Kafdrop `19093` 포트를 사용합니다.

## 2. Kafka 토픽 생성
Bitnami Kafka 이미지는 `/opt/bitnami/kafka/bin`에 CLI가 위치합니다.

1. `mydata.consent.v1`와 `pay-account.payaccount-deleted.v2` 토픽을 생성합니다.
   ```bash
   docker compose exec kafka /opt/bitnami/kafka/bin/kafka-topics.sh \
     --create \
     --topic mydata.consent.v1 \
     --bootstrap-server localhost:19092 \
     --partitions 1 \
     --replication-factor 1 \
     --if-not-exists

   docker compose exec kafka /opt/bitnami/kafka/bin/kafka-topics.sh \
     --create \
     --topic pay-account.payaccount-deleted.v2 \
     --bootstrap-server localhost:19092 \
     --partitions 1 \
     --replication-factor 1 \
     --if-not-exists
   ```
2. 토픽 목록을 확인합니다.
   ```bash
   docker compose exec kafka /opt/bitnami/kafka/bin/kafka-topics.sh \
     --list \
     --bootstrap-server localhost:19092
   ```

## 3. Kafdrop UI 확인
브라우저에서 다음 주소를 열어 토픽과 파티션 상태를 확인할 수 있습니다.

```
http://localhost:19093
```

첫 화면에서 브로커 연결 상태가 `UP`으로 표시되는지 확인하고, `Topics` 메뉴에서 생성한 토픽들을 확인합니다.

## 4. Kafka 메시지 테스트
서로 다른 터미널 두 개를 사용하면 편리합니다.

### 3-1. Consumer 실행
```bash
docker compose exec kafka /opt/bitnami/kafka/bin/kafka-console-consumer.sh \
  --topic mydata.consent.v1 \
  --bootstrap-server localhost:19092 \
  --from-beginning
```

### 3-2. Producer로 메시지 발행
다른 터미널에서 다음 메시지를 전송합니다.
```bash
echo '{"data":{"delete_event_type":"PFM_SERVICE_CLOSED_BY_USER","pay_account_id":46123695,"is_remove":true,"is_force":false},"type":"WITHDRAW"}' \
  | docker compose exec -T kafka /opt/bitnami/kafka/bin/kafka-console-producer.sh \
      --topic mydata.consent.v1 \
      --bootstrap-server localhost:19092
```

Consumer 터미널에 JSON 메시지가 출력되면 송수신이 정상적으로 동작합니다.

## 5. MySQL 초기화 확인
초기 테이블이 생성되었는지 확인합니다.
```bash
docker compose exec mysql mysql \
  -u"$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  -D "$MYSQL_DATABASE" \
  -e "SHOW TABLES;"
```
`MYDATA_TERMINATE_USER`, `PAY_TERMINATE_USER` 테이블이 보이면 초기화가 완료된 것입니다.

## 6. 정리
작업이 끝나면 컨테이너를 종료합니다.
```bash
docker compose down
```
볼륨을 따로 사용하지 않았으므로 컨테이너를 내리면 데이터가 삭제됩니다.

---
필요에 따라 3단계의 토픽과 메시지, 4단계의 쿼리를 조정해 애플리케이션 개발/테스트 흐름에 맞게 활용하세요.

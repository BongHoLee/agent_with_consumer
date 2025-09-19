#!/bin/bash

# JSON 메시지 발행 테스트 스크립트 (mydata.consent.v1)
# PoC용 테스트 데이터 생성

echo "=== JSON 메시지 발행 테스트 (mydata.consent.v1) ==="

# 파기 대상자 메시지 (is_remove = true)
echo "1. 파기 대상자 메시지 발행..."
echo '{
  "data": {
    "delete_event_type": "PFM_SERVICE_CLOSED_BY_USER",
    "pay_account_id": 46123695,
    "is_remove": true,
    "is_force": false
  },
  "type": "WITHDRAW"
}' | docker exec -i consumer-kafka kafka-console-producer.sh \
    --topic mydata.consent.v1 \
    --bootstrap-server localhost:9092

# 파기 비대상자 메시지 (is_remove = false) - 필터링될 예정
echo "2. 파기 비대상자 메시지 발행 (필터링 테스트)..."
echo '{
  "data": {
    "delete_event_type": "USER_CONSENT_WITHDRAW",
    "pay_account_id": 12345678,
    "is_remove": false,
    "is_force": false
  },
  "type": "WITHDRAW"
}' | docker exec -i consumer-kafka kafka-console-producer.sh \
    --topic mydata.consent.v1 \
    --bootstrap-server localhost:9092

# 또 다른 파기 대상자 메시지
echo "3. 추가 파기 대상자 메시지 발행..."
echo '{
  "data": {
    "delete_event_type": "SERVICE_TERMINATION",
    "pay_account_id": 87654321,
    "is_remove": true,
    "is_force": true
  },
  "type": "WITHDRAW"
}' | docker exec -i consumer-kafka kafka-console-producer.sh \
    --topic mydata.consent.v1 \
    --bootstrap-server localhost:9092

echo "=== JSON 메시지 발행 완료 ==="

# 토픽 내용 확인
echo "=== 토픽 내용 확인 ==="
docker exec consumer-kafka kafka-console-consumer.sh \
    --topic mydata.consent.v1 \
    --bootstrap-server localhost:9092 \
    --from-beginning \
    --max-messages 10 \
    --timeout-ms 5000
#!/bin/bash

# Avro 스키마 등록 및 메시지 발행 테스트 스크립트
# pay-account.payaccount-deleted.v2 토픽용

echo "=== Avro 스키마 등록 및 메시지 발행 테스트 ==="

# 1. Schema Registry 상태 확인
echo "1. Schema Registry 상태 확인..."
curl -s http://localhost:8081/subjects || echo "Schema Registry 연결 실패"

# 2. Avro 스키마 등록
echo "2. PayAccountDeletedEnvelop 스키마 등록..."
curl -X POST http://localhost:8081/subjects/pay-account.payaccount-deleted.v2-value/versions \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{
    "schema": "{\"type\":\"record\",\"name\":\"PayAccountDeletedEnvelop\",\"fields\":[{\"name\":\"uuid\",\"type\":\"string\"},{\"name\":\"occurred_at\",\"type\":\"long\"},{\"name\":\"payAccountId\",\"type\":\"long\"},{\"name\":\"reason\",\"type\":\"string\"}]}"
  }'

echo ""
echo "3. 등록된 스키마 확인..."
curl -s http://localhost:8081/subjects/pay-account.payaccount-deleted.v2-value/versions/latest

echo ""
echo "=== 스키마 등록 완료 ==="

# Note: Avro 메시지 발행은 Schema Registry와 연동된 producer가 필요하므로
# 실제 애플리케이션에서 테스트할 예정
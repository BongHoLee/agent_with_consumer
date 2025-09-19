#!/bin/bash

# Kafka Topics Creation Script for PoC Environment
# 토픽은 자동 생성이 활성화되어 있지만, 명시적으로 생성하여 파티션/복제본 수를 제어

echo "Waiting for Kafka to be ready..."
sleep 30

echo "Creating Kafka topics..."

# mydata.consent.v1 토픽 생성 (JSON 메시지)
kafka-topics --create \
  --topic mydata.consent.v1 \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=604800000 \
  --if-not-exists

# pay-account.payaccount-deleted.v2 토픽 생성 (Avro 메시지)
kafka-topics --create \
  --topic pay-account.payaccount-deleted.v2 \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=604800000 \
  --if-not-exists

echo "Listing created topics:"
kafka-topics --list --bootstrap-server localhost:9092

echo "Topic details:"
kafka-topics --describe --topic mydata.consent.v1 --bootstrap-server localhost:9092
kafka-topics --describe --topic pay-account.payaccount-deleted.v2 --bootstrap-server localhost:9092

echo "Topics creation completed!"
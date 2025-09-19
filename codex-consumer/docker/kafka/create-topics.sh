#!/usr/bin/env bash
set -euo pipefail

BOOTSTRAP_SERVER=${BOOTSTRAP_SERVER:-localhost:19092}
PARTITIONS=${PARTITIONS:-1}
REPLICATION_FACTOR=${REPLICATION_FACTOR:-1}

kafka-topics --create \
  --topic mydata.consent.v1 \
  --bootstrap-server "$BOOTSTRAP_SERVER" \
  --partitions "$PARTITIONS" \
  --replication-factor "$REPLICATION_FACTOR" \
  --if-not-exists

kafka-topics --create \
  --topic pay-account.payaccount-deleted.v2 \
  --bootstrap-server "$BOOTSTRAP_SERVER" \
  --partitions "$PARTITIONS" \
  --replication-factor "$REPLICATION_FACTOR" \
  --if-not-exists

kafka-topics --list --bootstrap-server "$BOOTSTRAP_SERVER"

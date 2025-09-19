package com.consumer.cconsumer.message.avro

import com.consumer.cconsumer.message.avro.PayAccountDeletedEnvelop
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.apache.avro.io.BinaryDecoder
import org.apache.avro.io.BinaryEncoder
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class PayAccountDeletedEnvelopTest : DescribeSpec({

    describe("PayAccountDeletedEnvelop Avro 직렬화/역직렬화 시") {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        val occurredAt = 1697360000000L // 2023-10-15T10:00:00Z in milliseconds
        val payAccountId = 12345L
        val reason = "ACCOUNT_DELETED"

        it("모든 필드로 생성될 수 있다") {
            val envelop = PayAccountDeletedEnvelop.newBuilder()
                .setUuid(uuid)
                .setOccurredAt(occurredAt)
                .setPayAccountId(payAccountId)
                .setReason(reason)
                .build()

            envelop.uuid shouldBe uuid
            envelop.occurredAt shouldBe occurredAt
            envelop.payAccountId shouldBe payAccountId
            envelop.reason shouldBe reason
        }

        it("생성자로 생성될 수 있다") {
            val envelop = PayAccountDeletedEnvelop(uuid, occurredAt, payAccountId, reason)

            envelop.uuid shouldBe uuid
            envelop.occurredAt shouldBe occurredAt
            envelop.payAccountId shouldBe payAccountId
            envelop.reason shouldBe reason
        }

        it("Avro 바이너리로 직렬화할 수 있다") {
            val envelop = PayAccountDeletedEnvelop.newBuilder()
                .setUuid(uuid)
                .setOccurredAt(occurredAt)
                .setPayAccountId(payAccountId)
                .setReason(reason)
                .build()

            val outputStream = ByteArrayOutputStream()
            val encoder: BinaryEncoder = EncoderFactory.get().binaryEncoder(outputStream, null)
            val writer = SpecificDatumWriter<PayAccountDeletedEnvelop>(PayAccountDeletedEnvelop::class.java)
            
            writer.write(envelop, encoder)
            encoder.flush()

            val serializedBytes = outputStream.toByteArray()
            serializedBytes.size shouldNotBe 0
        }

        it("Avro 바이너리에서 역직렬화할 수 있다") {
            val originalEnvelop = PayAccountDeletedEnvelop.newBuilder()
                .setUuid(uuid)
                .setOccurredAt(occurredAt)
                .setPayAccountId(payAccountId)
                .setReason(reason)
                .build()

            // 직렬화
            val outputStream = ByteArrayOutputStream()
            val encoder: BinaryEncoder = EncoderFactory.get().binaryEncoder(outputStream, null)
            val writer = SpecificDatumWriter<PayAccountDeletedEnvelop>(PayAccountDeletedEnvelop::class.java)
            writer.write(originalEnvelop, encoder)
            encoder.flush()

            // 역직렬화
            val inputStream = ByteArrayInputStream(outputStream.toByteArray())
            val decoder: BinaryDecoder = DecoderFactory.get().binaryDecoder(inputStream, null)
            val reader = SpecificDatumReader<PayAccountDeletedEnvelop>(PayAccountDeletedEnvelop::class.java)
            val deserializedEnvelop = reader.read(null, decoder)

            deserializedEnvelop.uuid shouldBe uuid
            deserializedEnvelop.occurredAt shouldBe occurredAt
            deserializedEnvelop.payAccountId shouldBe payAccountId
            deserializedEnvelop.reason shouldBe reason
        }

        it("toByteBuffer와 fromByteBuffer 메소드로 직렬화/역직렬화할 수 있다") {
            val originalEnvelop = PayAccountDeletedEnvelop.newBuilder()
                .setUuid(uuid)
                .setOccurredAt(occurredAt)
                .setPayAccountId(payAccountId)
                .setReason(reason)
                .build()

            // 직렬화
            val byteBuffer = originalEnvelop.toByteBuffer()
            byteBuffer shouldNotBe null

            // 역직렬화
            val deserializedEnvelop = PayAccountDeletedEnvelop.fromByteBuffer(byteBuffer)

            deserializedEnvelop.uuid shouldBe uuid
            deserializedEnvelop.occurredAt shouldBe occurredAt
            deserializedEnvelop.payAccountId shouldBe payAccountId
            deserializedEnvelop.reason shouldBe reason
        }

        it("스키마 정보를 제공한다") {
            val schema = PayAccountDeletedEnvelop.getClassSchema()
            
            schema shouldNotBe null
            schema.name shouldBe "PayAccountDeletedEnvelop"
            schema.namespace shouldBe "com.consumer.cconsumer.message.avro"
            schema.fields.size shouldBe 4
        }

        it("필드별 getter/setter가 정상 동작한다") {
            val envelop = PayAccountDeletedEnvelop()
            
            envelop.setUuid(uuid)
            envelop.setOccurredAt(occurredAt)
            envelop.setPayAccountId(payAccountId)
            envelop.setReason(reason)
            
            envelop.getUuid() shouldBe uuid
            envelop.getOccurredAt() shouldBe occurredAt
            envelop.getPayAccountId() shouldBe payAccountId
            envelop.getReason() shouldBe reason
        }

        it("Builder 패턴의 필드 설정/해제가 정상 동작한다") {
            val builder = PayAccountDeletedEnvelop.newBuilder()
                .setUuid(uuid)
                .setOccurredAt(occurredAt)
                .setPayAccountId(payAccountId)
                .setReason(reason)

            builder.hasUuid() shouldBe true
            builder.hasOccurredAt() shouldBe true
            builder.hasPayAccountId() shouldBe true
            builder.hasReason() shouldBe true

            builder.clearReason()
            builder.hasReason() shouldBe false
        }
    }
})
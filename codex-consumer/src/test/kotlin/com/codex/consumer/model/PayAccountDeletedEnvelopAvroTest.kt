package com.codex.consumer.model

import com.codex.consumer.model.avro.PayAccountDeletedEnvelop
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

@DisplayName("PayAccountDeletedEnvelop Avro mapping")
class PayAccountDeletedEnvelopAvroTest {

    @Nested
    @DisplayName("serialization round-trip")
    inner class SerializationRoundTrip {

        @Test
        fun `retains field values`() {
            val original = PayAccountDeletedEnvelop(
                "6517f633-eee3-4e3f-bd05-15cf98b26068",
                1_725_000_000_000L,
                912_345_678L,
                "CUSTOMER_REQUEST"
            )

            val encoded = encode(original)
            val decoded = decode(encoded)

            assertThat(decoded).isEqualTo(original)
            assertThat(decoded.reason).isEqualTo("CUSTOMER_REQUEST")
        }

        @Test
        fun `supports optional reason`() {
            val original = PayAccountDeletedEnvelop(
                "6517f633-eee3-4e3f-bd05-15cf98b26069",
                1_725_000_000_001L,
                912_345_679L,
                null
            )

            val decoded = decode(encode(original))

            assertThat(decoded.reason).isNull()
            assertThat(decoded.payAccountId).isEqualTo(912_345_679L)
        }
    }

    private fun encode(record: PayAccountDeletedEnvelop): ByteArray {
        val writer = SpecificDatumWriter(PayAccountDeletedEnvelop::class.java)
        val output = ByteArrayOutputStream()
        val encoder = EncoderFactory.get().binaryEncoder(output, null)
        writer.write(record, encoder)
        encoder.flush()
        return output.toByteArray()
    }

    private fun decode(bytes: ByteArray): PayAccountDeletedEnvelop {
        val reader = SpecificDatumReader(PayAccountDeletedEnvelop::class.java)
        val decoder = DecoderFactory.get().binaryDecoder(bytes, null)
        return reader.read(null, decoder)
    }
}

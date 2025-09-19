package com.codex.consumer.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ConsentMessage JSON mapping")
class ConsentMessageTest {

    private val objectMapper = jacksonObjectMapper()

    @Nested
    @DisplayName("deserialization")
    inner class Deserialization {

        @Test
        fun `parses sample message`() {
            val payload = """
                {
                  "data": {
                    "delete_event_type": "PFM_SERVICE_CLOSED_BY_USER",
                    "pay_account_id": 46123695,
                    "is_remove": true,
                    "is_force": false,
                    "extra_field": "ignored"
                  },
                  "type": "WITHDRAW",
                  "unexpected": "ignore me"
                }
            """.trimIndent()

            val message: ConsentMessage = objectMapper.readValue(payload)

            assertThat(message.type).isEqualTo("WITHDRAW")
            assertThat(message.data.deleteEventType).isEqualTo("PFM_SERVICE_CLOSED_BY_USER")
            assertThat(message.data.payAccountId).isEqualTo(46_123_695L)
            assertThat(message.data.isRemove).isTrue()
            assertThat(message.data.isForce).isFalse()
        }
    }

    @Nested
    @DisplayName("serialization")
    inner class Serialization {

        @Test
        fun `produces snake_case fields`() {
            val message = ConsentMessage(
                data = ConsentData(
                    deleteEventType = "PFM_SERVICE_CLOSED_BY_USER",
                    payAccountId = 46_123_695L,
                    isRemove = true,
                    isForce = false
                ),
                type = "WITHDRAW"
            )

            val json = objectMapper.writeValueAsString(message)

            assertThat(json).contains("\"delete_event_type\":")
            assertThat(json).contains("\"is_remove\":true")
            assertThat(json).contains("\"is_force\":false")
        }
    }
}

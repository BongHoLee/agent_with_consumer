package com.consumer.cconsumer.message.model

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ConsentMessageTest : DescribeSpec({

    val objectMapper = ObjectMapper()

    describe("ConsentMessage JSON 직렬화/역직렬화 시") {
        val consentData = ConsentData(
            deleteEventType = "PFM_SERVICE_CLOSED_BY_USER",
            payAccountId = 12345L,
            isRemove = true,
            isForce = false
        )
        
        val consentMessage = ConsentMessage(
            data = consentData,
            type = "CONSENT_WITHDRAWN"
        )

        it("JSON으로 직렬화할 수 있다") {
            val json = objectMapper.writeValueAsString(consentMessage)
            
            json shouldBe """{"data":{"deleteEventType":"PFM_SERVICE_CLOSED_BY_USER","payAccountId":12345,"remove":true,"force":false},"type":"CONSENT_WITHDRAWN"}"""
        }

        it("JSON에서 역직렬화할 수 있다") {
            val json = """{"data":{"delete_event_type":"PFM_SERVICE_CLOSED_BY_USER","pay_account_id":12345,"is_remove":true,"is_force":false},"type":"CONSENT_WITHDRAWN"}"""
            
            val deserializedMessage = objectMapper.readValue(json, ConsentMessage::class.java)
            
            deserializedMessage.type shouldBe "CONSENT_WITHDRAWN"
            deserializedMessage.data.deleteEventType shouldBe "PFM_SERVICE_CLOSED_BY_USER"
            deserializedMessage.data.payAccountId shouldBe 12345L
            deserializedMessage.data.isRemove shouldBe true
            deserializedMessage.data.isForce shouldBe false
        }
    }

    describe("ConsentData JSON 직렬화/역직렬화 시") {
        val consentData = ConsentData(
            deleteEventType = "ACCOUNT_DELETED",
            payAccountId = 67890L,
            isRemove = false,
            isForce = true
        )

        it("JSON으로 직렬화할 수 있다") {
            val json = objectMapper.writeValueAsString(consentData)
            
            json shouldBe """{"deleteEventType":"ACCOUNT_DELETED","payAccountId":67890,"remove":false,"force":true}"""
        }

        it("JSON에서 역직렬화할 수 있다") {
            val json = """{"delete_event_type":"ACCOUNT_DELETED","pay_account_id":67890,"is_remove":false,"is_force":true}"""
            
            val deserializedData = objectMapper.readValue(json, ConsentData::class.java)
            
            deserializedData.deleteEventType shouldBe "ACCOUNT_DELETED"
            deserializedData.payAccountId shouldBe 67890L
            deserializedData.isRemove shouldBe false
            deserializedData.isForce shouldBe true
        }
    }

    describe("필터링 로직 검증을 위한 테스트") {
        it("is_remove가 true인 메시지를 식별할 수 있다") {
            val json = """{"data":{"delete_event_type":"USER_CONSENT_WITHDRAWN","pay_account_id":12345,"is_remove":true,"is_force":false},"type":"CONSENT_EVENT"}"""
            
            val message = objectMapper.readValue(json, ConsentMessage::class.java)
            
            message.data.isRemove shouldBe true
        }

        it("is_remove가 false인 메시지를 식별할 수 있다") {
            val json = """{"data":{"delete_event_type":"USER_CONSENT_GRANTED","pay_account_id":12345,"is_remove":false,"is_force":false},"type":"CONSENT_EVENT"}"""
            
            val message = objectMapper.readValue(json, ConsentMessage::class.java)
            
            message.data.isRemove shouldBe false
        }
    }
})
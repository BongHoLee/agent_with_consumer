package com.consumer.cconsumer.domain.entity

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PayTerminateUserTest {

    @Test
    fun `PayTerminateUser should be created with required fields`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        val entity = PayTerminateUser(
            payAccountId = payAccountId,
            reason = reason
        )
        
        assertEquals(payAccountId, entity.payAccountId)
        assertEquals(reason, entity.reason)
        assertEquals(TerminateStatus.PENDING, entity.terminateStatus)
        assertEquals(0L, entity.id) // default value before persistence
        assertNotNull(entity.createdAt)
        assertNotNull(entity.updatedAt)
    }

    @Test
    fun `PayTerminateUser should be created with custom terminate status`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        val entity = PayTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.COMPLETED,
            reason = reason
        )
        
        assertEquals(payAccountId, entity.payAccountId)
        assertEquals(reason, entity.reason)
        assertEquals(TerminateStatus.COMPLETED, entity.terminateStatus)
    }

    @Test
    fun `PayTerminateUser should be created without reason`() {
        val payAccountId = 67890L
        
        val entity = PayTerminateUser(
            payAccountId = payAccountId
        )
        
        assertEquals(payAccountId, entity.payAccountId)
        assertEquals(null, entity.reason)
        assertEquals(TerminateStatus.PENDING, entity.terminateStatus)
    }

    @Test
    fun `PayTerminateUser should extend BaseEntity`() {
        val entity = PayTerminateUser(payAccountId = 67890L)
        
        assert(entity is BaseEntity)
    }
}
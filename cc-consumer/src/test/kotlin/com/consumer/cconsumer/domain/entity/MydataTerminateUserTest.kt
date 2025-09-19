package com.consumer.cconsumer.domain.entity

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MydataTerminateUserTest {

    @Test
    fun `MydataTerminateUser should be created with required fields`() {
        val payAccountId = 12345L
        val reason = "PFM_SERVICE_CLOSED_BY_USER"
        
        val entity = MydataTerminateUser(
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
    fun `MydataTerminateUser should be created with custom terminate status`() {
        val payAccountId = 12345L
        val reason = "PFM_SERVICE_CLOSED_BY_USER"
        
        val entity = MydataTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.COMPLETED,
            reason = reason
        )
        
        assertEquals(payAccountId, entity.payAccountId)
        assertEquals(reason, entity.reason)
        assertEquals(TerminateStatus.COMPLETED, entity.terminateStatus)
    }

    @Test
    fun `MydataTerminateUser should be created without reason`() {
        val payAccountId = 12345L
        
        val entity = MydataTerminateUser(
            payAccountId = payAccountId
        )
        
        assertEquals(payAccountId, entity.payAccountId)
        assertEquals(null, entity.reason)
        assertEquals(TerminateStatus.PENDING, entity.terminateStatus)
    }

    @Test
    fun `MydataTerminateUser should extend BaseEntity`() {
        val entity = MydataTerminateUser(payAccountId = 12345L)
        
        assert(entity is BaseEntity)
    }
}
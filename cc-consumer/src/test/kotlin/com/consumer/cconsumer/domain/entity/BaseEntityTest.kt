package com.consumer.cconsumer.domain.entity

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BaseEntityTest {

    @Test
    fun `BaseEntity should have default id value of 0`() {
        val entity = MydataTerminateUser(payAccountId = 12345L)
        
        assertEquals(0L, entity.id)
        assertNotNull(entity.createdAt)
        assertNotNull(entity.updatedAt)
    }

    @Test
    fun `BaseEntity should have timestamps initialized`() {
        val entity = PayTerminateUser(payAccountId = 67890L)
        
        assertNotNull(entity.createdAt)
        assertNotNull(entity.updatedAt)
    }
}
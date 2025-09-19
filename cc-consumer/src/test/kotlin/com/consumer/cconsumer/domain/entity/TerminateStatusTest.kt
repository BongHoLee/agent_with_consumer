package com.consumer.cconsumer.domain.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TerminateStatusTest {

    @Test
    fun `TerminateStatus enum should have PENDING and COMPLETED values`() {
        val values = TerminateStatus.values()
        
        assertEquals(2, values.size)
        assertTrue(values.contains(TerminateStatus.PENDING))
        assertTrue(values.contains(TerminateStatus.COMPLETED))
    }

    @Test
    fun `TerminateStatus should be convertible to string`() {
        assertEquals("PENDING", TerminateStatus.PENDING.name)
        assertEquals("COMPLETED", TerminateStatus.COMPLETED.name)
    }

    @Test
    fun `TerminateStatus should be parseable from string`() {
        assertDoesNotThrow {
            assertEquals(TerminateStatus.PENDING, TerminateStatus.valueOf("PENDING"))
            assertEquals(TerminateStatus.COMPLETED, TerminateStatus.valueOf("COMPLETED"))
        }
    }

    @Test
    fun `TerminateStatus ordinal should maintain order`() {
        assertEquals(0, TerminateStatus.PENDING.ordinal)
        assertEquals(1, TerminateStatus.COMPLETED.ordinal)
    }
}
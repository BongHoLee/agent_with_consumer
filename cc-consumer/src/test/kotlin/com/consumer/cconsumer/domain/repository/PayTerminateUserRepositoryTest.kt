package com.consumer.cconsumer.domain.repository

import com.consumer.cconsumer.domain.entity.PayTerminateUser
import com.consumer.cconsumer.domain.entity.TerminateStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
class PayTerminateUserRepositoryTest {

    @Autowired
    private lateinit var repository: PayTerminateUserRepository

    @Test
    fun `should save and retrieve PayTerminateUser`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        val entity = PayTerminateUser(
            payAccountId = payAccountId,
            reason = reason
        )
        
        // Before save, id should be 0 (default)
        assertEquals(0L, entity.id)
        
        val saved = repository.save(entity)
        
        // After save, id should be auto-generated (not 0)
        assertNotNull(saved.id)
        assert(saved.id > 0) { "ID should be auto-generated and greater than 0, but was ${saved.id}" }
        assertEquals(payAccountId, saved.payAccountId)
        assertEquals(reason, saved.reason)
        assertEquals(TerminateStatus.PENDING, saved.terminateStatus)
        assertNotNull(saved.createdAt)
        assertNotNull(saved.updatedAt)
    }

    @Test
    fun `should find by pay account id and terminate status`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        val entity = PayTerminateUser(
            payAccountId = payAccountId,
            reason = reason
        )
        
        repository.save(entity)
        
        val found = repository.findByPayAccountIdAndTerminateStatus(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING
        )
        
        assertNotNull(found)
        assertEquals(payAccountId, found.payAccountId)
        assertEquals(TerminateStatus.PENDING, found.terminateStatus)
    }

    @Test
    fun `should return null when no matching record found`() {
        val found = repository.findByPayAccountIdAndTerminateStatus(
            payAccountId = 99999L,
            terminateStatus = TerminateStatus.PENDING
        )
        
        assertNull(found)
    }

    @Test
    fun `should enforce unique constraint on pay_account_id and terminate_status`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        // First entity
        val entity1 = PayTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        repository.save(entity1)
        
        // Second entity with same payAccountId and terminateStatus
        val entity2 = PayTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        
        // Should throw exception due to unique constraint violation
        assertThrows<DataIntegrityViolationException> {
            repository.save(entity2)
            repository.flush() // Force execution
        }
    }

    @Test
    fun `should allow same pay_account_id with different terminate_status`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        // PENDING status
        val pendingEntity = PayTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        repository.save(pendingEntity)
        
        // COMPLETED status - should be allowed
        val completedEntity = PayTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.COMPLETED,
            reason = reason
        )
        
        val saved = repository.save(completedEntity)
        
        assertNotNull(saved.id)
        assertEquals(payAccountId, saved.payAccountId)
        assertEquals(TerminateStatus.COMPLETED, saved.terminateStatus)
        
        // Verify both records exist
        val pendingRecord = repository.findByPayAccountIdAndTerminateStatus(
            payAccountId, TerminateStatus.PENDING
        )
        val completedRecord = repository.findByPayAccountIdAndTerminateStatus(
            payAccountId, TerminateStatus.COMPLETED
        )
        
        assertNotNull(pendingRecord)
        assertNotNull(completedRecord)
    }

    @Test
    fun `should allow different pay_account_id with same terminate_status`() {
        val reason = "ACCOUNT_DELETED"
        
        val entity1 = PayTerminateUser(
            payAccountId = 12345L,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        repository.save(entity1)
        
        val entity2 = PayTerminateUser(
            payAccountId = 67890L,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        
        val saved = repository.save(entity2)
        
        assertNotNull(saved.id)
        assertEquals(67890L, saved.payAccountId)
        assertEquals(TerminateStatus.PENDING, saved.terminateStatus)
    }
}
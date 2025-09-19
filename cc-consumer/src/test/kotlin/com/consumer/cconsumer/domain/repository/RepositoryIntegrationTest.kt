package com.consumer.cconsumer.domain.repository

import com.consumer.cconsumer.domain.entity.MydataTerminateUser
import com.consumer.cconsumer.domain.entity.PayTerminateUser
import com.consumer.cconsumer.domain.entity.TerminateStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@TestPropertySource(locations = ["classpath:application.yml"])
@Transactional
class RepositoryIntegrationTest {

    @Autowired
    private lateinit var mydataRepository: MydataTerminateUserRepository

    @Autowired
    private lateinit var payRepository: PayTerminateUserRepository

    @Test
    fun `MydataTerminateUser should save and retrieve correctly`() {
        val payAccountId = 12345L
        val reason = "PFM_SERVICE_CLOSED_BY_USER"
        
        val entity = MydataTerminateUser(
            payAccountId = payAccountId,
            reason = reason
        )
        
        val saved = mydataRepository.save(entity)
        
        assertNotNull(saved.id)
        assertEquals(payAccountId, saved.payAccountId)
        assertEquals(reason, saved.reason)
        assertEquals(TerminateStatus.PENDING, saved.terminateStatus)
    }

    @Test
    fun `PayTerminateUser should save and retrieve correctly`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        val entity = PayTerminateUser(
            payAccountId = payAccountId,
            reason = reason
        )
        
        val saved = payRepository.save(entity)
        
        assertNotNull(saved.id)
        assertEquals(payAccountId, saved.payAccountId)
        assertEquals(reason, saved.reason)
        assertEquals(TerminateStatus.PENDING, saved.terminateStatus)
    }

    @Test
    fun `should find MydataTerminateUser by pay account id and terminate status`() {
        val payAccountId = 12345L
        val reason = "PFM_SERVICE_CLOSED_BY_USER"
        
        val entity = MydataTerminateUser(
            payAccountId = payAccountId,
            reason = reason
        )
        
        mydataRepository.save(entity)
        
        val found = mydataRepository.findByPayAccountIdAndTerminateStatus(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING
        )
        
        assertNotNull(found)
        assertEquals(payAccountId, found.payAccountId)
        assertEquals(TerminateStatus.PENDING, found.terminateStatus)
    }

    @Test
    fun `should find PayTerminateUser by pay account id and terminate status`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        val entity = PayTerminateUser(
            payAccountId = payAccountId,
            reason = reason
        )
        
        payRepository.save(entity)
        
        val found = payRepository.findByPayAccountIdAndTerminateStatus(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING
        )
        
        assertNotNull(found)
        assertEquals(payAccountId, found.payAccountId)
        assertEquals(TerminateStatus.PENDING, found.terminateStatus)
    }

    @Test
    fun `should return null when no matching MydataTerminateUser found`() {
        val found = mydataRepository.findByPayAccountIdAndTerminateStatus(
            payAccountId = 99999L,
            terminateStatus = TerminateStatus.PENDING
        )
        
        assertNull(found)
    }

    @Test
    fun `should return null when no matching PayTerminateUser found`() {
        val found = payRepository.findByPayAccountIdAndTerminateStatus(
            payAccountId = 99999L,
            terminateStatus = TerminateStatus.PENDING
        )
        
        assertNull(found)
    }

    @Test
    fun `should enforce unique constraint on MydataTerminateUser`() {
        val payAccountId = 12345L
        val reason = "PFM_SERVICE_CLOSED_BY_USER"
        
        // First entity
        val entity1 = MydataTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        mydataRepository.save(entity1)
        mydataRepository.flush()
        
        // Second entity with same payAccountId and terminateStatus
        val entity2 = MydataTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        
        // Should throw exception due to unique constraint violation
        assertThrows<DataIntegrityViolationException> {
            mydataRepository.save(entity2)
            mydataRepository.flush()
        }
    }

    @Test
    fun `should enforce unique constraint on PayTerminateUser`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        // First entity
        val entity1 = PayTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        payRepository.save(entity1)
        payRepository.flush()
        
        // Second entity with same payAccountId and terminateStatus
        val entity2 = PayTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        
        // Should throw exception due to unique constraint violation
        assertThrows<DataIntegrityViolationException> {
            payRepository.save(entity2)
            payRepository.flush()
        }
    }

    @Test
    fun `should allow same pay_account_id with different terminate_status for MydataTerminateUser`() {
        val payAccountId = 12345L
        val reason = "PFM_SERVICE_CLOSED_BY_USER"
        
        // PENDING status
        val pendingEntity = MydataTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        mydataRepository.save(pendingEntity)
        
        // COMPLETED status - should be allowed
        val completedEntity = MydataTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.COMPLETED,
            reason = reason
        )
        
        val saved = mydataRepository.save(completedEntity)
        
        assertNotNull(saved.id)
        assertEquals(payAccountId, saved.payAccountId)
        assertEquals(TerminateStatus.COMPLETED, saved.terminateStatus)
        
        // Verify both records exist
        val pendingRecord = mydataRepository.findByPayAccountIdAndTerminateStatus(
            payAccountId, TerminateStatus.PENDING
        )
        val completedRecord = mydataRepository.findByPayAccountIdAndTerminateStatus(
            payAccountId, TerminateStatus.COMPLETED
        )
        
        assertNotNull(pendingRecord)
        assertNotNull(completedRecord)
    }

    @Test
    fun `should allow same pay_account_id with different terminate_status for PayTerminateUser`() {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"
        
        // PENDING status
        val pendingEntity = PayTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )
        payRepository.save(pendingEntity)
        
        // COMPLETED status - should be allowed
        val completedEntity = PayTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.COMPLETED,
            reason = reason
        )
        
        val saved = payRepository.save(completedEntity)
        
        assertNotNull(saved.id)
        assertEquals(payAccountId, saved.payAccountId)
        assertEquals(TerminateStatus.COMPLETED, saved.terminateStatus)
        
        // Verify both records exist
        val pendingRecord = payRepository.findByPayAccountIdAndTerminateStatus(
            payAccountId, TerminateStatus.PENDING
        )
        val completedRecord = payRepository.findByPayAccountIdAndTerminateStatus(
            payAccountId, TerminateStatus.COMPLETED
        )
        
        assertNotNull(pendingRecord)
        assertNotNull(completedRecord)
    }
}
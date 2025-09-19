package com.codex.consumer.domain.repository

import com.codex.consumer.domain.entity.PayTerminateUser
import com.codex.consumer.domain.entity.TerminateStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

@DataJpaTest(properties = [
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create"
])
@DisplayName("PayTerminateUserRepository")
class PayTerminateUserRepositoryTest @Autowired constructor(
    private val repository: PayTerminateUserRepository
) {

    @Nested
    @DisplayName("save")
    inner class Save {

        @Test
        fun `assigns auto incremented id`() {
            val first = repository.saveAndFlush(payTerminateUser(payAccountId = 100L, reason = "first"))
            val second = repository.saveAndFlush(payTerminateUser(payAccountId = 101L, reason = "second"))

            assertThat(first.id).isNotNull
            assertThat(second.id).isNotNull
            assertThat(second.id).isGreaterThan(first.id)
        }

        @Test
        fun `rejects duplicate pending record for same payAccountId`() {
            repository.saveAndFlush(payTerminateUser(payAccountId = 200L))

            val violation = assertThrows<DataIntegrityViolationException> {
                repository.saveAndFlush(payTerminateUser(payAccountId = 200L))
            }

            assertThat(violation.mostSpecificCause::class.simpleName)
                .isEqualTo("JdbcSQLIntegrityConstraintViolationException")
        }

        @Test
        fun `allows re-registration after completion`() {
            val initial = repository.saveAndFlush(payTerminateUser(payAccountId = 300L, reason = "initial"))
            initial.terminateStatus = TerminateStatus.COMPLETED
            repository.saveAndFlush(initial)

            val reRegistered = repository.saveAndFlush(payTerminateUser(payAccountId = 300L, reason = "second run"))

            val stored = repository.findAll().filter { it.payAccountId == 300L }
            assertThat(stored).hasSize(2)
            assertThat(stored.map { it.terminateStatus }).containsExactlyInAnyOrder(
                TerminateStatus.COMPLETED,
                TerminateStatus.PENDING
            )
            assertThat(reRegistered.terminateStatus).isEqualTo(TerminateStatus.PENDING)
        }
    }

    @Nested
    @DisplayName("findByPayAccountIdAndTerminateStatus")
    inner class FindByCompositeKey {

        @Test
        fun `returns matching pending record`() {
            val saved = repository.saveAndFlush(payTerminateUser(payAccountId = 400L, reason = "persisted"))

            val found = repository.findByPayAccountIdAndTerminateStatus(400L, TerminateStatus.PENDING)

            assertThat(found).isNotNull
            assertThat(found!!.id).isEqualTo(saved.id)
            assertThat(found.terminateStatus).isEqualTo(TerminateStatus.PENDING)
        }

        @Test
        fun `returns null when status does not match`() {
            repository.saveAndFlush(payTerminateUser(payAccountId = 500L, reason = "persisted"))

            val absent = repository.findByPayAccountIdAndTerminateStatus(500L, TerminateStatus.COMPLETED)

            assertThat(absent).isNull()
        }
    }
}

private fun payTerminateUser(
    payAccountId: Long,
    status: TerminateStatus = TerminateStatus.PENDING,
    reason: String? = null
) = PayTerminateUser(
    payAccountId = payAccountId,
    terminateStatus = status,
    reason = reason
)

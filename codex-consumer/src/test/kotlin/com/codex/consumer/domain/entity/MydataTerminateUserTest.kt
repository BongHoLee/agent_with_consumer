package com.codex.consumer.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("MydataTerminateUser")
class MydataTerminateUserTest {

    @Nested
    @DisplayName("constructor")
    inner class Constructor {

        @Test
        fun `defaults terminateStatus to PENDING`() {
            val entity = MydataTerminateUser(payAccountId = 1L, reason = "sample")

            assertThat(entity.terminateStatus).isEqualTo(TerminateStatus.PENDING)
            assertThat(entity.id).isNull()
        }

        @Test
        fun `accepts explicit terminateStatus`() {
            val entity = MydataTerminateUser(
                payAccountId = 1L,
                terminateStatus = TerminateStatus.COMPLETED,
                reason = "processed"
            )

            assertThat(entity.terminateStatus).isEqualTo(TerminateStatus.COMPLETED)
            assertThat(entity.reason).isEqualTo("processed")
        }
    }

    @Nested
    @DisplayName("status mutation")
    inner class StatusMutation {

        @Test
        fun `allows status transition`() {
            val entity = MydataTerminateUser(payAccountId = 1L)

            entity.terminateStatus = TerminateStatus.COMPLETED

            assertThat(entity.terminateStatus).isEqualTo(TerminateStatus.COMPLETED)
        }
    }
}

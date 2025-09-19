package com.codex.consumer.domain.repository

import com.codex.consumer.domain.entity.PayTerminateUser
import com.codex.consumer.domain.entity.TerminateStatus
import org.springframework.data.jpa.repository.JpaRepository

interface PayTerminateUserRepository : JpaRepository<PayTerminateUser, Long> {
    fun findByPayAccountIdAndTerminateStatus(
        payAccountId: Long,
        terminateStatus: TerminateStatus
    ): PayTerminateUser?
}

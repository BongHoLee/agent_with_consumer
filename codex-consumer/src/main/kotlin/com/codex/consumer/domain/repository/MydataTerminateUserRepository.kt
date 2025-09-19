package com.codex.consumer.domain.repository

import com.codex.consumer.domain.entity.MydataTerminateUser
import com.codex.consumer.domain.entity.TerminateStatus
import org.springframework.data.jpa.repository.JpaRepository

interface MydataTerminateUserRepository : JpaRepository<MydataTerminateUser, Long> {
    fun findByPayAccountIdAndTerminateStatus(
        payAccountId: Long,
        terminateStatus: TerminateStatus
    ): MydataTerminateUser?
}

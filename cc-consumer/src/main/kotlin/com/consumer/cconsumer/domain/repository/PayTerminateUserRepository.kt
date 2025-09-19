package com.consumer.cconsumer.domain.repository

import com.consumer.cconsumer.domain.entity.PayTerminateUser
import com.consumer.cconsumer.domain.entity.TerminateStatus
import org.springframework.data.jpa.repository.JpaRepository

interface PayTerminateUserRepository : JpaRepository<PayTerminateUser, Long> {
    
    fun findByPayAccountIdAndTerminateStatus(
        payAccountId: Long,
        terminateStatus: TerminateStatus
    ): PayTerminateUser?
}
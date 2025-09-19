package com.consumer.cconsumer.domain.repository

import com.consumer.cconsumer.domain.entity.MydataTerminateUser
import com.consumer.cconsumer.domain.entity.TerminateStatus
import org.springframework.data.jpa.repository.JpaRepository

interface MydataTerminateUserRepository : JpaRepository<MydataTerminateUser, Long> {
    
    fun findByPayAccountIdAndTerminateStatus(
        payAccountId: Long,
        terminateStatus: TerminateStatus
    ): MydataTerminateUser?
}
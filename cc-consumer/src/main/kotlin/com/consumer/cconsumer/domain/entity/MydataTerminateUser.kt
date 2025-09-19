package com.consumer.cconsumer.domain.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "MYDATA_TERMINATE_USER",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_pay_account_terminate_status",
            columnNames = ["pay_account_id", "terminate_status"]
        )
    ]
)
class MydataTerminateUser(
    @Column(name = "pay_account_id", nullable = false)
    val payAccountId: Long,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "terminate_status", nullable = false, length = 20)
    val terminateStatus: TerminateStatus = TerminateStatus.PENDING,
    
    @Column(name = "reason")
    val reason: String? = null
) : BaseEntity()
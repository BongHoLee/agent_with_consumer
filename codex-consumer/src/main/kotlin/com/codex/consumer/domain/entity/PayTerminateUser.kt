package com.codex.consumer.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "PAY_TERMINATE_USER",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_pay_pay_account_status",
            columnNames = ["pay_account_id", "terminate_status"]
        )
    ]
)
class PayTerminateUser(
    @Column(name = "pay_account_id", nullable = false)
    var payAccountId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "terminate_status", nullable = false)
    var terminateStatus: TerminateStatus = TerminateStatus.PENDING,

    @Column(name = "reason")
    var reason: String? = null
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    var id: Long? = null
        private set
}

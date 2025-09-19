package com.consumer.cconsumer.domain.entity

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class PayTerminateUserTest : DescribeSpec({

    describe("PayTerminateUser 엔티티 생성 시") {
        val payAccountId = 67890L
        val reason = "ACCOUNT_DELETED"

        it("필수 필드와 기본값으로 생성될 수 있다") {
            val entity = PayTerminateUser(
                payAccountId = payAccountId,
                reason = reason
            )

            entity.payAccountId shouldBe payAccountId
            entity.reason shouldBe reason
            entity.terminateStatus shouldBe TerminateStatus.PENDING
            entity.id shouldBe 0L // default value before persistence
            entity.createdAt shouldNotBe null
            entity.updatedAt shouldNotBe null
        }

        it("사용자 정의 파기 상태로 생성될 수 있다") {
            val entity = PayTerminateUser(
                payAccountId = payAccountId,
                terminateStatus = TerminateStatus.COMPLETED,
                reason = reason
            )

            entity.payAccountId shouldBe payAccountId
            entity.reason shouldBe reason
            entity.terminateStatus shouldBe TerminateStatus.COMPLETED
        }

        it("사유 없이 생성될 수 있다") {
            val entity = PayTerminateUser(
                payAccountId = payAccountId
            )

            entity.payAccountId shouldBe payAccountId
            entity.reason shouldBe null
            entity.terminateStatus shouldBe TerminateStatus.PENDING
        }

        it("BaseEntity를 상속한다") {
            val entity = PayTerminateUser(payAccountId = payAccountId)

            entity.shouldBeInstanceOf<BaseEntity>()
        }
    }
})
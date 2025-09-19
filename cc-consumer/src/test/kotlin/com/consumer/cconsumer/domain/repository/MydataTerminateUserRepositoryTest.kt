package com.consumer.cconsumer.domain.repository

import com.consumer.cconsumer.domain.entity.MydataTerminateUser
import com.consumer.cconsumer.domain.entity.TerminateStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

@DataJpaTest
class MydataTerminateUserRepositoryTest : DescribeSpec() {

    @Autowired
    private lateinit var repository: MydataTerminateUserRepository

    override fun extensions() = listOf(SpringExtension)

    init {
        describe("MydataTerminateUserRepository는") {

            val payAccountId = 12345L
            val reason = "PFM_SERVICE_CLOSED_BY_USER"

            it("MydataTerminateUser를 저장하고 조회할 수 있다") {
                val entity = MydataTerminateUser(
                    payAccountId = payAccountId,
                    reason = reason
                )
                entity.id shouldBe 0L

                val saved = repository.save(entity)

                saved.id shouldNotBe null
                saved.id shouldBeGreaterThan 0L
                saved.payAccountId shouldBe payAccountId
                saved.reason shouldBe reason
                saved.terminateStatus shouldBe TerminateStatus.PENDING
                saved.createdAt shouldNotBe null
                saved.updatedAt shouldNotBe null
            }

            it("pay_account_id와 terminate_status로 조회할 수 있다") {
                val entity = MydataTerminateUser(
                    payAccountId = payAccountId,
                    reason = reason
                )
                repository.save(entity)

                val found = repository.findByPayAccountIdAndTerminateStatus(
                    payAccountId = payAccountId,
                    terminateStatus = TerminateStatus.PENDING
                )

                found shouldNotBe null
                found?.payAccountId shouldBe payAccountId
                found?.terminateStatus shouldBe TerminateStatus.PENDING
            }

            it("일치하는 레코드가 없으면 null을 반환한다") {
                val found = repository.findByPayAccountIdAndTerminateStatus(
                    payAccountId = 99999L,
                    terminateStatus = TerminateStatus.PENDING
                )

                found shouldBe null
            }

            it("pay_account_id와 terminate_status에 대한 유니크 제약조건을 강제한다") {
                val entity1 = MydataTerminateUser(
                    payAccountId = payAccountId,
                    terminateStatus = TerminateStatus.PENDING,
                    reason = reason
                )
                repository.save(entity1)

                val entity2 = MydataTerminateUser(
                    payAccountId = payAccountId,
                    terminateStatus = TerminateStatus.PENDING,
                    reason = reason
                )

                shouldThrow<DataIntegrityViolationException> {
                    repository.saveAndFlush(entity2)
                }
            }

            it("terminate_status가 다르면 동일한 pay_account_id를 허용한다") {
                val pendingEntity = MydataTerminateUser(
                    payAccountId = payAccountId,
                    terminateStatus = TerminateStatus.PENDING,
                    reason = reason
                )
                repository.save(pendingEntity)

                val completedEntity = MydataTerminateUser(
                    payAccountId = payAccountId,
                    terminateStatus = TerminateStatus.COMPLETED,
                    reason = reason
                )
                val saved = repository.save(completedEntity)

                saved.id shouldNotBe null
                saved.payAccountId shouldBe payAccountId
                saved.terminateStatus shouldBe TerminateStatus.COMPLETED

                val pendingRecord = repository.findByPayAccountIdAndTerminateStatus(
                    payAccountId, TerminateStatus.PENDING
                )
                val completedRecord = repository.findByPayAccountIdAndTerminateStatus(
                    payAccountId, TerminateStatus.COMPLETED
                )

                pendingRecord shouldNotBe null
                completedRecord shouldNotBe null
            }

            it("pay_account_id가 다르면 동일한 terminate_status를 허용한다") {
                val entity1 = MydataTerminateUser(
                    payAccountId = 12345L,
                    terminateStatus = TerminateStatus.PENDING,
                    reason = reason
                )
                repository.save(entity1)

                val entity2 = MydataTerminateUser(
                    payAccountId = 67890L,
                    terminateStatus = TerminateStatus.PENDING,
                    reason = reason
                )
                val saved = repository.save(entity2)

                saved.id shouldNotBe null
                saved.payAccountId shouldBe 67890L
                saved.terminateStatus shouldBe TerminateStatus.PENDING
            }
        }
    }
}
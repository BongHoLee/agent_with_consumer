package com.consumer.cconsumer.domain.repository

import com.consumer.cconsumer.domain.entity.MydataTerminateUser
import com.consumer.cconsumer.domain.entity.PayTerminateUser
import com.consumer.cconsumer.domain.entity.TerminateStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestPropertySource(locations = ["classpath:application.yml"])
@Transactional
class RepositoryIntegrationTest : DescribeSpec() {

    @Autowired
    private lateinit var mydataRepository: MydataTerminateUserRepository

    @Autowired
    private lateinit var payRepository: PayTerminateUserRepository

    override fun extensions() = listOf(SpringExtension)

    init {
        describe("레포지토리 통합 테스트") {

            describe("MydataTerminateUser의 경우") {
                val payAccountId = 12345L
                val reason = "PFM_SERVICE_CLOSED_BY_USER"

                it("정상적으로 저장하고 조회한다") {
                    val entity = MydataTerminateUser(payAccountId = payAccountId, reason = reason)
                    val saved = mydataRepository.save(entity)

                    saved.id shouldNotBe null
                    saved.payAccountId shouldBe payAccountId
                    saved.reason shouldBe reason
                    saved.terminateStatus shouldBe TerminateStatus.PENDING
                }

                it("pay_account_id와 terminate_status로 조회한다") {
                    val entity = MydataTerminateUser(payAccountId = payAccountId, reason = reason)
                    mydataRepository.save(entity)

                    val found = mydataRepository.findByPayAccountIdAndTerminateStatus(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.PENDING
                    )

                    found shouldNotBe null
                    found?.payAccountId shouldBe payAccountId
                    found?.terminateStatus shouldBe TerminateStatus.PENDING
                }
                
                it("일치하는 레코드가 없으면 null을 반환한다") {
                    val found = mydataRepository.findByPayAccountIdAndTerminateStatus(
                        payAccountId = 99999L,
                        terminateStatus = TerminateStatus.PENDING
                    )
                    found shouldBe null
                }

                it("유니크 제약조건을 강제한다") {
                    val entity1 = MydataTerminateUser(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.PENDING,
                        reason = reason
                    )
                    mydataRepository.saveAndFlush(entity1)

                    val entity2 = MydataTerminateUser(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.PENDING,
                        reason = reason
                    )

                    shouldThrow<DataIntegrityViolationException> {
                        mydataRepository.saveAndFlush(entity2)
                    }
                }

                it("terminate_status가 다르면 동일한 pay_account_id를 허용한다") {
                    val pendingEntity = MydataTerminateUser(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.PENDING,
                        reason = reason
                    )
                    mydataRepository.save(pendingEntity)

                    val completedEntity = MydataTerminateUser(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.COMPLETED,
                        reason = reason
                    )
                    val saved = mydataRepository.save(completedEntity)

                    saved.id shouldNotBe null
                    
                    val pendingRecord = mydataRepository.findByPayAccountIdAndTerminateStatus(
                        payAccountId, TerminateStatus.PENDING
                    )
                    val completedRecord = mydataRepository.findByPayAccountIdAndTerminateStatus(
                        payAccountId, TerminateStatus.COMPLETED
                    )

                    pendingRecord shouldNotBe null
                    completedRecord shouldNotBe null
                }
            }

            describe("PayTerminateUser의 경우") {
                val payAccountId = 67890L
                val reason = "ACCOUNT_DELETED"

                it("정상적으로 저장하고 조회한다") {
                    val entity = PayTerminateUser(payAccountId = payAccountId, reason = reason)
                    val saved = payRepository.save(entity)

                    saved.id shouldNotBe null
                    saved.payAccountId shouldBe payAccountId
                    saved.reason shouldBe reason
                    saved.terminateStatus shouldBe TerminateStatus.PENDING
                }

                it("pay_account_id와 terminate_status로 조회한다") {
                    val entity = PayTerminateUser(payAccountId = payAccountId, reason = reason)
                    payRepository.save(entity)

                    val found = payRepository.findByPayAccountIdAndTerminateStatus(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.PENDING
                    )

                    found shouldNotBe null
                    found?.payAccountId shouldBe payAccountId
                    found?.terminateStatus shouldBe TerminateStatus.PENDING
                }
                
                it("일치하는 레코드가 없으면 null을 반환한다") {
                    val found = payRepository.findByPayAccountIdAndTerminateStatus(
                        payAccountId = 99999L,
                        terminateStatus = TerminateStatus.PENDING
                    )
                    found shouldBe null
                }

                it("유니크 제약조건을 강제한다") {
                    val entity1 = PayTerminateUser(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.PENDING,
                        reason = reason
                    )
                    payRepository.saveAndFlush(entity1)

                    val entity2 = PayTerminateUser(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.PENDING,
                        reason = reason
                    )

                    shouldThrow<DataIntegrityViolationException> {
                        payRepository.saveAndFlush(entity2)
                    }
                }

                it("terminate_status가 다르면 동일한 pay_account_id를 허용한다") {
                    val pendingEntity = PayTerminateUser(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.PENDING,
                        reason = reason
                    )
                    payRepository.save(pendingEntity)

                    val completedEntity = PayTerminateUser(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.COMPLETED,
                        reason = reason
                    )
                    val saved = payRepository.save(completedEntity)

                    saved.id shouldNotBe null
                    
                    val pendingRecord = payRepository.findByPayAccountIdAndTerminateStatus(
                        payAccountId, TerminateStatus.PENDING
                    )
                    val completedRecord = payRepository.findByPayAccountIdAndTerminateStatus(
                        payAccountId, TerminateStatus.COMPLETED
                    )

                    pendingRecord shouldNotBe null
                    completedRecord shouldNotBe null
                }
            }
        }
    }
}
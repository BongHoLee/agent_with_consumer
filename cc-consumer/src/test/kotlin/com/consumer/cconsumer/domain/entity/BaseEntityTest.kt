package com.consumer.cconsumer.domain.entity

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldNotBe

class BaseEntityTest : DescribeSpec({

    describe("BaseEntity 엔티티는") {
        it("기본 id 값으로 0을 가지며, 생성/수정 시간은 초기화된다") {
            val entity = MydataTerminateUser(payAccountId = 12345L)

            entity.id shouldBeExactly 0L
            entity.createdAt shouldNotBe null
            entity.updatedAt shouldNotBe null
        }

        it("다른 엔티티 타입에 대해서도 생성/수정 시간은 초기화된다") {
            val entity = PayTerminateUser(payAccountId = 67890L)

            entity.createdAt shouldNotBe null
            entity.updatedAt shouldNotBe null
        }
    }
})
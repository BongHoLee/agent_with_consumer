# Kotest 테스트 프레임워크 전환 보고서

## 1. 개요

본 문서는 `cc-consumer` 프로젝트의 기존 JUnit 기반 테스트 코드를 Kotest 프레임워크로 전환하는 리팩토링 과정과 결과를 상세히 기술합니다.

**목표:**
- 테스트 코드의 가독성 및 표현력 향상
- Kotlin 언어 특성을 활용한 BDD(Behavior-Driven Development) 스타일 테스트 작성
- 테스트 코드의 유지보수성 증대

---

## 2. `build.gradle.kts` 의존성 변경

Kotest를 프로젝트에 도입하기 위해 `build.gradle.kts` 파일의 의존성을 수정하고 테스트 실행을 위한 설정을 추가했습니다.

### 2.1. 변경 전 (JUnit)

'''kotlin
dependencies {
    // ... other dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
'''

### 2.2. 변경 후 (Kotest)

'''kotlin
val kotestVersion = "5.9.1"

dependencies {
    // ... other dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${kotestVersion}")
    testImplementation("io.kotest:kotest-property-jvm:${kotestVersion}")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
'''

### 2.3. 주요 변경 사항

- **Kotest 의존성 추가**:
    - `kotest-runner-junit5-jvm`: Kotest를 JUnit 5 플랫폼에서 실행하기 위한 러너.
    - `kotest-assertions-core-jvm`: `shouldBe`, `shouldNotBe` 등 직관적인 Assertion을 제공.
    - `kotest-extensions-spring`: `@SpringBootTest`, `@DataJpaTest` 등 Spring 테스트 컨텍스트를 지원.
- **기존 의존성 제거**:
    - `org.testcontainers:junit-jupiter`: Kotest의 Testcontainers 확장으로 대체.
    - `org.jetbrains.kotlin:kotlin-test`: Kotest의 Assertion 라이브러리로 대체.
- **테스트 실행 설정**:
    - `tasks.withType<Test> { useJUnitPlatform() }` 설정을 추가하여 Gradle이 Kotest 테스트를 인식하고 실행할 수 있도록 변경했습니다.

---

## 3. 테스트 코드 리팩토링

프로젝트 내 모든 JUnit 테스트 코드를 Kotest의 `DescribeSpec` 스타일로 전환했습니다. 이를 통해 테스트의 의도와 맥락을 명확하게 표현할 수 있게 되었습니다.

### 3.1. 리팩토링 예시 1: `MydataTerminateUserTest.kt`

**변경 전 (JUnit)**
'''kotlin
class MydataTerminateUserTest {
    @Test
    fun `MydataTerminateUser should be created with required fields`() {
        val payAccountId = 12345L
        val reason = "PFM_SERVICE_CLOSED_BY_USER"
        
        val entity = MydataTerminateUser(
            payAccountId = payAccountId,
            reason = reason
        )
        
        assertEquals(payAccountId, entity.payAccountId)
        assertEquals(reason, entity.reason)
        assertEquals(TerminateStatus.PENDING, entity.terminateStatus)
    }
}
'''

**변경 후 (Kotest)**
'''kotlin
class MydataTerminateUserTest : DescribeSpec({
    describe("MydataTerminateUser creation") {
        val payAccountId = 12345L
        val reason = "PFM_SERVICE_CLOSED_BY_USER"

        it("should be created with required fields and default values") {
            val entity = MydataTerminateUser(
                payAccountId = payAccountId,
                reason = reason
            )

            entity.payAccountId shouldBe payAccountId
            entity.reason shouldBe reason
            entity.terminateStatus shouldBe TerminateStatus.PENDING
        }
    }
})
'''

### 3.2. 리팩토링 예시 2: `MydataTerminateUserRepositoryTest.kt` (Spring 연동)

**변경 전 (JUnit)**
'''kotlin
@DataJpaTest
class MydataTerminateUserRepositoryTest {

    @Autowired
    private lateinit var repository: MydataTerminateUserRepository

    @Test
    fun `should enforce unique constraint on pay_account_id and terminate_status`() {
        val entity1 = MydataTerminateUser(...)
        repository.save(entity1)
        
        val entity2 = MydataTerminateUser(...)
        
        assertThrows<DataIntegrityViolationException> {
            repository.save(entity2)
            repository.flush()
        }
    }
}
'''

**변경 후 (Kotest)**
'''kotlin
@DataJpaTest
class MydataTerminateUserRepositoryTest : DescribeSpec() {

    @Autowired
    private lateinit var repository: MydataTerminateUserRepository

    override fun extensions() = listOf(SpringExtension)

    init {
        describe("MydataTerminateUserRepository") {
            it("should enforce unique constraint on pay_account_id and terminate_status") {
                val entity1 = MydataTerminateUser(...)
                repository.save(entity1)

                val entity2 = MydataTerminateUser(...)

                shouldThrow<DataIntegrityViolationException> {
                    repository.saveAndFlush(entity2)
                }
            }
        }
    }
}
'''

### 3.3. 전체 전환 파일 목록
- `src/test/kotlin/com/consumer/cconsumer/domain/entity/BaseEntityTest.kt`
- `src/test/kotlin/com/consumer/cconsumer/domain/entity/MydataTerminateUserTest.kt`
- `src/test/kotlin/com/consumer/cconsumer/domain/entity/PayTerminateUserTest.kt`
- `src/test/kotlin/com/consumer/cconsumer/domain/entity/TerminateStatusTest.kt`
- `src/test/kotlin/com/consumer/cconsumer/domain/repository/MydataTerminateUserRepositoryTest.kt`
- `src/test/kotlin/com/consumer/cconsumer/domain/repository/PayTerminateUserRepositoryTest.kt`
- `src/test/kotlin/com/consumer/cconsumer/domain/repository/RepositoryIntegrationTest.kt`

---

## 4. 검증

- 모든 테스트 코드를 Kotest로 전환한 후, Gradle의 `test` 태스크를 실행하여 모든 테스트가 성공적으로 통과함을 확인했습니다.
- 기존 JUnit 테스트와 동일한 로직과 검증 포인트를 유지하여 테스트의 신뢰성을 보장했습니다.

---

## 5. 결론

JUnit에서 Kotest로의 전환을 통해 다음과 같은 긍정적인 효과를 얻었습니다.

- **가독성 향상**: `describe-it` 구조와 자연어에 가까운 Matcher(`shouldBe`)를 통해 테스트의 시나리오를 쉽게 이해할 수 있습니다.
- **Kotlin 친화적**: 보일러플레이트 코드를 줄이고, Kotlin의 강력한 언어 기능을 테스트 코드에 적극적으로 활용할 수 있습니다.
- **유지보수성 증대**: 테스트의 구조가 명확해져 새로운 테스트 케이스를 추가하거나 기존 테스트를 수정하기 용이해졌습니다.

이번 리팩토링은 향후 개발 생산성과 코드 품질을 높이는 데 기여할 것입니다.

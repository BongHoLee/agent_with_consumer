dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier = ""
    mainClass.set("com.codex.consumer.CodexConsumerApplication")
}

tasks.named<Jar>("jar") {
    enabled = false
}

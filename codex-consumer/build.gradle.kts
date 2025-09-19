plugins {
    id("com.github.davidmc24.gradle.plugin.avro")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier = ""
    mainClass.set("com.codex.consumer.CodexConsumerApplication")
}

tasks.named<Jar>("jar") {
    enabled = false
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier = ""
    mainClass.set("com.consumer.cconsumer.CcConsumerApplication")
}

tasks.named<Jar>("jar") {
    enabled = false
}
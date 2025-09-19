plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    id("org.springframework.boot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
}

allprojects {
    group = "bong"
    version = "0.0.1-SNAPSHOT"
    
    repositories {
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    
    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
    
    dependencies {
        val implementation by configurations
        val developmentOnly by configurations
        val runtimeOnly by configurations
        val testImplementation by configurations
        val testRuntimeOnly by configurations
        
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.springframework.kafka:spring-kafka")
        implementation("org.apache.avro:avro:1.11.3")
        implementation("io.confluent:kafka-avro-serializer:7.5.0")
        developmentOnly("org.springframework.boot:spring-boot-docker-compose")
        runtimeOnly("com.mysql:mysql-connector-j")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testImplementation("org.springframework.kafka:spring-kafka-test")
        testImplementation("com.h2database:h2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
    
    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }
    
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    
    tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        enabled = false
    }
    
    tasks.named<Jar>("jar") {
        enabled = true
        archiveClassifier.set("")
    }
}

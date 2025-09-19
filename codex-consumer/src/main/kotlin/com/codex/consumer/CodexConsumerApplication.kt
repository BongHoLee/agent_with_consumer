package com.codex.consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CodexConsumerApplication

fun main(args: Array<String>) {
    runApplication<CodexConsumerApplication>(*args)
}

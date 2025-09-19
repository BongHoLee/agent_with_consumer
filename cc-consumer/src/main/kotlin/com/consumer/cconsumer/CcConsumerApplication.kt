package com.consumer.cconsumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CcConsumerApplication

fun main(args: Array<String>) {
    runApplication<CcConsumerApplication>(*args)
}
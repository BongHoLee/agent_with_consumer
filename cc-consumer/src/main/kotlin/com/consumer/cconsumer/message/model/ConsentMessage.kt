package com.consumer.cconsumer.message.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ConsentMessage(
    @JsonProperty("data")
    val data: ConsentData,
    
    @JsonProperty("type")
    val type: String
)
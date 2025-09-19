package com.codex.consumer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConsentMessage(
    @JsonProperty("data")
    val data: ConsentData,

    @JsonProperty("type")
    val type: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConsentData(
    @JsonProperty("delete_event_type")
    val deleteEventType: String,

    @JsonProperty("pay_account_id")
    val payAccountId: Long,

    @JsonProperty("is_remove")
    val isRemove: Boolean,

    @JsonProperty("is_force")
    val isForce: Boolean
)

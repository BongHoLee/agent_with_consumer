package com.consumer.cconsumer.message.model

import com.fasterxml.jackson.annotation.JsonProperty

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
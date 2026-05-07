package com.altibbi.telehealth.model

import com.google.gson.annotations.SerializedName

data class SinaResponse(
    @SerializedName("user_message") val userMessage: SinaMessage?,
    @SerializedName("sina_message") val sinaMessage: SinaMessage?,
)

package com.altibbi.telehealth

import com.google.gson.annotations.SerializedName

class VoipConfig {
    data class Data(
        val id : Int,
        @SerializedName("consultation_id") val consultationId: Int?,
        @SerializedName("api_key") val apiKey: String?,
        @SerializedName("call_id") val callId: String?,
        val token : String,
    )
}
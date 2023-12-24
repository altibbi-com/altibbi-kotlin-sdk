package com.example.altibbi

import com.google.gson.annotations.SerializedName

class VoipConfig {
    data class Data(
        @SerializedName("id") val id: Int?,
        @SerializedName("consultation_id") val consultationId: Int?,
        @SerializedName("api_key") val apiKey: String?,
        @SerializedName("call_id") val callId: String?,
        @SerializedName("token") val token: String?
    )
}
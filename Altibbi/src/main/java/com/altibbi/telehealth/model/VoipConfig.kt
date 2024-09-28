package com.altibbi.telehealth.model

import com.google.gson.annotations.SerializedName

data class VoipConfig(
    val id: Int?,
    @SerializedName("consultation_id") val consultationId: Int?,
    @SerializedName("api_key") val apiKey: String?,
    @SerializedName("call_id") val callId: String?,
    @SerializedName("token") val token: String?,
) {
    companion object {
        fun fromJson(json: Map<String, Any>): VoipConfig {
            return VoipConfig(
                json["id"] as? Int,
                json["consultation_id"] as? Int,
                json["api_key"] as? String,
                json["call_id"] as? String,
                json["token"] as? String
            )
        }
    }
}
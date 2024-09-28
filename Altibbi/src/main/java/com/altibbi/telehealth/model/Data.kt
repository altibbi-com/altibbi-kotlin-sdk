package com.altibbi.telehealth.model

import com.google.gson.annotations.SerializedName

data class Data(
    val id: String?,
    val message: String?,
    @SerializedName("sent_at") val sentAt: String?,
    @SerializedName("chat_user_id") val chatUserId: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Data {
            return Data(
                json["id"] as? String,
                json["message"] as? String,
                json["sent_at"] as? String,
                json["chat_user_id"] as? String
            )
        }
    }
}
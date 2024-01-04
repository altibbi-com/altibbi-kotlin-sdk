package com.altibbi.telehealth

import com.google.gson.annotations.SerializedName
data class ChatConfig(
    val id: Int?,
    @SerializedName("consultation_id") val consultationId: Int?,
    @SerializedName("group_id") val groupId: String?,
    @SerializedName("app_id") val appId: String?,
    @SerializedName("chat_user_id") val chatUserId: String?,
    @SerializedName("chat_user_token") val chatUserToken: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): ChatConfig {
            return ChatConfig(
                id = json["id"] as? Int,
                consultationId = json["consultation_id"] as? Int,
                groupId = json["group_id"] as? String,
                appId = json["app_id"] as? String,
                chatUserId = json["chat_user_id"] as? String,
                chatUserToken = json["chat_user_token"] as? String
            )
        }
    }
}
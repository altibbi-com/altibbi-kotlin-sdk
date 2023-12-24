package com.example.altibbi

import com.google.gson.annotations.SerializedName

class ChatConfig {
    data class Data(
        @SerializedName("id") val id: Int?,
        @SerializedName("consultation_id") val consultationId: Int?,
        @SerializedName("group_id") val groupId: String?,
        @SerializedName("app_id") val appId: String?,
        @SerializedName("chat_user_id") val chatUserId: String?,
        @SerializedName("chat_user_token") val chatUserToken: String?
    )
}
package com.example.altibbi

import com.google.gson.annotations.SerializedName

class ChatHistory {
    data class MessageData(
        val id: String?,
        @SerializedName("message") val message: String?,
        @SerializedName("sent_at") val sentAt: String?,
        @SerializedName("chat_user_id") val chatUserId: String?
    )
    data class Data(
        val id: String?,
        @SerializedName("consultation_id") val consultationId: Int?,
        val data : List<MessageData>,
        @SerializedName("created_at") val createdIt: String?,
        @SerializedName("updated_at") val updateAt: String?,
    )
}
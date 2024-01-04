package com.altibbi.telehealth

import com.google.gson.annotations.SerializedName
data class ChatHistory(
    val id: Int?,
    @SerializedName("consultation_id") val consultationId: Int?,
    val data: List<Data>?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): ChatHistory {
            return ChatHistory(
                json["id"] as? Int,
                json["consultation_id"] as? Int,
                (json["data"] as? List<*>)?.map { it as? Map<String, Any> }
                    ?.map { Data.fromJson(it!!) },
                json["created_at"] as? String,
                json["updated_at"] as? String,
            )
        }
    }
}
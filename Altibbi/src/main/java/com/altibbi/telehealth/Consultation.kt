package com.altibbi.telehealth

import com.google.gson.annotations.SerializedName
data class Consultation(
    val id: Int?,
    @SerializedName("user_id") val userId: Int?,
    val question: String?,
    val medium: String?,
    val status: String?,
    @SerializedName("is_fulfilled") val isFulfilled: Int?,
    @SerializedName("parent_consultation_id") val parentConsultationId: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val parentConsultation: Consultation?,
    val media: List<Media>?,
    val user: User?,
    val consultations: List<Consultation>?,
    @SerializedName("pusherChannel") val socketChannel: String?,
    @SerializedName("pusherAppKey") val appKey: String?,
    val chatConfig: ChatConfig?,
    val voipConfig: VoipConfig?,
    val videoConfig: VoipConfig?,
    val chatHistory: ChatHistory?,
    val recommendation: Recommendation?,
    @SerializedName("doctor_name") val doctorName: String?,
    @SerializedName("doctor_avatar") val doctorAvatar: String?,
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Consultation {
            return Consultation(
                id = json["id"] as? Int,
                userId = json["user_id"] as? Int,
                question = json["question"] as? String,
                medium = json["medium"] as? String,
                status = json["status"] as? String,
                isFulfilled = json["is_fulfilled"] as? Int,
                parentConsultationId = json["parent_consultation_id"] as? Int,
                createdAt = json["created_at"] as? String,
                updatedAt = json["updated_at"] as? String,
                parentConsultation = json["parentConsultation"]?.let { Consultation.fromJson(it as Map<String, Any>) },
                media = (json["media"] as? List<*>)?.map { it as? Map<String, Any> }
                    ?.map { Media.fromJson(it!!) },
                user = json["user"]?.let { User.fromJson(it as Map<String, Any>) },
                consultations = (json["consultations"] as? List<*>)?.map { it as? Map<String, Any> }
                    ?.map { Consultation.fromJson(it!!) },
                socketChannel = json["pusherChannel"] as? String,
                appKey = json["pusherAppKey"] as? String,
                chatConfig = json["chatConfig"]?.let { ChatConfig.fromJson(it as Map<String, Any>) },
                voipConfig = json["voipConfig"]?.let { VoipConfig.fromJson(it as Map<String, Any>) },
                videoConfig = json["videoConfig"]?.let { VoipConfig.fromJson(it as Map<String, Any>) },
                chatHistory = json["chatHistory"]?.let { ChatHistory.fromJson(it as Map<String, Any>) },
                recommendation = json["recommendation"]?.let { Recommendation.fromJson(it as Map<String, Any>) },
                doctorName = json["doctor_name"] as? String,
                doctorAvatar = json["doctor_avatar"] as? String,
            )
        }
    }
}
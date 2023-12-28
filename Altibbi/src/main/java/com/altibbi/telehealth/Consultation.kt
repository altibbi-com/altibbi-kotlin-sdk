package com.altibbi.telehealth

import com.google.gson.annotations.SerializedName

class Consultation {

    data class ConsultationResponse(
        val id: Int,
        @SerializedName("user_id") val userId: Int?,
        @SerializedName("doctor_name") val doctorName: String?,
        val question: String,
        val medium: String,
        val status: String,
        @SerializedName("is_fulfilled") val isFulfilled: Int?,
        @SerializedName("parent_consultation_id") val parentConsultationId: Int?,
        @SerializedName("accepted_at") val acceptedAt: String?,
        @SerializedName("closed_at") val closedAt: String?,
        @SerializedName("created_at") val createdAt: String?,
        @SerializedName("updated_at") val updatedAt: String?,
        @SerializedName("doctor_avatar") val doctorAvatar: String?,
        val user: User.UserResponse,
        val parentConsultation: ConsultationResponse?,
        val consultations: List<ConsultationResponse>,
        val media: List<Media.Data>,
        val pusherChannel: String,
        val pusherAppKey: String,
        val chatConfig: ChatConfig.Data,
        val chatHistory: ChatHistory.Data,
        val voipConfig: VoipConfig.Data,
        val videoConfig: VoipConfig.Data
    )


    interface GetLastConsultationCallback {
        fun onSuccess(response: Any)
        fun onError(error: Any)
    }

    interface CreateConsultationCallback {
        fun onSuccess(response: ConsultationResponse)
        fun onError(error: Any)
    }

    data class ConsultationData(
        val question: String,
        val medium: String,
        val userID: Int,
        val mediaIDs: Array<String>? = null,
        val followUpId: Int? = null
    )

    interface DownloadPrescriptionCallback {
        fun onSuccess(file: String)
        fun onError(errorMessage: String)
    }

    data class CancelConsultationResponse(
        @SerializedName("consultation_id") val consultationId: Int?,
        val status: String,
    )

    interface CancelConsultationCallBack {
        fun onSuccess(response: CancelConsultationResponse)
        fun onError(error: Any)
        fun onErrorObj(error: ConsultationNotFound)
    }

    data class ConsultationNotFound(
        val name: String,
        val message: String,
        val code: Int,
        val status: Int,
        val type: String
    )
    data class GetConsultationByIdResponse(
        val id: Int,
        @SerializedName("user_id") val userId: Int?,
        @SerializedName("doctor_name") val doctorName: String?,
        val question: String,
        val medium: String,
        val status: String,
        @SerializedName("is_fulfilled") val isFulfilled: Int?,
        @SerializedName("parent_consultation_id") val parentConsultationId: Int?,
        @SerializedName("accepted_at") val acceptedAt: String?,
        @SerializedName("closed_at") val closedAt: String?,
        @SerializedName("created_at") val createdAt: String?,
        @SerializedName("updated_at") val updatedAt: String?,
        @SerializedName("doctor_avatar") val doctorAvatar: String?,
        val user: User.UserResponse,
        val parentConsultation: Any?,
        val consultations: List<Any>,
        val media: List<Media.Data>,
        val pusherChannel: String,
        val chatConfig: ChatConfig.Data,
        val chatHistory: ChatHistory.Data,
        val voipConfig: VoipConfig.Data?,
        val videoConfig: VoipConfig.Data?,
    )

    interface GetConsultationByIdCallBack {
        fun onSuccess(response: GetConsultationByIdResponse)
        fun onError(error: Any)
        fun onErrorObj(error: ConsultationNotFound)
    }

    interface GetConsultationListCallBack {
        fun onSuccess(response: List<ConsultationResponse>)
        fun onError(error: Any)
    }

    interface DeleteConsultationCallBack{
        fun onSuccess(response: Any)
        fun onError(response: Any)
    }

    data class UploadMediaResponse(
        val id: String,
        val type: String,
        val name: String,
        val path: String,
        val extension: String,
        val size: Int,
        val url: String
    )
    interface UploadCallback {
        fun onSuccess(response: UploadMediaResponse)
        fun onError(error: Any)
    }
}
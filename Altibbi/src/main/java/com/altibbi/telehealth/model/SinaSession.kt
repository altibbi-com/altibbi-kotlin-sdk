package com.altibbi.telehealth.model

import com.google.gson.annotations.SerializedName

data class SinaSession(
    val id: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("video_config") val videoConfig: VoipConfig?,
    @SerializedName("voip_config") val voipConfig: VoipConfig?,
)

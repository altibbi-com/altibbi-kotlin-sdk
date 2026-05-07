package com.altibbi.telehealth.model

import com.google.gson.annotations.SerializedName

data class SinaMessageExtra(
    @SerializedName("general_answer") val generalAnswer: String?,
)

data class SinaLink(
    val url: String?,
    val brief: String?,
)

data class SinaMessageData(
    @SerializedName("content_type") val contentType: String?,
    @SerializedName("found_in_rag") val foundInRag: Boolean?,
    val links: List<SinaLink>?,
    val extra: SinaMessageExtra?,
)

data class SinaMessagesPage(
    val data: List<SinaMessage>?,
)

data class SinaMessage(
    val id: Long?,
    val sender: String?,
    val text: String?,
    @SerializedName("chat_id") val chatId: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val media: Media?,
    val data: SinaMessageData?,
)

package com.altibbi.telehealth
data class Media(
    val id: String?,
    val type: String?,
    val name: String?,
    val path: String?,
    val extension: String?,
    val size: Int?,
    val url: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Media {
            return Media(
                json["id"] as? String,
                json["type"] as? String,
                json["name"] as? String,
                json["path"] as? String,
                json["extension"] as? String,
                json["size"] as? Int,
                json["url"] as? String
            )
        }
    }
}
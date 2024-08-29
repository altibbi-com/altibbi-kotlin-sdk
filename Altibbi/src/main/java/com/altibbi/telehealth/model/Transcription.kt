package com.altibbi.telehealth.model

data class Transcription(
    val transcript: String
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Transcription {
            return Transcription(
                transcript = json["transcript"] as String
            )
        }
    }
}


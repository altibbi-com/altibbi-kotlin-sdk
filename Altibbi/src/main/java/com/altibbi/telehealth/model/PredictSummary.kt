package com.altibbi.telehealth.model

data class PredictSummary(
    val summary: String
) {
    companion object {
        fun fromJson(json: Map<String, Any>): PredictSummary {
            return PredictSummary(
                summary = json["summary"] as String
            )
        }
    }
}

package com.altibbi.telehealth.model

data class PredictSpecialty(
    val specialtyId: Int
) {
    companion object {
        fun fromJson(json: Map<String, Any>): PredictSpecialty {
            return PredictSpecialty(
                specialtyId = json["specialty_id"] as Int
            )
        }
    }
}
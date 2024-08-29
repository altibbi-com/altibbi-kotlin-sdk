package com.altibbi.telehealth.model

import com.google.gson.annotations.SerializedName
data class Recommendation(
    val id: Int?,
    @SerializedName("consultation_id") val consultationId: Int?,
    val data: RecommendationData?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Recommendation {
            return Recommendation(
                json["id"] as? Int,
                json["consultation_id"] as? Int,
                json["data"]?.let { RecommendationData.fromJson(it as Map<String, Any>) },
                json["created_at"] as? String,
                json["updated_at"] as? String
            )
        }
    }
}
data class RecommendationData(
    val lab: RecommendationLab?,
    val drug: RecommendationDrug?,
    val icd10: RecommendationICD10?,
    val followUp: List<RecommendationFollowUp>?,
    val doctorReferral: RecommendationDoctorReferral?,
    val postCallAnswer: List<RecommendationPostCallAnswer>?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationData {
            return RecommendationData(
                json["lab"]?.let { RecommendationLab.fromJson(it as Map<String, Any>) },
                json["drug"]?.let { RecommendationDrug.fromJson(it as Map<String, Any>) },
                json["icd10"]?.let { RecommendationICD10.fromJson(it as Map<String, Any>) },
                json["followUp"]?.let {
                    (it as List<Map<String, Any>>).map { item ->
                        RecommendationFollowUp.fromJson(item)
                    }
                },
                json["doctorReferral"]?.let { RecommendationDoctorReferral.fromJson(it as Map<String, Any>) },
                json["postCallAnswer"]?.let {
                    (it as List<Map<String, Any>>).map { item ->
                        RecommendationPostCallAnswer.fromJson(item)
                    }
                }
            )
        }
    }
}
data class RecommendationLab(
    val lab: List<RecommendationLabItem>?,
    val panel: List<RecommendationLabItem>?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationLab {
            return RecommendationLab(
                json["lab"]?.let {
                    (it as List<Map<String, Any>>).map { item ->
                        RecommendationLabItem.fromJson(item)
                    }
                },
                json["panel"]?.let {
                    (it as List<Map<String, Any>>).map { item ->
                        RecommendationLabItem.fromJson(item)
                    }
                }
            )
        }
    }
}
data class RecommendationLabItem(
    val name: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationLabItem {
            return RecommendationLabItem(
                json["name"] as? String
            )
        }
    }
}
data class RecommendationDrug(
    val fdaDrug: List<RecommendationFdaDrug>?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationDrug {
            return RecommendationDrug(
                json["fdaDrug"]?.let {
                    (it as List<Map<String, Any>>).map { item ->
                        RecommendationFdaDrug.fromJson(item)
                    }
                }
            )
        }
    }
}
data class RecommendationFdaDrug(
    val name: String?,
    val dosage: String?,
    val duration: Int?,
    val howToUse: String?,
    val frequency: String?,
    val tradeName: String?,
    val dosageForm: String?,
    val dosageUnit: String?,
    val packageSize: String?,
    val packageType: String?,
    val strengthValue: String?,
    val relationWithFood: String?,
    val specialInstructions: String?,
    val routeOfAdministration: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationFdaDrug {
            return RecommendationFdaDrug(
                json["name"] as? String,
                json["dosage"] as? String,
                json["duration"] as? Int,
                json["howToUse"] as? String,
                json["frequency"] as? String,
                json["tradeName"] as? String,
                json["dosageForm"] as? String,
                json["dosageUnit"] as? String,
                json["packageSize"] as? String,
                json["packageType"] as? String,
                json["strengthValue"] as? String,
                json["relationWithFood"] as? String,
                json["specialInstructions"] as? String,
                json["routeOfAdministration"] as? String
            )
        }
    }
}
data class RecommendationICD10(
    val symptom: List<RecommendationSymptom>?,
    val diagnosis: List<RecommendationDiagnosis>?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationICD10 {
            return RecommendationICD10(
                json["symptom"]?.let {
                    (it as List<Map<String, Any>>).map { item ->
                        RecommendationSymptom.fromJson(item)
                    }
                },
                json["diagnosis"]?.let {
                    (it as List<Map<String, Any>>).map { item ->
                        RecommendationDiagnosis.fromJson(item)
                    }
                }
            )
        }
    }
}
data class RecommendationSymptom(
    val code: String?,
    val name: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationSymptom {
            return RecommendationSymptom(
                json["code"] as? String,
                json["name"] as? String
            )
        }
    }
}
data class RecommendationDiagnosis(
    val code: String?,
    val name: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationDiagnosis {
            return RecommendationDiagnosis(
                json["code"] as? String,
                json["name"] as? String
            )
        }
    }
}
data class RecommendationFollowUp(
    val name: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationFollowUp {
            return RecommendationFollowUp(
                json["name"] as? String
            )
        }
    }
}
data class RecommendationDoctorReferral(
    val name: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationDoctorReferral {
            return RecommendationDoctorReferral(
                json["name"] as? String
            )
        }
    }
}
data class RecommendationPostCallAnswer(
    val answer: String?,
    val question: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): RecommendationPostCallAnswer {
            return RecommendationPostCallAnswer(
                json["answer"] as? String,
                json["question"] as? String
            )
        }
    }
}
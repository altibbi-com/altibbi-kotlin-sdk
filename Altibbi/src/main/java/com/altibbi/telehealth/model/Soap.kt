package com.altibbi.telehealth.model


data class Soap(
    val summary: Summary
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Soap {
            return Soap(
                summary = Summary.fromJson(json["summary"] as Map<String, Any>)
            )
        }
    }

    fun toJson(): Map<String, Any> {
        return mapOf(
            "summary" to summary.toJson()
        )
    }
}

data class Summary(
    val subjective: Subjective,
    val objective: Objective,
    val assessment: Assessment,
    val plan: Plan
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Summary {
            return Summary(
                subjective = Subjective.fromJson(json["subjective"] as Map<String, Any>),
                objective = Objective.fromJson(json["objective"] as Map<String, Any>),
                assessment = Assessment.fromJson(json["assessment"] as Map<String, Any>),
                plan = Plan.fromJson(json["plan"] as Map<String, Any>)
            )
        }
    }

    fun toJson(): Map<String, Any> {
        return mapOf(
            "subjective" to subjective.toJson(),
            "objective" to objective.toJson(),
            "assessment" to assessment.toJson(),
            "plan" to plan.toJson()
        )
    }
}

data class Subjective(
    val symptoms: String?,
    val concerns: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Subjective {
            return Subjective(
                symptoms = json["symptoms"] as String?,
                concerns = json["concerns"] as String?
            )
        }
    }

    fun toJson(): Map<String, String?> {
        return mapOf(
            "symptoms" to symptoms,
            "concerns" to concerns
        )
    }
}

data class Objective(
    val laboratoryResults: String?,
    val physicalExaminationFindings: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Objective {
            return Objective(
                laboratoryResults = json["laboratory_results"] as String?,
                physicalExaminationFindings = json["physical_examination_findings"] as String?
            )
        }
    }

    fun toJson(): Map<String, String?> {
        return mapOf(
            "laboratory_results" to laboratoryResults,
            "physical_examination_findings" to physicalExaminationFindings
        )
    }
}

data class Assessment(
    val diagnosis: String?,
    val differentialDiagnosis: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Assessment {
            return Assessment(
                diagnosis = json["diagnosis"] as String?,
                differentialDiagnosis = json["differential_diagnosis"] as String?
            )
        }
    }

    fun toJson(): Map<String, String?> {
        return mapOf(
            "diagnosis" to diagnosis,
            "differential_diagnosis" to differentialDiagnosis
        )
    }
}

data class Plan(
    val nonPharmacologicalIntervention: String?,
    val medications: String?,
    val referrals: String?,
    val followUpInstructions: String?
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Plan {
            return Plan(
                nonPharmacologicalIntervention = json["non_pharmacological_intervention"] as String?,
                medications = json["medications"] as String?,
                referrals = json["referrals"] as String?,
                followUpInstructions = json["follow_up_instructions"] as String?
            )
        }
    }

    fun toJson(): Map<String, String?> {
        return mapOf(
            "non_pharmacological_intervention" to nonPharmacologicalIntervention,
            "medications" to medications,
            "referrals" to referrals,
            "follow_up_instructions" to followUpInstructions
        )
    }
}
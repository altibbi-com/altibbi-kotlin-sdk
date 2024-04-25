package com.altibbi.telehealth


import com.altibbi.telehealth.types.BloodType
import com.altibbi.telehealth.types.Gender
import com.altibbi.telehealth.types.MaritalStatus
import com.altibbi.telehealth.types.RelationType
import com.google.gson.annotations.SerializedName
data class User(
    var id: String? = null,
    var name: String? = null,
    @SerializedName("phone_number") var phoneNumber: String? = null,
    var email: String? = null,
    @SerializedName("date_of_birth") var dateOfBirth: String? = null,
    var gender: Gender? = null,
    @SerializedName("insurance_id") var insuranceId: String? = null,
    @SerializedName("policy_number") var policyNumber: String? = null,
    @SerializedName("nationality_number") var nationalityNumber: String? = null,
    var height: Int? = null,
    var weight: Int? = null,
    @SerializedName("blood_type") var bloodType: BloodType? = null,
    var smoker: String? = null,
    var alcoholic: String? = null,
    @SerializedName("marital_status") var maritalStatus: MaritalStatus? = null,
    @SerializedName("relation_type") var relationType: RelationType? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null
) {
    constructor(json: Map<String, Any>) : this(
        id = json["id"] as? String?,
        name = json["name"] as? String,
        phoneNumber = json["phone_number"] as? String,
        email = json["email"] as? String,
        dateOfBirth = json["date_of_birth"] as? String,
        gender = json["gender"] as? Gender,
        insuranceId = json["insurance_id"] as? String,
        policyNumber = json["policy_number"] as? String,
        nationalityNumber = json["nationality_number"] as? String,
        height = json["height"] as? Int,
        weight = json["weight"] as? Int,
        bloodType = json["blood_type"] as? BloodType,
        smoker = json["smoker"] as? String,
        alcoholic = json["alcoholic"] as? String,
        maritalStatus = json["marital_status"] as? MaritalStatus,
        relationType = json["relation_type"] as? RelationType,
        createdAt = json["created_at"] as? String,
        updatedAt = json["updated_at"] as? String
    )
    companion object {
        fun fromJson(json: Map<String, Any>): User {
            return User(
                json["id"] as? String,
                json["name"] as? String,
                json["phone_number"] as? String,
                json["email"] as? String,
                json["date_of_birth"] as? String,
                json["gender"] as? Gender,
                json["insurance_id"] as? String,
                json["policy_number"] as? String,
                json["nationality_number"] as? String,
                json["height"] as? Int,
                json["weight"] as? Int,
                json["blood_type"] as? BloodType,
                json["smoker"] as? String,
                json["alcoholic"] as? String,
                json["marital_status"] as? MaritalStatus,
                json["relation_type"] as? RelationType,
                json["created_at"] as? String,
                json["updated_at"] as? String
            )
        }
    }
    fun toJson(): Map<String, Any?> {
        val jsonMap = mapOf(
            "name" to name,
            "email" to email,
            "date_of_birth" to dateOfBirth,
            "gender" to gender?.type,
            "insurance_id" to insuranceId,
            "policy_number" to policyNumber,
            "nationality_number" to nationalityNumber,
            "height" to height,
            "weight" to weight,
            "blood_type" to bloodType?.type,
            "smoker" to smoker,
            "alcoholic" to alcoholic,
            "marital_status" to maritalStatus?.type,
            "relation_type" to relationType?.type
        )
        println("filtered data is -> ${
            jsonMap.filterValues { it != null }
        }")
        return jsonMap.filterValues { it != null }
    }
}

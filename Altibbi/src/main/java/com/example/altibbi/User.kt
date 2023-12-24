package com.example.altibbi

import com.google.gson.annotations.SerializedName

class User {
    data class UserResponse(
        val id: Int,
        val name: String,
        @SerializedName("nationality_number") val nationalityNumber: String?,
        val email: String?,
        @SerializedName("date_of_birth") val dateOfBirth: String?,
        val gender: String,
        @SerializedName("insurance_id") val insuranceId: String?,
        @SerializedName("policy_number") val policyNumber: String?,
        val height: String?,
        val weight: String?,
        @SerializedName("blood_type") val bloodType: String?,
        val smoker: String?,
        val alcoholic: String?,
        @SerializedName("marital_status") val maritalStatus: String?,
        @SerializedName("created_at") val createdAt: String?,
        @SerializedName("updated_at") val updatedAt: String?
    )

    interface GetUserCallback {
        fun onSuccess(response: UserResponse)
        fun onError(error: Any)
    }

    interface DeleteUserCallBack{
        fun onSuccess(response: Any?)
        fun onError(response: Any)
    }

    interface GetUsersCallback {
        fun onSuccess(response: List<UserResponse>)
        fun onError(error: Any)
    }

    interface CreateUserCallBack{
        fun onSuccess(response: UserResponse)
        fun onError(response: Any)
    }
    data class CreateUserData(
        val name: String?,
        val email: String?,
        @SerializedName("date_of_birth") val dateOfBirth: String?,
        val gender: String?,
        @SerializedName("insurance_id") val insuranceId: String?,
        @SerializedName("policy_number") val policyNumber: String?,
        @SerializedName("nationality_number") val nationalityNumber: String?,
        val height: Int?,
        val weight: Int?,
        @SerializedName("blood_type") val bloodType: String?,
        val smoker: String?,
        val alcoholic: String?,
        @SerializedName("marital_status") val maritalStatus: String?,
    ) {
        fun extractNonNullValues(): Map<String, Any?> = mapOf(
            "name" to name,
            "email" to email,
            "date_of_birth" to dateOfBirth,
            "insurance_id" to insuranceId,
            "policy_number" to policyNumber,
            "height" to height,
            "weight" to weight,
            "gender" to gender,
            "smoker" to smoker,
            "blood_type" to bloodType,
            "nationalityNumber" to nationalityNumber,
            "alcoholic" to alcoholic
        )
    }

    interface UpdateUserCallback {
        fun onSuccess(response: UserResponse)
        fun onError(error: Any)
    }
    data class UpdateUserData(
        val id: String?,
        val name: String?,
        val email: String?,
        @SerializedName("date_of_birth") val dateOfBirth: String?,
        val gender: String?,
        @SerializedName("insurance_id") val insuranceId: String?,
        @SerializedName("policy_number") val policyNumber: String?,
        @SerializedName("nationality_number") val nationalityNumber: String?,
        val height: Int?,
        val weight: Int?,
        @SerializedName("blood_type") val bloodType: String?,
        val smoker: String?,
        val alcoholic: String?,
        @SerializedName("marital_status") val maritalStatus: String?,
    ) {
        fun extractNonNullValues(): Map<String, Any?> = mapOf(
            "name" to name,
            "email" to email,
            "date_of_birth" to dateOfBirth,
            "insurance_id" to insuranceId,
            "policy_number" to policyNumber,
            "height" to height,
            "weight" to weight,
            "gender" to gender,
            "smoker" to smoker,
            "blood_type" to bloodType,
            "nationalityNumber" to nationalityNumber,
            "alcoholic" to alcoholic
        )
    }

}
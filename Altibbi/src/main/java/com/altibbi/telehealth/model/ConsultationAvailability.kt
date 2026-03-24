package com.altibbi.telehealth.model

import com.google.gson.annotations.SerializedName

data class ConsultationAvailableShift(
    val day: String?,
    val from: Int?,
    val to: Int?,
    val booked: Boolean?,
    @SerializedName("full_date") val fullDate: String?,
) {
    fun shiftValue(): String? = fullDate?.takeIf { it.isNotEmpty() }

    fun displayText(): String {
        val dayPrefix = if (!day.isNullOrEmpty()) "$day " else ""
        return if (from != null && to != null) "${dayPrefix}$from -> $to" else dayPrefix.trim()
    }
}

data class ConsultationAvailableShifts(val shifts: List<ConsultationAvailableShift>)

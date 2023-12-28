package com.altibbi.telehealth

class Media {
    data class Data(
        val id: String?,
        val type: String?,
        val name: String?,
        val path: String?,
        val extension: String?,
        val url: String?,
        val size: Int?
    )
}
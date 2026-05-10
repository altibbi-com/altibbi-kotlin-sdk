package com.altibbi.telehealth

import android.util.Log

class AltibbiService {
    companion object {
        var authToken: String? = null
        var url: String? = null
        var lang: String = "en"
        var enableDebug: Boolean = false
        var sinaEndpoint: String? = null

        fun init(
            token: String,
            baseUrl: String,
            language: String = "en",
            sinaModelEndPoint: String = "",
        ) {
            authToken = token
            url = baseUrl
            lang = language
            sinaEndpoint = sinaModelEndPoint.ifBlank { null }
        }

        internal fun log(tag: String, message: String) {
            if (enableDebug) Log.d(tag, message)
        }

        internal fun logError(tag: String, message: String, e: Throwable? = null) {
            if (enableDebug) Log.e(tag, message, e)
        }
    }
}

package com.altibbi.telehealth
class AltibbiService {
    companion object {
        var authToken: String? = null // Authentication token for the service
        var url: String? = null // Base URL for the service
        var lang: String = "en" // lang preference (ar or en)
        var enableDebug: Boolean = false // controls SDK-level debug logging
        /**
         * Initializes the Altibbi service with the specified parameters.
         *
         * @param token The authentication token for the service.
         * @param baseUrl The base URL for the service.
         * @param language The language preference for the service (default is 'en').
         */
        fun init(token: String, baseUrl: String, language: String = "en") {
            authToken = token
            url = baseUrl
            lang = language
        }
    }
}

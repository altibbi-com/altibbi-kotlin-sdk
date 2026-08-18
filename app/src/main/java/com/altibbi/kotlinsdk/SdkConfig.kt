package com.altibbi.kotlinsdk

import android.content.Context
import com.altibbi.telehealth.AltibbiService

object SdkConfig {

    private const val PREFS = "altibbi_sdk_config"
    private const val KEY_TOKEN = "token"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_SINA_ENDPOINT = "sina_endpoint"

    data class Values(
        val token: String,
        val baseUrl: String,
        val language: String,
        val sinaEndpoint: String
    ) {
        val isComplete: Boolean get() = token.isNotBlank() && baseUrl.isNotBlank()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): Values = prefs(context).let {
        Values(
            token = it.getString(KEY_TOKEN, "").orEmpty(),
            baseUrl = it.getString(KEY_BASE_URL, "").orEmpty(),
            language = it.getString(KEY_LANGUAGE, "en").orEmpty().ifBlank { "en" },
            sinaEndpoint = it.getString(KEY_SINA_ENDPOINT, "").orEmpty()
        )
    }

    fun save(context: Context, values: Values) {
        prefs(context).edit()
            .putString(KEY_TOKEN, values.token)
            .putString(KEY_BASE_URL, values.baseUrl)
            .putString(KEY_LANGUAGE, values.language)
            .putString(KEY_SINA_ENDPOINT, values.sinaEndpoint)
            .apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun apply(values: Values) {
        AltibbiService.enableDebug = true
        AltibbiService.init(
            token = values.token,
            baseUrl = values.baseUrl,
            language = values.language,
            sinaModelEndPoint = values.sinaEndpoint,
        )
    }
}

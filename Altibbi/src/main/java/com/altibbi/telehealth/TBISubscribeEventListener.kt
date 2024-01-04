package com.altibbi.telehealth

import org.json.JSONObject

interface TBISubscribeEventListener {
    fun onEvent(event: JSONObject)
    fun onAuthenticationFailure(message: String?, e: Exception?)
    fun onSubscriptionSucceeded(channelName: String)
}
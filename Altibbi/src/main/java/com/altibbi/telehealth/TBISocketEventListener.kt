package com.altibbi.telehealth

interface TBISocketEventListener {
    fun onConnectionStateChange(previousState:String? , currentState: String?)
    fun onError(message: String, code: String?, e: Exception?)
}
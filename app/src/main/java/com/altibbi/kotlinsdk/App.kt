package com.altibbi.kotlinsdk

import android.app.Application
import com.altibbi.telehealth.AltibbiService

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AltibbiService.enableDebug = true
        AltibbiService.init(
            token = "",
            baseUrl = "",
            language = "en",
            sinaModelEndPoint = "",
        )
    }
}

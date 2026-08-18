package com.altibbi.kotlinsdk

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = SdkConfig.load(this)
        if (config.isComplete) {
            SdkConfig.apply(config)
        }
    }
}

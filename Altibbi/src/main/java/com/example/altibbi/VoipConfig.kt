package com.example.altibbi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.annotations.SerializedName

class VoipConfig : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voip_config)
    }

    data class Data(
        @SerializedName("id") val id: Int?,
        @SerializedName("consultation_id") val consultationId: Int?,
        @SerializedName("api_key") val apiKey: String?,
        @SerializedName("call_id") val callId: String?,
        @SerializedName("token") val token: String?
    )
}
package com.example.altibbi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.annotations.SerializedName

class ChatConfig : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_config)
    }

    data class Data(
        @SerializedName("id") val id: Int?,
        @SerializedName("consultation_id") val consultationId: Int?,
        @SerializedName("group_id") val groupId: String?,
        @SerializedName("app_id") val appId: String?,
        @SerializedName("chat_user_id") val chatUserId: String?,
        @SerializedName("chat_user_token") val chatUserToken: String?
    )
}
package com.example.altibbi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.annotations.SerializedName

class ChatHistory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)
    }

    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("message") val message: String?,
        @SerializedName("sent_at") val sentAt: String?,
        @SerializedName("chat_user_id") val chatUserId: String?
    )


}
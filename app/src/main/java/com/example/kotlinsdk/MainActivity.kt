package com.example.kotlinsdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override

    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        val consultationPageButton = findViewById<Button>(R.id.button);
        consultationPageButton.setOnClickListener {
            val intent = Intent(this, ConsultationPage::class.java)
            startActivity(intent)
        }

        val userPageButton = findViewById<Button>(R.id.button3);
        userPageButton.setOnClickListener {
            val intent = Intent(this, UserPage::class.java)
            startActivity(intent)
        }
    }
}
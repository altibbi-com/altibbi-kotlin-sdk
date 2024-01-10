package com.altibbi.kotlinsdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.altibbi.telehealth.AltibbiService

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

        val initButton = findViewById<Button>(R.id.button15)

        initButton.setOnClickListener{
            val endPoint : EditText = findViewById(R.id.textInputEditText17)
            val auth : EditText = findViewById(R.id.textInputEditText16)
            AltibbiService.init(auth.text.toString(), endPoint.text.toString(), "en")
        }
    }
}
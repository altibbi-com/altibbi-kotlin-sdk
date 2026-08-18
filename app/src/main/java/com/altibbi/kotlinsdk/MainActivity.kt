package com.altibbi.kotlinsdk

import com.altibbi.kotlinsdk.R
import com.altibbi.kotlinsdk.consultation.ConsultationPageActivity
import com.altibbi.kotlinsdk.consultation.ConsultationListActivity
import com.altibbi.kotlinsdk.sina.AskSinaActivity
import com.altibbi.kotlinsdk.user.UserPageActivity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<MaterialButton>(R.id.button_consultation).setOnClickListener {
            startActivity(Intent(this, ConsultationPageActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.button_ask_sina).setOnClickListener {
            startActivity(Intent(this, AskSinaActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.button_consultation_list).setOnClickListener {
            startActivity(Intent(this, ConsultationListActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.button_user_page).setOnClickListener {
            startActivity(Intent(this, UserPageActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.button_sdk_setup).setOnClickListener {
            startActivity(
                Intent(this, SetupActivity::class.java)
                    .putExtra(SetupActivity.EXTRA_EDIT_CONFIG, true)
            )
            finish()
        }
    }
}

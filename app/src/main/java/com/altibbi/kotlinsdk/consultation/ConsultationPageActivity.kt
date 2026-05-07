package com.altibbi.kotlinsdk.consultation

import com.altibbi.kotlinsdk.R
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class ConsultationPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation_page)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = "Consultation"

        findViewById<View>(R.id.btn_new_consultation).setOnClickListener {
            startActivity(Intent(this, NewConsultationActivity::class.java))
        }
        findViewById<View>(R.id.btn_follow_up).setOnClickListener {
            startActivity(Intent(this, FollowUpActivity::class.java))
        }
        findViewById<View>(R.id.btn_ai_support).setOnClickListener {
            startActivity(Intent(this, AiSupportActivity::class.java))
        }
        findViewById<View>(R.id.btn_developer_tools).setOnClickListener {
            startActivity(Intent(this, DeveloperToolsActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}

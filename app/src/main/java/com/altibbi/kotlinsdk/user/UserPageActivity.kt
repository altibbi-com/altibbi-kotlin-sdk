package com.altibbi.kotlinsdk.user

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.altibbi.kotlinsdk.R
import com.google.android.material.appbar.MaterialToolbar

class UserPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = "User Management"

        findViewById<View>(R.id.btn_create_user).setOnClickListener {
            startActivity(Intent(this, CreateUserActivity::class.java))
        }
        findViewById<View>(R.id.btn_update_user).setOnClickListener {
            startActivity(Intent(this, UpdateUserActivity::class.java))
        }
        findViewById<View>(R.id.btn_user_tools).setOnClickListener {
            startActivity(Intent(this, UserToolsActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}

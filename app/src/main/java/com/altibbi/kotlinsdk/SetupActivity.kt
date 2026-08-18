package com.altibbi.kotlinsdk

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SetupActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EDIT_CONFIG = "edit_config"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val saved = SdkConfig.load(this)
        if (saved.isComplete && !intent.getBooleanExtra(EXTRA_EDIT_CONFIG, false)) {
            SdkConfig.apply(saved)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_setup)

        val toolbar = findViewById<MaterialToolbar>(R.id.setup_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<TextInputEditText>(R.id.et_setup_token).setText(saved.token)
        findViewById<TextInputEditText>(R.id.et_setup_base_url).setText(saved.baseUrl)
        findViewById<TextInputEditText>(R.id.et_setup_language).setText(saved.language)
        findViewById<TextInputEditText>(R.id.et_setup_sina_endpoint).setText(saved.sinaEndpoint)

        findViewById<MaterialButton>(R.id.btn_setup_continue).setOnClickListener { submit() }
        findViewById<MaterialButton>(R.id.btn_setup_clear).setOnClickListener { clear() }
    }

    private fun submit() {
        val values = SdkConfig.Values(
            token = text(R.id.et_setup_token),
            baseUrl = text(R.id.et_setup_base_url).trimEnd('/'),
            language = text(R.id.et_setup_language).ifBlank { "en" },
            sinaEndpoint = text(R.id.et_setup_sina_endpoint)
        )

        if (!values.isComplete) {
            showStatus("Token and Base URL are required.", true)
            return
        }

        SdkConfig.save(this, values)
        SdkConfig.apply(values)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun clear() {
        SdkConfig.clear(this)
        findViewById<TextInputEditText>(R.id.et_setup_token).setText("")
        findViewById<TextInputEditText>(R.id.et_setup_base_url).setText("")
        findViewById<TextInputEditText>(R.id.et_setup_language).setText("en")
        findViewById<TextInputEditText>(R.id.et_setup_sina_endpoint).setText("")
        showStatus("Saved config cleared.", false)
    }

    private fun text(viewId: Int): String =
        findViewById<TextInputEditText>(viewId).text?.toString()?.trim().orEmpty()

    private fun showStatus(message: String, isError: Boolean) {
        val tv = findViewById<TextView>(R.id.tv_setup_status)
        tv.visibility = View.VISIBLE
        tv.text = message
        tv.background = ContextCompat.getDrawable(
            this,
            if (isError) R.drawable.bg_feedback_error else R.drawable.bg_feedback_success
        )
        tv.setTextColor(ContextCompat.getColor(this, if (isError) R.color.error else R.color.success))
    }
}

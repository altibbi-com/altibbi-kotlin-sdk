package com.altibbi.kotlinsdk.sina

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.altibbi.kotlinsdk.R
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.SinaSession
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class AskSinaActivity : AppCompatActivity() {

    private lateinit var feedback: TextView
    private lateinit var button: MaterialButton
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_sina)

        val toolbar = findViewById<MaterialToolbar>(R.id.ask_sina_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        feedback = findViewById(R.id.ask_sina_feedback)
        button = findViewById(R.id.button_start_session)

        button.setOnClickListener {
            if (!isLoading) handleStartSession()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun handleStartSession() {
        setLoading(true)
        feedback.visibility = View.GONE

        ApiService.createSinaSession(object : ApiCallback<SinaSession> {
            override fun onSuccess(response: SinaSession) {
                val sessionId = response.id
                runOnUiThread {
                    setLoading(false)
                    if (sessionId != null) {
                        val intent = Intent(this@AskSinaActivity, SinaChatActivity::class.java)
                            .putExtra(SinaChatActivity.EXTRA_SESSION_ID, sessionId)
                        val videoConfig = response.videoConfig
                        val voipConfig = response.voipConfig
                        if (videoConfig != null) {
                            intent.putExtra(SinaChatActivity.EXTRA_API_KEY, videoConfig.apiKey)
                            intent.putExtra(SinaChatActivity.EXTRA_CALL_ID, videoConfig.callId)
                            intent.putExtra(SinaChatActivity.EXTRA_TOKEN, videoConfig.token)
                            intent.putExtra(SinaChatActivity.EXTRA_VOIP, false)
                        } else if (voipConfig != null) {
                            intent.putExtra(SinaChatActivity.EXTRA_API_KEY, voipConfig.apiKey)
                            intent.putExtra(SinaChatActivity.EXTRA_CALL_ID, voipConfig.callId)
                            intent.putExtra(SinaChatActivity.EXTRA_TOKEN, voipConfig.token)
                            intent.putExtra(SinaChatActivity.EXTRA_VOIP, true)
                        }
                        startActivity(intent)
                    } else {
                        showFeedback(getString(R.string.ask_sina_session_no_id), isError = true)
                    }
                }
            }

            override fun onFailure(error: String?) {
                runOnUiThread {
                    setLoading(false)
                    showFeedback(getString(R.string.ask_sina_session_failed), isError = true)
                }
            }

            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        button.isEnabled = !loading
        button.text = if (loading) getString(R.string.ask_sina_starting) else getString(R.string.ask_sina_start_session)
    }

    private fun showFeedback(message: String, isError: Boolean) {
        feedback.visibility = View.VISIBLE
        feedback.text = message
        if (isError) {
            feedback.background = ContextCompat.getDrawable(this, R.drawable.bg_sina_feedback_error)
            feedback.setTextColor(ContextCompat.getColor(this, R.color.error))
        } else {
            feedback.background = ContextCompat.getDrawable(this, R.drawable.bg_sina_feedback_success)
            feedback.setTextColor(ContextCompat.getColor(this, R.color.success))
        }
    }
}

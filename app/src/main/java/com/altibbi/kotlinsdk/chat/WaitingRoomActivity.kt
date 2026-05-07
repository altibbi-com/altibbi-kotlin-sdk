package com.altibbi.kotlinsdk.chat

import com.altibbi.kotlinsdk.video.VideoActivity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import coil.load
import coil.transform.CircleCropTransformation
import com.airbnb.lottie.LottieAnimationView
import com.altibbi.kotlinsdk.R
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Consultation
import com.altibbi.telehealth.TBISocket
import com.altibbi.telehealth.TBISocketEventListener
import com.altibbi.telehealth.TBISubscribeEventListener
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.util.Locale

class WaitingRoomActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "WaitingRoomActivity"
    }

    val socket = TBISocket()

    var currentConsultation: Consultation? = null

    private val handler = Handler(Looper.getMainLooper())
    private var startTimeMs: Long = 0L
    private lateinit var tvElapsed: TextView
    private lateinit var tvTip: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTitle: TextView
    private lateinit var ivDoctorAvatar: ImageView
    private lateinit var lottieDoctor: LottieAnimationView
    private val tips by lazy {
        listOf(
            R.string.waiting_tip_1,
            R.string.waiting_tip_2,
            R.string.waiting_tip_3,
            R.string.waiting_tip_4,
            R.string.waiting_tip_5
        )
    }
    private var tipIndex = 0

    private val tickRunnable = object : Runnable {
        override fun run() {
            val elapsed = SystemClock.elapsedRealtime() - startTimeMs
            val totalSec = elapsed / 1000
            val mm = totalSec / 60
            val ss = totalSec % 60
            tvElapsed.text = String.format(Locale.getDefault(), "%02d:%02d", mm, ss)
            handler.postDelayed(this, 1000)
        }
    }

    private val tipRunnable = object : Runnable {
        override fun run() {
            tipIndex = (tipIndex + 1) % tips.size
            tvTip.animate().alpha(0f).setDuration(250).withEndAction {
                tvTip.setText(tips[tipIndex])
                tvTip.animate().alpha(1f).setDuration(250).start()
            }.start()
            handler.postDelayed(this, 6000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)

        tvElapsed = findViewById(R.id.tv_elapsed)
        tvTip = findViewById(R.id.tv_tip)
        tvStatus = findViewById(R.id.tv_status)
        tvTitle = findViewById(R.id.tv_title)
        ivDoctorAvatar = findViewById(R.id.iv_doctor_avatar)
        lottieDoctor = findViewById(R.id.lottie_doctor)

        findViewById<android.view.View>(R.id.pulse_outer)
            .startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse))
        findViewById<android.view.View>(R.id.pulse_inner)
            .startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse_delayed))

        findViewById<MaterialButton>(R.id.btn_cancel).setOnClickListener {
            confirmCancel()
        }

        startTimeMs = SystemClock.elapsedRealtime()
        handler.post(tickRunnable)
        handler.postDelayed(tipRunnable, 6000)

        getConsultation(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun confirmCancel() {
        AlertDialog.Builder(this)
            .setTitle(R.string.waiting_cancel_confirm_title)
            .setMessage(R.string.waiting_cancel_confirm_msg)
            .setPositiveButton(R.string.waiting_cancel_confirm_yes) { d, _ ->
                d.dismiss()
                cancelConsultation(currentConsultation?.id.toString())
            }
            .setNegativeButton(R.string.waiting_cancel_confirm_no) { d, _ -> d.dismiss() }
            .show()
    }

    private fun getConsultation(context: Context) {
        ApiService.getLastConsultation(object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                currentConsultation = response
                runOnUiThread { updateDoctorInfo(response) }
                if (response.status == "in_progress") {
                    routeByMedium(response.medium ?: "", response.id?.toString())
                } else {
                    initSocket(response, context)
                }
            }

            override fun onFailure(error: String?) {
                Log.e(TAG, "getConsultation onFailure: $error")
            }

            override fun onRequestError(error: String?) {
                Log.e(TAG, "getConsultation onRequestError: $error")
            }
        })
    }

    private fun updateDoctorInfo(consultation: Consultation) {
        consultation.doctorName?.let { name ->
            tvTitle.text = name
        }
        consultation.doctorAvatar?.let { url ->
            ivDoctorAvatar.visibility = View.VISIBLE
            lottieDoctor.visibility = View.GONE
            ivDoctorAvatar.load(url) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
    }

    private fun routeByMedium(medium: String, consultationId: String?) {
        Log.d(TAG, "routeByMedium — medium=$medium id=$consultationId")
        runOnUiThread {
            when (medium) {
                "video", "voip" -> {
                    Log.d(TAG, "routing to VideoActivity")
                    val intent = Intent(applicationContext, VideoActivity::class.java)
                    consultationId?.let { intent.putExtra("consultationId", it) }
                    startActivity(intent)
                    finish()
                }
                "chat" -> {
                    Log.d(TAG, "routing to ChatActivity")
                    val intent = Intent(applicationContext, ChatActivity::class.java)
                    consultationId?.let { intent.putExtra("consultationId", it) }
                    startActivity(intent)
                    finish()
                }
                else -> Log.e(TAG, "routeByMedium — unknown medium=$medium")
            }
        }
    }

    private fun initSocket(response: Consultation, context: Context) {
        socket.init(
            channelName = response.socketChannel!!,
            appKey = response.appKey!!,
            connectionCallback = object : TBISocketEventListener {
                override fun onConnectionStateChange(previousState: String?, currentState: String?) {
                    if (currentState == "CONNECTED") {
                        socket.subscribe("call-status", object : TBISubscribeEventListener {
                            override fun onEvent(event: JSONObject) {
                                val status = event.getString("status")
                                Log.d(TAG, "socket event status=$status")
                                when (status) {
                                    "checking_medical_profile" -> {
                                        runOnUiThread {
                                            tvStatus.setText(R.string.waiting_status_checking_profile)
                                        }
                                        ApiService.getLastConsultation(object : ApiCallback<Consultation> {
                                            override fun onSuccess(response: Consultation) {
                                                currentConsultation = response
                                                runOnUiThread { updateDoctorInfo(response) }
                                            }
                                            override fun onFailure(error: String?) { Log.e(TAG, "checking_medical_profile getLastConsultation onFailure: $error") }
                                            override fun onRequestError(error: String?) { Log.e(TAG, "checking_medical_profile getLastConsultation onRequestError: $error") }
                                        })
                                    }
                                    "in_progress" -> {
                                        val consultationId = event.optInt("id").takeIf { it != 0 }?.toString()
                                        val medium = event.optString("medium")
                                        Log.d(TAG, "in_progress — id=$consultationId medium=$medium")
                                        routeByMedium(medium, consultationId)
                                    }
                                    "closed" -> {
                                        Log.d(TAG, "closed — finishing")
                                        finish()
                                    }
                                }
                            }
                            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                                Log.e(TAG, "socket authFailure: $message", e)
                            }
                            override fun onSubscriptionSucceeded(channelName: String) {
                                Log.d(TAG, "socket subscribed: $channelName")
                            }
                        })
                    }
                }
                override fun onError(message: String, code: String?, e: Exception?) {}
            },
            subscribeCallback = object : TBISubscribeEventListener {
                override fun onEvent(event: JSONObject) {}
                override fun onAuthenticationFailure(message: String?, e: Exception?) {}
                override fun onSubscriptionSucceeded(channelName: String) {}
            }
        )
    }

    private fun cancelConsultation(id: String) {
        Log.d(TAG, "cancelConsultation — id=$id")
        ApiService.cancelConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) {
                Log.d(TAG, "cancelConsultation success=$response")
                if (response) finish()
            }
            override fun onFailure(error: String?) { Log.e(TAG, "cancelConsultation onFailure: $error") }
            override fun onRequestError(error: String?) { Log.e(TAG, "cancelConsultation onRequestError: $error") }
        })
    }
}

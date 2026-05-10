package com.altibbi.kotlinsdk.video

import com.altibbi.kotlinsdk.R
import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.content.res.ColorStateList
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Handler
import android.os.Looper
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Consultation
import com.opentok.android.*

class VideoActivity : AppCompatActivity() {

    companion object {
        private val TAG = VideoActivity::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 124
    }

    private var session: Session? = null
    private var publisher: Publisher? = null
    private var subscriber: Subscriber? = null

    private var apiKey: String? = null
    private var callId: String? = null
    private var token: String? = null
    private var voip: Boolean = false
    private var resolvedConsultationId: String? = null
    private var configRetried = false

    private var permissionsGranted = false
    private var consultationLoaded = false

    private var isVideoEnabled = true
    private var isAudioEnabled = true

    private lateinit var publisherContainer: FrameLayout
    private lateinit var subscriberContainer: FrameLayout
    private lateinit var btnToggleVideo: FrameLayout
    private lateinit var btnToggleAudio: FrameLayout
    private lateinit var btnEndCall: FrameLayout
    private lateinit var btnSwitchCamera: FrameLayout
    private lateinit var tvVideoLabel: TextView
    private lateinit var tvAudioLabel: TextView
    private lateinit var tvWaiting: TextView
    private lateinit var icVideo: ImageView
    private lateinit var icAudio: ImageView

    private val publisherListener = object : PublisherKit.PublisherListener {
        override fun onStreamCreated(kit: PublisherKit, stream: Stream) {
            Log.d(TAG, "Stream created: ${stream.streamId}")
        }
        override fun onStreamDestroyed(kit: PublisherKit, stream: Stream) {
            Log.d(TAG, "Stream destroyed: ${stream.streamId}")
        }
        override fun onError(kit: PublisherKit, error: OpentokError) {
            finishWithMessage("Publisher error: ${error.message}")
        }
    }

    private val sessionListener = object : Session.SessionListener {
        override fun onConnected(session: Session) {
            Log.d(TAG, "Session connected: ${session.sessionId}")
            publisher = Publisher.Builder(this@VideoActivity).build().also { pub ->
                pub.setPublisherListener(publisherListener)
                pub.renderer?.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL)
                publisherContainer.addView(pub.view)
                if (pub.view is GLSurfaceView) (pub.view as GLSurfaceView).setZOrderOnTop(true)
                if (voip) pub.publishVideo = false
            }
            session.publish(publisher)
        }

        override fun onDisconnected(session: Session) {
            Log.d(TAG, "Session disconnected")
        }

        override fun onStreamReceived(session: Session, stream: Stream) {
            Log.d(TAG, "Stream received: ${stream.streamId}")
            tvWaiting.visibility = View.GONE
            if (subscriber == null) {
                subscriber = Subscriber.Builder(this@VideoActivity, stream).build().also { sub ->
                    sub.renderer?.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL)
                    sub.setSubscriberListener(subscriberListener)
                    session.subscribe(sub)
                    subscriberContainer.addView(sub.view)
                }
            }
        }

        override fun onStreamDropped(session: Session, stream: Stream) {
            Log.d(TAG, "Stream dropped: ${stream.streamId}")
            if (subscriber != null) {
                subscriber = null
                subscriberContainer.removeAllViews()
                tvWaiting.visibility = View.VISIBLE
            }
        }

        override fun onError(session: Session, error: OpentokError) {
            finishWithMessage("Session error: ${error.message}")
        }
    }

    private val subscriberListener = object : SubscriberKit.SubscriberListener {
        override fun onConnected(kit: SubscriberKit) {
            Log.d(TAG, "Subscriber connected: ${kit.stream.streamId}")
        }
        override fun onDisconnected(kit: SubscriberKit) {
            Log.d(TAG, "Subscriber disconnected: ${kit.stream.streamId}")
        }
        override fun onError(kit: SubscriberKit, error: OpentokError) {
            finishWithMessage("Subscriber error: ${error.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        publisherContainer = findViewById(R.id.publisher_container)
        subscriberContainer = findViewById(R.id.subscriber_container)
        btnToggleVideo = findViewById(R.id.btn_toggle_video)
        btnToggleAudio = findViewById(R.id.btn_toggle_audio)
        btnEndCall = findViewById(R.id.btn_end_call)
        btnSwitchCamera = findViewById(R.id.btn_switch_camera)
        tvVideoLabel = findViewById(R.id.tv_video_label)
        tvAudioLabel = findViewById(R.id.tv_audio_label)
        tvWaiting = findViewById(R.id.tv_waiting)
        icVideo = findViewById(R.id.ic_video)
        icAudio = findViewById(R.id.ic_audio)
        applyControlState(btnToggleVideo, icVideo, tvVideoLabel, isVideoEnabled, "Video", "Off")
        applyControlState(btnToggleAudio, icAudio, tvAudioLabel, isAudioEnabled, "Audio", "Muted")

        btnToggleVideo.setOnClickListener { toggleVideo() }
        btnToggleAudio.setOnClickListener { toggleAudio() }
        btnEndCall.setOnClickListener { cancelAndFinish() }
        btnSwitchCamera.setOnClickListener { publisher?.cycleCamera() }

        checkAndRequestPermissions()
        loadConsultation(intent.getStringExtra("consultationId"))
    }

    override fun onPause() { super.onPause(); session?.onPause() }
    override fun onResume() { super.onResume(); session?.onResume() }

    private fun loadConsultation(consultationId: String?) {
        val callback = object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                applyConsultationConfig(response)
            }
            override fun onFailure(error: String?) {
                finishWithMessage("Failed to load consultation: $error")
            }
            override fun onRequestError(error: String?) {
                finishWithMessage("Failed to load consultation: $error")
            }
        }
        if (consultationId != null) {
            ApiService.getConsultationInfo(consultationId, callback)
        } else {
            ApiService.getLastConsultation(callback)
        }
    }

    private fun applyConsultationConfig(consultation: Consultation) {
        val config = consultation.videoConfig ?: consultation.voipConfig
        if (config == null) {
            if (!configRetried) {
                configRetried = true
                Log.w(TAG, "videoConfig null — retrying in 5s")
                Handler(Looper.getMainLooper()).postDelayed({
                    loadConsultation(consultation.id?.toString() ?: intent.getStringExtra("consultationId"))
                }, 5000)
            } else {
                finishWithMessage("No active video/voip session found.")
            }
            return
        }
        resolvedConsultationId = consultation.id?.toString()
        apiKey = config.apiKey
        callId = config.callId
        token = config.token
        voip = consultation.videoConfig == null && consultation.voipConfig != null
        consultationLoaded = true
        runOnUiThread {
            if (voip) {
                btnToggleVideo.visibility = View.GONE
                tvVideoLabel.visibility = View.GONE
            }
            maybeInitSession()
        }
    }

    private fun checkAndRequestPermissions() {
        val needed = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isEmpty()) {
            permissionsGranted = true
            maybeInitSession()
        } else {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            permissionsGranted = true
            maybeInitSession()
        }
    }

    private fun maybeInitSession() {
        if (permissionsGranted && consultationLoaded) {
            val key = apiKey ?: return
            val id = callId ?: return
            val tok = token ?: return
            initializeSession(key, id, tok)
        }
    }

    private fun initializeSession(apiKey: String, sessionId: String, token: String) {
        session = Session.Builder(this, apiKey, sessionId).build().also {
            it.setSessionListener(sessionListener)
            it.connect(token)
        }
    }

    private fun toggleVideo() {
        isVideoEnabled = !isVideoEnabled
        publisher?.publishVideo = isVideoEnabled
        applyControlState(btnToggleVideo, icVideo, tvVideoLabel, isVideoEnabled, "Video", "Off")
    }

    private fun toggleAudio() {
        isAudioEnabled = !isAudioEnabled
        publisher?.publishAudio = isAudioEnabled
        applyControlState(btnToggleAudio, icAudio, tvAudioLabel, isAudioEnabled, "Audio", "Muted")
    }

    private fun applyControlState(
        button: FrameLayout,
        icon: ImageView,
        label: TextView,
        enabled: Boolean,
        labelOn: String,
        labelOff: String
    ) {
        button.background = ContextCompat.getDrawable(
            this,
            if (enabled) R.drawable.bg_video_control_active else R.drawable.bg_video_control_inactive
        )
        val iconColor = if (enabled) ContextCompat.getColor(this, R.color.text_primary)
        else ContextCompat.getColor(this, android.R.color.white)
        icon.imageTintList = ColorStateList.valueOf(iconColor)
        label.text = if (enabled) labelOn else labelOff
        label.setTextColor(
            ContextCompat.getColor(
                this,
                if (enabled) android.R.color.white else R.color.error
            )
        )
    }

    private fun cancelAndFinish() {
        val id = resolvedConsultationId
        if (id == null) {
            finish()
            return
        }
        ApiService.cancelConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) { finish() }
            override fun onFailure(error: String?) { finish() }
            override fun onRequestError(error: String?) { finish() }
        })
    }

    private fun finishWithMessage(message: String) {
        Log.e(TAG, message)
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

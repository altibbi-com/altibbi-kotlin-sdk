package com.altibbi.kotlinsdk

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.opentok.android.*


class Video : AppCompatActivity() {

    private var session: Session? = null
    private var publisher: Publisher? = null
    private var subscriber: Subscriber? = null
    var apiKey: String? = null
    var callId: String? = null
    var token: String? = null
    var voip: Boolean = false
    private lateinit var enableVideoIcon: ImageView
    private lateinit var enableAudioIcon: ImageView
    private lateinit var switchCameraIcon: ImageView
    private lateinit var publisherViewContainer: FrameLayout
    private lateinit var subscriberViewContainer: FrameLayout

    private val publisherListener: PublisherKit.PublisherListener = object :
        PublisherKit.PublisherListener {
        override fun onStreamCreated(publisherKit: PublisherKit, stream: Stream) {
            Log.d(TAG, "onStreamCreated: Publisher Stream Created. Own stream ${stream.streamId}")
        }

        override fun onStreamDestroyed(publisherKit: PublisherKit, stream: Stream) {
            Log.d(
                TAG,
                "onStreamDestroyed: Publisher Stream Destroyed. Own stream ${stream.streamId}"
            )
        }

        override fun onError(publisherKit: PublisherKit, opentokError: OpentokError) {
            finishWithMessage("PublisherKit onError: ${opentokError.message}")
        }
    }
    private val sessionListener: Session.SessionListener = object : Session.SessionListener {
        override fun onConnected(session: Session) {
            Log.d(TAG, "onConnected: Connected to session: ${session.sessionId}")
            publisher = Publisher.Builder(this@Video).build()
            publisher?.setPublisherListener(publisherListener)
            publisher?.renderer?.setStyle(
                BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL
            )
            publisherViewContainer.addView(publisher?.view)
            if (publisher?.view is GLSurfaceView) {
                (publisher?.view as GLSurfaceView).setZOrderOnTop(true)
            }
            voip = intent.getBooleanExtra("voip", false)
            if(voip){
                publisher?.publishVideo = false
            }
            session.publish(publisher)
        }

        override fun onDisconnected(session: Session) {
            Log.d(TAG, "onDisconnected: Disconnected from session: ${session.sessionId}")
        }

        override fun onStreamReceived(session: Session, stream: Stream) {
            Log.d(
                TAG,
                "onStreamReceived: New Stream Received ${stream.streamId} in session: ${session.sessionId}"
            )
            if (subscriber == null) {
                subscriber = Subscriber.Builder(this@Video, stream).build().also {
                    it.renderer?.setStyle(
                        BaseVideoRenderer.STYLE_VIDEO_SCALE,
                        BaseVideoRenderer.STYLE_VIDEO_FILL
                    )

                    it.setSubscriberListener(subscriberListener)
                }

                session.subscribe(subscriber)
                subscriberViewContainer.addView(subscriber?.view)
            }
        }

        override fun onStreamDropped(session: Session, stream: Stream) {
            Log.d(
                TAG,
                "onStreamDropped: Stream Dropped: ${stream.streamId} in session: ${session.sessionId}"
            )
            if (subscriber != null) {
                subscriber = null
                subscriberViewContainer.removeAllViews()
            }
        }

        override fun onError(session: Session, opentokError: OpentokError) {
            finishWithMessage("Session error: ${opentokError.message}")
        }
    }
    var subscriberListener: SubscriberKit.SubscriberListener = object :
        SubscriberKit.SubscriberListener {
        override fun onConnected(subscriberKit: SubscriberKit) {
            Log.d(
                TAG,
                "onConnected: Subscriber connected. Stream: ${subscriberKit.stream.streamId}"
            )
        }

        override fun onDisconnected(subscriberKit: SubscriberKit) {
            Log.d(
                TAG,
                "onDisconnected: Subscriber disconnected. Stream: ${subscriberKit.stream.streamId}"
            )
        }

        override fun onError(subscriberKit: SubscriberKit, opentokError: OpentokError) {
            finishWithMessage("SubscriberKit onError: ${opentokError.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        publisherViewContainer = findViewById(R.id.publisher_container)
        subscriberViewContainer = findViewById(R.id.subscriber_container)
        enableVideoIcon = findViewById(R.id.enableVideoIcon)
        enableAudioIcon = findViewById(R.id.enableAudioIcon)
        switchCameraIcon = findViewById(R.id.switchCameraIcon)

        enableVideoIcon.setOnClickListener { toggleVideo() }
        enableAudioIcon.setOnClickListener { toggleAudio() }
        switchCameraIcon.setOnClickListener { switchCamera() }

        apiKey = intent.getStringExtra("apiKey")
        callId = intent.getStringExtra("callId")
        token = intent.getStringExtra("token")
        checkAndRequestPermissions()

    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check for Internet permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.INTERNET)
        }

        // Check for Camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        // Check for Record Audio permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            initializeSession(apiKey!!, callId!!, token!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                // Check if all requested permissions are granted
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(
                        TAG,
                        "granted $apiKey"
                    )
                    initializeSession(apiKey!!, callId!!, token!!)
                } else {
                    // Some permissions were not granted
                    // Handle this case (show a message, disable features, etc.)
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onPause() {
        super.onPause()
        session?.onPause()
    }

    override fun onResume() {
        super.onResume()
        session?.onResume()
    }

    private fun initializeSession(apiKey: String, sessionId: String, token: String) {

        /*
        The context used depends on the specific use case, but usually, it is desired for the session to
        live outside of the Activity e.g: live between activities. For a production applications,
        it's convenient to use Application context instead of Activity context.
         */
        session = Session.Builder(this, apiKey, sessionId).build().also {
            it.setSessionListener(sessionListener)
            it.connect(token)
        }
    }

    private fun finishWithMessage(message: String) {
        Log.e(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }


    private fun toggleVideo() {
        // Implement logic to enable/disable video
        // Example: publisher?.publishVideo = !publisher?.publishVideo!!
        publisher?.publishVideo = !publisher?.publishVideo!!
    }

    private fun toggleAudio() {
        // Implement logic to enable/disable audio
        // Example: publisher?.publishAudio = !publisher?.publishAudio!!
        publisher?.publishAudio = !publisher?.publishAudio!!
    }

    private fun switchCamera() {
        // Implement logic to switch the camera
        // Example: publisher?.cycleCamera()
        publisher?.cycleCamera()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 124
    }


}
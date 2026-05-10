package com.altibbi.kotlinsdk.chat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.altibbi.kotlinsdk.R
import com.altibbi.telehealth.AltibbiChat
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Consultation
import com.altibbi.telehealth.model.Media
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.PreviousMessageListQuery.MessageListQueryResult
import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdException
import com.sendbird.android.User
import com.sendbird.android.UserMessage
import java.io.File
import java.io.FileOutputStream

class ChatActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ChatActivity"
        private const val REQ_READ_EXTERNAL_STORAGE = 124
        private const val REQ_CAMERA = 125
    }

    var currentChannel: GroupChannel? = null
    private var resolvedConsultationId: String? = null
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoPickerLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var documentLauncher: ActivityResultLauncher<Array<String>>
    private var cameraImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setupLaunchers()

        val consultationId = intent.extras?.getString("consultationId")
        val messageInput = findViewById<TextView>(R.id.messageInput)
        val btnSend = findViewById<View>(R.id.buttonSendMessage1)
        val btnAttach = findViewById<View>(R.id.buttonAttachImage)
        val btnCancel = findViewById<View>(R.id.btn_cancel_consultation)
        val ivBack = findViewById<View>(R.id.iv_back)

        messageAdapter = MessageAdapter(this) { scrollToLastMessage() }
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        ivBack.setOnClickListener { cancelAndFinish() }
        btnCancel.visibility = View.GONE

        loadConsultation(this, consultationId) { resolvedId ->
            resolvedConsultationId = resolvedId
            runOnUiThread {
                btnCancel.visibility = View.VISIBLE
                btnCancel.setOnClickListener { cancelConsultation(resolvedId) }
            }
        }

        btnAttach.setOnClickListener { showAttachmentPicker() }

        btnSend.setOnClickListener {
            val text = messageInput.text.toString()
            if (text.isNotBlank()) {
                currentChannel?.sendUserMessage(text, object : BaseChannel.SendUserMessageHandler {
                    override fun onSent(msg: UserMessage?, e: SendBirdException?) {
                        if (e == null) {
                            msg?.let { messageAdapter.addMessage(it) }
                            messageInput.text = ""
                            scrollToLastMessage()
                        }
                    }
                })
            }
        }

        AltibbiChat.addChannelHandler("myChannelHandler", MyChannelHandler(this) { msg ->
            runOnUiThread {
                messageAdapter.addMessage(msg)
                scrollToLastMessage()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        AltibbiChat.addChannelHandler("myChannelHandler", MyChannelHandler(this) { msg ->
            runOnUiThread {
                messageAdapter.addMessage(msg)
                scrollToLastMessage()
            }
        })
    }

    private fun setupLaunchers() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { handleImageUri(it) }
            }
        }
        photoPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) handleImageUri(uri)
        }
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) cameraImageUri?.let { handleImageUri(it) }
        }
        documentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) handleImageUri(uri)
        }
    }

    private fun showAttachmentPicker() {
        AlertDialog.Builder(this)
            .setTitle("Attach file")
            .setItems(arrayOf("Camera", "Gallery", "Document")) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                    2 -> documentLauncher.launch(arrayOf("image/*", "application/pdf", "*/*"))
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA)
        }
    }

    private fun openCamera() {
        val file = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            photoPickerLauncher.launch(arrayOf("image/*"))
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_READ_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        when (requestCode) {
            REQ_READ_EXTERNAL_STORAGE -> if (granted) openGallery()
            REQ_CAMERA -> if (granted) openCamera()
        }
    }

    private fun handleImageUri(uri: Uri) {
        val file = uriToFile(this, uri)
        if (!file.exists()) return
        ApiService.uploadMedia(file, object : ApiCallback<Media> {
            override fun onSuccess(response: Media) {
                response.url?.let { url ->
                    runOnUiThread {
                        currentChannel?.sendUserMessage(url, object : BaseChannel.SendUserMessageHandler {
                            override fun onSent(msg: UserMessage?, e: SendBirdException?) {
                                if (e == null) {
                                    msg?.let { messageAdapter.addMessage(it) }
                                    scrollToLastMessage()
                                }
                            }
                        })
                    }
                }
            }
            override fun onFailure(error: String?) {}
            override fun onRequestError(error: String?) {}
        })
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.png")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { input.copyTo(it) }
        }
        return file
    }

    private fun loadConsultation(context: Context, consId: String?, onResolved: (String) -> Unit = {}) {
        Log.d(TAG, "loadConsultation — consId=$consId")
        val callback = object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                val id = response.id?.toString() ?: run {
                    Log.e(TAG, "loadConsultation — response.id is null")
                    return
                }
                Log.d(TAG, "loadConsultation — resolved id=$id status=${response.status} chatConfig=${response.chatConfig != null}")
                onResolved(id)
                runOnUiThread {
                    findViewById<TextView>(R.id.tv_doctor_name).text =
                        response.doctorName ?: "Your Doctor"
                    response.doctorAvatar?.let { url ->
                        findViewById<ImageView>(R.id.iv_doctor_avatar).load(url) {
                            crossfade(true)
                            placeholder(R.color.avatar_placeholder)
                        }
                    }
                }
                val config = response.chatConfig
                if (config == null) {
                    Log.e(TAG, "loadConsultation — chatConfig is null, cannot init chat")
                    return
                }
                val appId = config.appId
                val chatUserId = config.chatUserId
                val chatUserToken = config.chatUserToken
                val groupId = config.groupId
                Log.d(TAG, "loadConsultation — appId=$appId chatUserId=$chatUserId groupId=$groupId tokenPresent=${chatUserToken != null}")
                if (appId == null || chatUserId == null || chatUserToken == null || groupId == null) {
                    Log.e(TAG, "loadConsultation — incomplete chatConfig: appId=$appId chatUserId=$chatUserId groupId=$groupId")
                    return
                }
                AltibbiChat.init(appId, context, chatUserId, chatUserToken) {
                    Log.d(TAG, "loadConsultation — AltibbiChat connected, fetching channel channel_$groupId")
                    AltibbiChat.getChannel("channel_$groupId", object : AltibbiChat.Companion.ChannelCallback {
                        override fun onChannelReceived(channel: GroupChannel?) {
                            if (channel == null) {
                                Log.e(TAG, "loadConsultation — getChannel returned null")
                                return
                            }
                            Log.d(TAG, "loadConsultation — channel ready url=${channel.url}")
                            currentChannel = channel
                            channel.createPreviousMessageListQuery()?.load(100, false, object : MessageListQueryResult {
                                override fun onResult(messages: MutableList<BaseMessage>?, e: SendBirdException?) {
                                    if (e != null) {
                                        Log.e(TAG, "loadConsultation — load history failed: $e")
                                    } else {
                                        Log.d(TAG, "loadConsultation — loaded ${messages?.size ?: 0} messages")
                                        messages?.let { messageAdapter.addMessages(it) }
                                    }
                                }
                            })
                        }
                    })
                }
            }
            override fun onFailure(error: String?) {
                Log.e(TAG, "loadConsultation — onFailure: $error")
            }
            override fun onRequestError(error: String?) {
                Log.e(TAG, "loadConsultation — onRequestError: $error")
            }
        }
        if (consId != null) {
            ApiService.getConsultationInfo(consId, callback)
        } else {
            ApiService.getLastConsultation(callback)
        }
    }

    private fun scrollToLastMessage() {
        val last = messageAdapter.itemCount - 1
        if (last >= 0) recyclerView.scrollToPosition(last)
    }

    private fun cancelAndFinish() {
        val id = resolvedConsultationId
        if (id == null) {
            finish()
            return
        }
        cancelConsultation(id)
    }

    private fun cancelConsultation(id: String) {
        ApiService.cancelConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) { if (response) finish() }
            override fun onFailure(error: String?) { finish() }
            override fun onRequestError(error: String?) { finish() }
        })
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    class MessageAdapter(
        private val activity: ChatActivity,
        private val onMessagesAdded: () -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val messages = mutableListOf<BaseMessage>()

        companion object {
            private const val TYPE_USER = 0
            private const val TYPE_DOCTOR = 1
        }

        fun addMessage(message: BaseMessage) {
            messages.add(message)
            notifyItemInserted(messages.size - 1)
            onMessagesAdded()
        }

        fun addMessages(newMessages: List<BaseMessage>) {
            messages.addAll(newMessages)
            notifyDataSetChanged()
            onMessagesAdded()
        }

        override fun getItemViewType(position: Int): Int {
            val msg = messages[position]
            val isOwn = try {
                msg.sender?.userId == SendBird.getCurrentUser()?.userId
            } catch (e: Exception) { false }
            return if (isOwn) TYPE_USER else TYPE_DOCTOR
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = android.view.LayoutInflater.from(parent.context)
            return if (viewType == TYPE_USER) {
                UserVH(inflater.inflate(R.layout.item_message_user, parent, false))
            } else {
                DoctorVH(inflater.inflate(R.layout.item_message_doctor, parent, false))
            }
        }

        override fun getItemCount() = messages.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val msg = messages[position]
            val text = msg.message
            when (holder) {
                is UserVH -> holder.bind(text)
                is DoctorVH -> holder.bind(text)
            }
        }

        private fun isImageUrl(url: String): Boolean {
            val path = url.trim().split("?")[0]
            return path.matches(Regex(".*\\.(jpg|jpeg|png|gif|heic|webp)$", RegexOption.IGNORE_CASE))
        }

        private fun isExternalLink(url: String): Boolean {
            val t = url.trim()
            return (t.startsWith("http://", true) || t.startsWith("https://", true)) && !isImageUrl(url)
        }

        inner class UserVH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvMessage: TextView = view.findViewById(R.id.tv_message)
            private val ivImage: ImageView = view.findViewById(R.id.iv_image)

            fun bind(text: String) {
                if (isImageUrl(text)) {
                    tvMessage.visibility = View.GONE
                    ivImage.visibility = View.VISIBLE
                    ivImage.load(text.trim()) { crossfade(true) }
                    ivImage.setOnClickListener { showFullScreen(text) }
                } else {
                    ivImage.visibility = View.GONE
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = text
                    if (isExternalLink(text)) {
                        tvMessage.setOnClickListener {
                            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(text.trim())))
                        }
                    } else {
                        tvMessage.setOnClickListener(null)
                    }
                }
            }
        }

        inner class DoctorVH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvMessage: TextView = view.findViewById(R.id.messageText)
            private val ivImage: ImageView = view.findViewById(R.id.messageImage)

            fun bind(text: String) {
                if (isImageUrl(text)) {
                    tvMessage.visibility = View.GONE
                    ivImage.visibility = View.VISIBLE
                    ivImage.load(text.trim()) { crossfade(true) }
                    ivImage.setOnClickListener { showFullScreen(text) }
                } else {
                    ivImage.visibility = View.GONE
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = text
                    if (isExternalLink(text)) {
                        tvMessage.setOnClickListener {
                            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(text.trim())))
                        }
                    } else {
                        tvMessage.setOnClickListener(null)
                    }
                }
            }
        }

        private fun showFullScreen(url: String) {
            val imageView = ImageView(activity).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    activity.resources.displayMetrics.widthPixels,
                    (activity.resources.displayMetrics.widthPixels * 0.75f).toInt()
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                load(url.trim()) { crossfade(true) }
            }
            AlertDialog.Builder(activity)
                .setView(imageView)
                .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                .show()
        }
    }

    class MyChannelHandler(
        private val activity: Activity,
        private val onMessage: (BaseMessage) -> Unit
    ) : SendBird.ChannelHandler() {
        override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
            if (message.message.isNotEmpty()) onMessage(message)
        }
        override fun onTypingStatusUpdated(channel: GroupChannel?) {}
        override fun onUserLeft(channel: GroupChannel?, user: User?) {
            activity.finish()
        }
    }
}

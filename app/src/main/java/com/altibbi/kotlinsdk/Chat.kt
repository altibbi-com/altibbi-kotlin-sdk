package com.altibbi.kotlinsdk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
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

class Chat : AppCompatActivity() {
    var currentChannel: GroupChannel? = null
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val REQ_READ_EXTERNAL_STORAGE = 124
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        galleryActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri -> handleGalleryResult(uri) }
            }
        }
        val bundle = intent.extras
        val consultationId = bundle?.getString("consultationId")
        val buttonSendMessage = findViewById<Button>(R.id.buttonSendMessage1)
        val cancelConsultationButton = findViewById<Button>(R.id.button17)
        val messageInput: EditText = findViewById(R.id.messageInput)
        val buttonAttachImage = findViewById<Button>(R.id.buttonAttachImage)

        messageAdapter = MessageAdapter(this) { scrollToLastMessage() }
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (consultationId != null) {
            getConsultation(this, consultationId)
            cancelConsultationButton.setOnClickListener {
                cancelConsultation(consultationId)
            }
        }

        buttonAttachImage.setOnClickListener {
            if (hasReadPermission()) showImagePicker()
            else requestReadPermission()
        }

        buttonSendMessage.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotBlank()) {
                currentChannel?.sendUserMessage(message, object : BaseChannel.SendUserMessageHandler {
                    override fun onSent(userMessage: UserMessage?, e: SendBirdException?) {
                        if (e == null) {
                            userMessage?.let { messageAdapter.addMessage(it) }
                            messageInput.text.clear()
                            scrollToLastMessage()
                        } else {
                            println("Error sending message: ${e.message}")
                        }
                    }
                })
            }
        }

        val channelHandler = MyChannelHandler(
            activity = this,
            onChannelMessageReceived = { message: BaseMessage ->
                runOnUiThread {
                    messageAdapter.addMessage(message)
                    scrollToLastMessage()
                }
            }
        )
        AltibbiChat.addChannelHandler("myChannelHandler", channelHandler)
    }

    private fun hasReadPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestReadPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQ_READ_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_READ_EXTERNAL_STORAGE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            showImagePicker()
        }
    }

    private fun showImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream")
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { output ->
            inputStream.use { input -> input.copyTo(output) }
        }
        return file
    }

    private fun handleGalleryResult(uri: Uri) {
        val file = uriToFile(this, uri)
        if (!file.exists()) return
        ApiService.uploadMedia(file, object : ApiCallback<Media> {
            override fun onSuccess(response: Media) {
                response.url?.let { url ->
                    runOnUiThread {
                        currentChannel?.sendUserMessage(url, object : BaseChannel.SendUserMessageHandler {
                            override fun onSent(userMessage: UserMessage?, e: SendBirdException?) {
                                if (e == null) {
                                    userMessage?.let { messageAdapter.addMessage(it) }
                                    scrollToLastMessage()
                                } else {
                                    println("Error sending message: ${e.message}")
                                }
                            }
                        })
                    }
                }
            }
            override fun onFailure(error: String?) { println(error) }
            override fun onRequestError(error: String?) { println(error) }
        })
    }


    override fun onStart() {
        super.onStart()

        val channelHandler = MyChannelHandler(
            activity = this,
            onChannelMessageReceived = { message: BaseMessage ->
                runOnUiThread {
                    messageAdapter.addMessage(message)
                    scrollToLastMessage()
                }
            }
        )
        AltibbiChat.addChannelHandler("myChannelHandler",channelHandler)
    }

//    override fun onResume() {
//        super.onResume()
//
//        val channelHandler = MyChannelHandler(
//            activity = this,
//            onChannelMessageReceived = { message: BaseMessage ->
//                runOnUiThread {
//                    messageAdapter.addMessage(message)
//                    scrollToLastMessage()
//                }
//            }
//        )
//        AltibbiChat.addChannelHandler("myChannelHandler",channelHandler)
//    }

    class MessageAdapter(
        private val activity: Chat,
        private val scrollToLastMessage: () -> Unit
    ) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
        private val messages: MutableList<BaseMessage> = mutableListOf()

        fun addMessage(message: BaseMessage) {
            messages.add(message)
            notifyItemInserted(messages.size - 1)
        }

        fun addMessages(newMessages: List<BaseMessage>) {
            messages.addAll(newMessages)
            notifyDataSetChanged()
            scrollToLastMessage()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.bind(messages[position])
        }

        override fun getItemCount(): Int = messages.size

        private fun isImageLink(msg: String): Boolean {
            if (msg.isBlank()) return false
            val path = msg.trim().split("?")[0]
            return path.matches(Regex(".*\\.(jpg|jpeg|png|gif|heic|webp)$", RegexOption.IGNORE_CASE))
        }

        private fun isExternalLink(msg: String): Boolean {
            if (msg.isBlank() || isImageLink(msg)) return false
            val trimmed = msg.trim()
            return trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)
        }

        inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(message: BaseMessage) {
                val messageText: TextView = itemView.findViewById(R.id.messageText)
                val messageImage: ImageView = itemView.findViewById(R.id.messageImage)
                val isCurrentUserMessage = message.sender.userId == SendBird.getCurrentUser().userId
                val gravity = if (isCurrentUserMessage) Gravity.END else Gravity.START

                when (message) {
                    is UserMessage -> {
                        val text = message.message
                        if (isImageLink(text)) {
                            messageText.visibility = View.GONE
                            messageImage.visibility = View.VISIBLE
                            messageImage.load(text.trim()) {
                                crossfade(true)
                            }
                            messageImage.setOnClickListener {
                                val imageView = ImageView(activity).apply {
                                    layoutParams = android.widget.LinearLayout.LayoutParams(
                                        activity.resources.displayMetrics.widthPixels,
                                        (activity.resources.displayMetrics.widthPixels * 0.75f).toInt()
                                    )
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    load(text.trim()) { crossfade(true) }
                                }
                                AlertDialog.Builder(activity)
                                    .setView(imageView)
                                    .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                                    .show()
                            }
                        } else {
                            messageImage.visibility = View.GONE
                            messageImage.setOnClickListener(null)
                            messageText.visibility = View.VISIBLE
                            messageText.gravity = gravity
                            messageText.text = text
                            if (isExternalLink(text)) {
                                messageText.setOnClickListener {
                                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(text.trim())))
                                }
                            } else {
                                messageText.setOnClickListener(null)
                            }
                        }
                    }
                    else -> {
                        messageImage.visibility = View.GONE
                        messageImage.setOnClickListener(null)
                        messageText.visibility = View.VISIBLE
                        messageText.gravity = gravity
                        val text = message.message
                        messageText.text = text
                        if (isExternalLink(text)) {
                            messageText.setOnClickListener {
                                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(text.trim())))
                            }
                        } else {
                            messageText.setOnClickListener(null)
                        }
                    }
                }
            }
        }
    }

    class MyChannelHandler(
        private val activity: Activity,
        val onChannelMessageReceived: (BaseMessage) -> Unit
    ) : SendBird.ChannelHandler() {

        override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
            if (message.message.isNotEmpty()) {
                onChannelMessageReceived(message)
            }
        }
        override fun onTypingStatusUpdated(channel: GroupChannel?) {
            println("typing started from Dr side")
        }
        override fun onUserLeft(channel: GroupChannel?, user: User?) {
            println("Chat finished")
            activity.finish()
        }
    }

    private fun getConsultation(context: Context, consId: String) {

        ApiService.getConsultationInfo(consId, object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                println("get consultation info response is -> $response")
                response.chatConfig?.appId?.let { response.chatConfig!!.chatUserId?.let { it1 ->
                    AltibbiChat.init(it, context, it1, response.chatConfig!!.chatUserToken!!)
                    AltibbiChat.getChannel("channel_${response.chatConfig!!.groupId}", object :
                        AltibbiChat.Companion.ChannelCallback {
                        override fun onChannelReceived(channel: GroupChannel?) {
                            currentChannel = channel
                            currentChannel?.createPreviousMessageListQuery()?.load(100, false, object : MessageListQueryResult{
                                override fun onResult(
                                    p0: MutableList<BaseMessage>?,
                                    p1: SendBirdException?
                                ) {
                                    p0?.let { messageAdapter.addMessages(it) }
                                }
                            })
                        }
                    })

                } }
            }

            override fun onFailure(error: String?) {
                println(error)
            }

            override fun onRequestError(error: String?) {
                println(error)
            }


        })
    }

    private fun scrollToLastMessage() {
        val lastItemPosition = messageAdapter.itemCount - 1
        if (lastItemPosition >= 0) {
            recyclerView.scrollToPosition(lastItemPosition)
        }
    }

    private fun cancelConsultation(id: String){

        ApiService.cancelConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) {
                println("cancelConsultation response : $response")
                if (response){
                    finish()
                }
            }
            override fun onFailure(error: String?) {
                println(error)
            }
            override fun onRequestError(error: String?) {
                println(error)
            }
        })
    }
}


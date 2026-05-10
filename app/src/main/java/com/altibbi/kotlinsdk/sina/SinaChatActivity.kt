package com.altibbi.kotlinsdk.sina

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
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
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Media
import com.altibbi.telehealth.model.SinaMessage
import com.altibbi.telehealth.model.SinaResponse
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SinaChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_API_KEY = "api_key"
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_TOKEN = "token"
        const val EXTRA_VOIP = "voip"
        // Keys forwarded to Video activity (must match Video.kt's expected extras)
        private const val VIDEO_EXTRA_API_KEY = "apiKey"
        private const val VIDEO_EXTRA_CALL_ID = "callId"
        private const val VIDEO_EXTRA_TOKEN = "token"
        private const val VIDEO_EXTRA_VOIP = "voip"
        private const val TYPEWRITER_DELAY_MS = 40L
        private const val REQ_READ_EXTERNAL_STORAGE = 125
        private const val REQ_CAMERA = 126
    }

    private lateinit var sessionId: String
    private lateinit var adapter: ChatAdapter
    private lateinit var markwon: Markwon
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoPickerLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var documentLauncher: ActivityResultLauncher<Array<String>>
    private var pendingCameraUri: Uri? = null

    private val messages = mutableListOf<ChatItem>()
    private var isTyping = false
    private var isAttaching = false

    private var videoApiKey: String? = null
    private var videoCallId: String? = null
    private var videoToken: String? = null
    private var isVoip: Boolean = false

    sealed class ChatItem {
        data class UserMsg(val text: String, val time: String, val mediaUrl: String? = null) : ChatItem()
        data class SinaMsg(val text: String, val time: String, val mediaUrl: String? = null) : ChatItem()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sina_chat)

        sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: run { finish(); return }
        videoApiKey = intent.getStringExtra(EXTRA_API_KEY)
        videoCallId = intent.getStringExtra(EXTRA_CALL_ID)
        videoToken = intent.getStringExtra(EXTRA_TOKEN)
        isVoip = intent.getBooleanExtra(EXTRA_VOIP, false)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        markwon = Markwon.builder(this)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(this))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()

        layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        adapter = ChatAdapter()
        findViewById<RecyclerView>(R.id.rv_messages).apply {
            this.layoutManager = this@SinaChatActivity.layoutManager
            this.adapter = this@SinaChatActivity.adapter
        }

        setupGalleryLaunchers()
        setupInput()
        setupAttach()
        setupSuggestions()
        loadHistory()
        updateVideoMenuItem()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sina_chat, menu)
        menu.findItem(R.id.action_video_call)?.isVisible =
            videoApiKey != null && videoCallId != null && videoToken != null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_video_call -> { launchVideoCall(); true }
            R.id.action_end_session -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateVideoMenuItem() {
        invalidateOptionsMenu()
    }

    private fun launchVideoCall() {
        val apiKey = videoApiKey ?: return
        val callId = videoCallId ?: return
        val token = videoToken ?: return
        startActivity(
            Intent(this, com.altibbi.kotlinsdk.video.VideoActivity::class.java)
                .putExtra(VIDEO_EXTRA_API_KEY, apiKey)
                .putExtra(VIDEO_EXTRA_CALL_ID, callId)
                .putExtra(VIDEO_EXTRA_TOKEN, token)
                .putExtra(VIDEO_EXTRA_VOIP, isVoip)
        )
    }

    private fun setupGalleryLaunchers() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { handleImageUri(it) }
            }
        }
        photoPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) handleImageUri(uri)
        }
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uri = pendingCameraUri
            pendingCameraUri = null
            if (success && uri != null) handleImageUri(uri)
        }
        documentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) handleDocumentUri(uri)
        }
    }

    private fun setupAttach() {
        findViewById<MaterialButton>(R.id.btn_attach).setOnClickListener {
            if (isAttaching || isTyping) return@setOnClickListener
            showAttachChooser()
        }
    }

    private fun showAttachChooser() {
        val items = arrayOf(
            getString(R.string.sina_chat_attach_camera),
            getString(R.string.sina_chat_attach_gallery),
            getString(R.string.sina_chat_attach_document)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.sina_chat_attach_choose)
            .setItems(items) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> openCameraWithPermission()
                    1 -> openGalleryFlow()
                    2 -> openDocumentPicker()
                }
            }
            .show()
    }

    private fun openDocumentPicker() {
        val mimeTypes = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "text/csv"
        )
        documentLauncher.launch(mimeTypes)
    }

    private fun openGalleryFlow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            photoPickerLauncher.launch(arrayOf("image/*"))
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_READ_EXTERNAL_STORAGE)
        }
    }

    private fun openCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA)
        }
    }

    private fun launchCamera() {
        val file = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg").apply { createNewFile() }
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    private fun openImagePicker() {
        galleryLauncher.launch(Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) openImagePicker()
            }
            REQ_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    android.widget.Toast.makeText(this, R.string.sina_chat_camera_permission_denied, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleImageUri(uri: Uri) {
        setAttaching(true)
        // Show optimistic message with local URI
        val time = formatTime(null)
        val placeholderIndex = messages.size
        messages.add(ChatItem.UserMsg("", time, uri.toString()))
        adapter.notifyItemInserted(placeholderIndex)
        scrollToBottom()
        updateSuggestionsVisibility()

        val file = uriToFile(this, uri)
        ApiService.uploadSinaMedia(file, object : ApiCallback<Media> {
            override fun onSuccess(response: Media) {
                val mediaId = response.id ?: run {
                    runOnUiThread {
                        messages.removeAt(placeholderIndex)
                        adapter.notifyItemRemoved(placeholderIndex)
                        setAttaching(false)
                    }
                    return
                }
                runOnUiThread {
                    setAttaching(false)
                    setTyping(true)
                    ApiService.sendSinaMessage(sessionId, "", mediaId, object : ApiCallback<SinaResponse> {
                        override fun onSuccess(response: SinaResponse) {
                            val actualUrl = response.userMessage?.media?.url ?: uri.toString()
                            val aiText = response.sinaMessage?.text
                                ?: response.sinaMessage?.data?.extra?.generalAnswer
                                ?: ""
                            runOnUiThread {
                                messages[placeholderIndex] = ChatItem.UserMsg("", time, actualUrl)
                                adapter.notifyItemChanged(placeholderIndex)
                                setTyping(false)
                                if (aiText.isBlank()) {
                                    appendSinaMessage(getString(R.string.sina_chat_error_generic), formatTime(null))
                                } else {
                                    appendSinaMessageWithTypewriter(aiText, formatTime(null))
                                }
                            }
                        }
                        override fun onFailure(error: String?) {
                            runOnUiThread {
                                setTyping(false)
                                appendSinaMessage(getString(R.string.sina_chat_error_generic), formatTime(null))
                            }
                        }
                        override fun onRequestError(error: String?) = onFailure(error)
                    })
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread {
                    messages.removeAt(placeholderIndex)
                    adapter.notifyItemRemoved(placeholderIndex)
                    setAttaching(false)
                }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.png")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { input.copyTo(it) }
        }
        return file
    }

    private fun queryDisplayName(uri: Uri): String {
        var name = "document"
        contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = c.getString(idx) ?: name
            }
        }
        return name
    }

    private fun uriToNamedFile(uri: Uri, displayName: String): File {
        val safe = displayName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val file = File(cacheDir, "doc_${System.currentTimeMillis()}_$safe")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { input.copyTo(it) }
        }
        return file
    }

    private fun handleDocumentUri(uri: Uri) {
        setAttaching(true)
        val displayName = queryDisplayName(uri)
        val time = formatTime(null)
        val placeholderIndex = messages.size
        val labelText = getString(R.string.sina_chat_document_label, displayName)
        messages.add(ChatItem.UserMsg(labelText, time, null))
        adapter.notifyItemInserted(placeholderIndex)
        scrollToBottom()
        updateSuggestionsVisibility()

        val file = uriToNamedFile(uri, displayName)
        ApiService.uploadSinaMedia(file, object : ApiCallback<Media> {
            override fun onSuccess(response: Media) {
                val mediaId = response.id ?: run {
                    runOnUiThread {
                        messages.removeAt(placeholderIndex)
                        adapter.notifyItemRemoved(placeholderIndex)
                        setAttaching(false)
                    }
                    return
                }
                val mediaUrl = response.url
                runOnUiThread {
                    setAttaching(false)
                    setTyping(true)
                    if (mediaUrl != null) {
                        messages[placeholderIndex] = ChatItem.UserMsg(labelText, time, mediaUrl)
                        adapter.notifyItemChanged(placeholderIndex)
                    }
                    ApiService.sendSinaMessage(sessionId, "", mediaId, object : ApiCallback<SinaResponse> {
                        override fun onSuccess(response: SinaResponse) {
                            val aiText = response.sinaMessage?.text
                                ?: response.sinaMessage?.data?.extra?.generalAnswer
                                ?: ""
                            runOnUiThread {
                                setTyping(false)
                                if (aiText.isBlank()) {
                                    appendSinaMessage(getString(R.string.sina_chat_error_generic), formatTime(null))
                                } else {
                                    appendSinaMessageWithTypewriter(aiText, formatTime(null))
                                }
                            }
                        }
                        override fun onFailure(error: String?) {
                            runOnUiThread {
                                setTyping(false)
                                appendSinaMessage(getString(R.string.sina_chat_error_generic), formatTime(null))
                            }
                        }
                        override fun onRequestError(error: String?) = onFailure(error)
                    })
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread {
                    messages.removeAt(placeholderIndex)
                    adapter.notifyItemRemoved(placeholderIndex)
                    setAttaching(false)
                }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun setupInput() {
        val etMessage = findViewById<TextInputEditText>(R.id.et_message)
        val btnSend = findViewById<MaterialButton>(R.id.btn_send)

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnSend.isEnabled = s?.isNotBlank() == true && !isTyping && !isAttaching
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSend.setOnClickListener {
            val text = etMessage.text?.toString()?.trim() ?: return@setOnClickListener
            if (text.isBlank() || isTyping || isAttaching) return@setOnClickListener
            etMessage.setText("")
            sendMessage(text)
        }
    }

    private fun setupSuggestions() {
        val chipGroup = findViewById<ChipGroup>(R.id.chip_group_suggestions)
        val suggestions = listOf(
            getString(R.string.sina_suggestion_1),
            getString(R.string.sina_suggestion_2),
            getString(R.string.sina_suggestion_3),
            getString(R.string.sina_suggestion_4),
            getString(R.string.sina_suggestion_5),
            getString(R.string.sina_suggestion_6)
        )
        suggestions.forEach { suggestion ->
            val chip = Chip(this).apply {
                text = suggestion
                isCheckable = false
                setOnClickListener {
                    findViewById<TextInputEditText>(R.id.et_message).setText(suggestion)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun updateSuggestionsVisibility() {
        val scroll = findViewById<View>(R.id.scroll_suggestions)
        scroll.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadHistory() {
        ApiService.getSinaChatMessages(sessionId, 1, 50, object : ApiCallback<List<SinaMessage>> {
            override fun onSuccess(response: List<SinaMessage>) {
                val items = response.reversed().map { msg ->
                    val time = formatTime(msg.createdAt)
                    val mediaUrl = msg.media?.url
                    if (msg.sender == "sina" || msg.sender == "ai") {
                        ChatItem.SinaMsg(msg.text.orEmpty(), time, mediaUrl)
                    } else {
                        ChatItem.UserMsg(msg.text.orEmpty(), time, mediaUrl)
                    }
                }
                runOnUiThread {
                    messages.addAll(items)
                    if (messages.isEmpty()) showGreeting()
                    adapter.notifyDataSetChanged()
                    scrollToBottom()
                    updateSuggestionsVisibility()
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread {
                    showGreeting()
                    updateSuggestionsVisibility()
                }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun showGreeting() {
        messages.add(ChatItem.SinaMsg(
            getString(R.string.sina_chat_greeting),
            formatTime(null)
        ))
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun sendMessage(text: String) {
        val time = formatTime(null)
        messages.add(ChatItem.UserMsg(text, time))
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
        updateSuggestionsVisibility()

        setTyping(true)

        ApiService.sendSinaMessage(sessionId, text, null, object : ApiCallback<SinaResponse> {
            override fun onSuccess(response: SinaResponse) {
                val aiText = response.sinaMessage?.text
                    ?: response.sinaMessage?.data?.extra?.generalAnswer
                    ?: ""
                runOnUiThread {
                    setTyping(false)
                    if (aiText.isBlank()) {
                        appendSinaMessage(getString(R.string.sina_chat_error_read), formatTime(null))
                    } else {
                        appendSinaMessageWithTypewriter(aiText, formatTime(null))
                    }
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread {
                    setTyping(false)
                    appendSinaMessage(getString(R.string.sina_chat_error_generic), formatTime(null))
                }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun appendSinaMessageWithTypewriter(fullText: String, time: String) {
        val index = messages.size
        messages.add(ChatItem.SinaMsg("", time))
        adapter.notifyItemInserted(index)
        scrollToBottom()

        val tokens = Regex("\\S+\\s*|\\s+").findAll(fullText).map { it.value }.toList()
        val handler = Handler(Looper.getMainLooper())
        var tokenIndex = 0
        val builder = StringBuilder()

        fun revealNextToken() {
            if (tokenIndex >= tokens.size) return
            builder.append(tokens[tokenIndex])
            tokenIndex++
            messages[index] = ChatItem.SinaMsg(builder.toString(), time)
            adapter.notifyItemChanged(index)
            scrollToBottom()
            if (tokenIndex < tokens.size) handler.postDelayed(::revealNextToken, TYPEWRITER_DELAY_MS)
        }
        handler.post(::revealNextToken)
    }

    private fun appendSinaMessage(text: String, time: String) {
        messages.add(ChatItem.SinaMsg(text, time))
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun setTyping(typing: Boolean) {
        isTyping = typing
        findViewById<View>(R.id.layout_typing).visibility = if (typing) View.VISIBLE else View.GONE
        refreshInputState()
    }

    private fun setAttaching(attaching: Boolean) {
        isAttaching = attaching
        refreshInputState()
    }

    private fun refreshInputState() {
        val busy = isTyping || isAttaching
        val etMessage = findViewById<TextInputEditText>(R.id.et_message)
        val btnSend = findViewById<MaterialButton>(R.id.btn_send)
        val btnAttach = findViewById<MaterialButton>(R.id.btn_attach)
        btnSend.isEnabled = !busy && etMessage.text?.isNotBlank() == true
        btnAttach.isEnabled = !busy
    }

    private fun scrollToBottom() {
        if (messages.isNotEmpty()) {
            findViewById<RecyclerView>(R.id.rv_messages).scrollToPosition(messages.size - 1)
        }
    }

    private fun formatTime(isoDate: String?): String {
        return try {
            if (isoDate == null) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val date = sdf.parse(isoDate) ?: Date()
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }
    }

    private fun isImageUrl(url: String): Boolean {
        val path = url.trim().split("?")[0]
        return path.matches(Regex(".*\\.(jpg|jpeg|png|gif|heic|webp)$", RegexOption.IGNORE_CASE))
                || url.startsWith("content://")
    }

    private fun isExternalLink(url: String): Boolean {
        val trimmed = url.trim()
        return (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true))
                && !isImageUrl(url)
    }

    private fun showFullScreenImage(url: String) {
        val imageView = ImageView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                resources.displayMetrics.widthPixels,
                (resources.displayMetrics.widthPixels * 0.75f).toInt()
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            load(url) { crossfade(true) }
        }
        AlertDialog.Builder(this)
            .setView(imageView)
            .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
            .show()
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    inner class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val TYPE_USER = 0
        private val TYPE_SINA = 1

        override fun getItemViewType(position: Int) =
            if (messages[position] is ChatItem.UserMsg) TYPE_USER else TYPE_SINA

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = layoutInflater
            return if (viewType == TYPE_USER) {
                UserVH(inflater.inflate(R.layout.item_message_user, parent, false))
            } else {
                SinaVH(inflater.inflate(R.layout.item_message_sina, parent, false))
            }
        }

        override fun getItemCount() = messages.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = messages[position]) {
                is ChatItem.UserMsg -> (holder as UserVH).bind(item)
                is ChatItem.SinaMsg -> (holder as SinaVH).bind(item)
            }
        }

        inner class UserVH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvMessage: TextView = view.findViewById(R.id.tv_message)
            private val ivImage: ImageView = view.findViewById(R.id.iv_image)
            private val tvTime: TextView = view.findViewById(R.id.tv_time)

            fun bind(item: ChatItem.UserMsg) {
                tvTime.text = item.time
                val mediaUrl = item.mediaUrl
                if (mediaUrl != null && isImageUrl(mediaUrl)) {
                    tvMessage.visibility = View.GONE
                    ivImage.visibility = View.VISIBLE
                    ivImage.load(mediaUrl) { crossfade(true) }
                    ivImage.setOnClickListener { showFullScreenImage(mediaUrl) }
                } else if (mediaUrl != null && isExternalLink(mediaUrl)) {
                    ivImage.visibility = View.GONE
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = if (item.text.isNotBlank()) item.text else mediaUrl
                    tvMessage.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mediaUrl.trim())))
                    }
                } else {
                    ivImage.visibility = View.GONE
                    ivImage.setOnClickListener(null)
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = item.text
                    if (isExternalLink(item.text)) {
                        tvMessage.setOnClickListener {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.text.trim())))
                        }
                    } else {
                        tvMessage.setOnClickListener(null)
                    }
                }
            }
        }

        inner class SinaVH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvMessage: TextView = view.findViewById(R.id.tv_message)
            private val ivImage: ImageView = view.findViewById(R.id.iv_image)
            private val tvTime: TextView = view.findViewById(R.id.tv_time)

            fun bind(item: ChatItem.SinaMsg) {
                tvTime.text = item.time
                val mediaUrl = item.mediaUrl
                if (mediaUrl != null && isImageUrl(mediaUrl)) {
                    tvMessage.visibility = View.GONE
                    ivImage.visibility = View.VISIBLE
                    ivImage.load(mediaUrl) { crossfade(true) }
                    ivImage.setOnClickListener { showFullScreenImage(mediaUrl) }
                } else {
                    ivImage.visibility = View.GONE
                    ivImage.setOnClickListener(null)
                    tvMessage.visibility = View.VISIBLE
                    markwon.setMarkdown(tvMessage, item.text)
                    if (isExternalLink(item.text)) {
                        tvMessage.setOnClickListener {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.text.trim())))
                        }
                    } else {
                        tvMessage.setOnClickListener(null)
                    }
                }
            }
        }
    }
}

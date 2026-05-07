package com.altibbi.kotlinsdk.consultation

import com.altibbi.kotlinsdk.R
import com.altibbi.kotlinsdk.chat.WaitingRoomActivity
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Consultation
import com.altibbi.telehealth.model.Media
import com.altibbi.telehealth.model.Medium
import com.altibbi.telehealth.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream

class NewConsultationActivity : AppCompatActivity() {

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoPickerLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var documentLauncher: ActivityResultLauncher<Array<String>>
    private var cameraImageUri: Uri? = null

    private val uploadedMediaIds = mutableListOf<String>()
    private var isCreating = false

    companion object {
        private const val REQ_READ_EXTERNAL_STORAGE = 123
        private const val REQ_CAMERA = 126
        private const val FORCE_WL_PARTNER = "partnerTest"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_consultation)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = "New Consultation"

        setupGalleryLauncher()
        setupPhotoPickerLauncher()

        findViewById<View>(R.id.btn_attach_media).setOnClickListener { showAttachmentPicker() }
        findViewById<View>(R.id.btn_remove_media).setOnClickListener { clearMedia() }
        findViewById<View>(R.id.btn_create_consultation).setOnClickListener {
            if (!isCreating) submitConsultation()
        }
        findViewById<View>(R.id.btn_active_consultation).setOnClickListener { getLastConsultation() }

        fetchMainUserId()
    }

    override fun onResume() {
        super.onResume()
        resetForm()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun resetForm() {
        findViewById<RadioGroup>(R.id.rg_medium).check(R.id.rb_chat)
        findViewById<TextInputEditText>(R.id.et_question).setText("")
        clearMedia()
        hideStatus()
        setCreating(false)
        fetchMainUserId()
    }

    private fun fetchMainUserId() {
        ApiService.getUsers(object : ApiCallback<List<User>> {
            override fun onSuccess(response: List<User>) {
                val mainUserId = response.firstOrNull()?.id
                runOnUiThread {
                    if (!mainUserId.isNullOrBlank()) {
                        findViewById<TextInputEditText>(R.id.et_user_id).setText(mainUserId)
                    } else {
                        showStatus("No users found to auto-fill User ID.", true)
                    }
                }
            }

            override fun onFailure(error: String?) {
                runOnUiThread {
                    showStatus(error ?: "Failed to fetch users.", true)
                }
            }

            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun selectedMedium(): Medium {
        return when (findViewById<RadioGroup>(R.id.rg_medium).checkedRadioButtonId) {
            R.id.rb_gsm -> Medium.gsm
            R.id.rb_video -> Medium.video
            R.id.rb_voip -> Medium.voip
            else -> Medium.chat
        }
    }

    private fun setCreating(loading: Boolean) {
        isCreating = loading
        val btn = findViewById<MaterialButton>(R.id.btn_create_consultation)
        btn.isEnabled = !loading
        btn.text = if (loading) "Creating…" else "Create Consultation"
    }

    private fun showStatus(message: String, isError: Boolean) {
        val tv = findViewById<TextView>(R.id.tv_create_status)
        tv.visibility = View.VISIBLE
        tv.text = message
        tv.background = ContextCompat.getDrawable(this, if (isError) R.drawable.bg_feedback_error else R.drawable.bg_feedback_success)
        tv.setTextColor(ContextCompat.getColor(this, if (isError) R.color.error else R.color.success))
    }

    private fun hideStatus() {
        findViewById<TextView>(R.id.tv_create_status).visibility = View.GONE
    }

    private fun setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) result.data?.data?.let { handleImageUri(it) }
        }
    }

    private fun setupPhotoPickerLauncher() {
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
            galleryLauncher.launch(Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
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
        setMediaState(MediaState.UPLOADING)
        val file = uriToFile(this, uri)
        ApiService.uploadMedia(file, object : ApiCallback<Media> {
            override fun onSuccess(response: Media) {
                response.id?.let { uploadedMediaIds.add(it) }
                runOnUiThread {
                    findViewById<ShapeableImageView>(R.id.img_preview).setImageURI(uri)
                    setMediaState(MediaState.PREVIEW)
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread {
                    setMediaState(MediaState.DEFAULT)
                    showStatus("Failed to upload image.", true)
                }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private enum class MediaState { DEFAULT, UPLOADING, PREVIEW }

    private fun setMediaState(state: MediaState) {
        findViewById<View>(R.id.btn_attach_media).visibility = if (state == MediaState.DEFAULT) View.VISIBLE else View.GONE
        findViewById<View>(R.id.layout_upload_progress).visibility = if (state == MediaState.UPLOADING) View.VISIBLE else View.GONE
        findViewById<FrameLayout>(R.id.frame_preview).visibility = if (state == MediaState.PREVIEW) View.VISIBLE else View.GONE
    }

    private fun clearMedia() {
        uploadedMediaIds.clear()
        setMediaState(MediaState.DEFAULT)
    }

    private fun submitConsultation() {
        val question = findViewById<TextInputEditText>(R.id.et_question).text?.toString().orEmpty()
        val userId = findViewById<TextInputEditText>(R.id.et_user_id).text?.toString()?.toIntOrNull() ?: 0

        if (question.length < 10 || userId == 0) {
            showStatus("Question too short or User ID missing.", true)
            return
        }

        hideStatus()
        setCreating(true)

        ApiService.createConsultation(
            question = question,
            medium = selectedMedium(),
            userID = userId,
            mediaIDs = uploadedMediaIds.takeIf { it.isNotEmpty() }?.toList(),
            followUpId = null,
            scheduledTo = null,
            forceWhiteLabelingPartnerName = FORCE_WL_PARTNER,
            callback = object : ApiCallback<Consultation> {
                override fun onSuccess(response: Consultation) {
                    runOnUiThread {
                        setCreating(false)
                        clearMedia()
                        if (response.status == "new" || response.status == "scheduled") {
                            startActivity(Intent(applicationContext, WaitingRoomActivity::class.java))
                        } else {
                            getLastConsultation()
                        }
                    }
                }
                override fun onFailure(error: String?) {
                    runOnUiThread {
                        setCreating(false)
                        showStatus(error ?: "Failed to create consultation.", true)
                    }
                }
                override fun onRequestError(error: String?) = onFailure(error)
            }
        )
    }

    private fun getLastConsultation() {
        ApiService.getLastConsultation(object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                if (response.status == "new" || response.status == "in_progress") {
                    startActivity(Intent(applicationContext, WaitingRoomActivity::class.java))
                } else {
                    runOnUiThread { showStatus("No active consultation found.", false) }
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread { showStatus("Failed to fetch active consultation.", true) }
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
}

package com.altibbi.kotlinsdk.consultation

import com.altibbi.kotlinsdk.R
import com.altibbi.kotlinsdk.chat.WaitingRoomActivity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Consultation
import com.altibbi.telehealth.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ConsultationListActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var empty: TextView
    private lateinit var adapter: ConsultationListAdapter
    private lateinit var userIdInput: TextInputEditText
    private lateinit var statusView: TextView

    private var userId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation_list)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.action_my_consultations)
        toolbar.subtitle = "Loading user..."

        progress = findViewById(R.id.list_progress)
        empty = findViewById(R.id.list_empty)
        recycler = findViewById(R.id.recycler_consultations)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        userIdInput = findViewById(R.id.et_consultation_user_id)
        statusView = findViewById(R.id.tv_consultation_status)

        adapter = ConsultationListAdapter(
            onViewDetails = { id -> openDetails(id) },
            onDeleteConsultation = { id -> confirmDeleteConsultation(id) },
            onShowMedia = { url -> openMediaUrl(url) },
            onDeleteMedia = { mediaId -> confirmDeleteMedia(mediaId) },
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        swipeRefresh.setOnRefreshListener { loadConsultations(showInitialSpinner = false) }
        swipeRefresh.setColorSchemeResources(R.color.primary)
        findViewById<MaterialButton>(R.id.btn_load_consultations).setOnClickListener {
            loadConsultations(showInitialSpinner = true)
        }
        findViewById<MaterialButton>(R.id.btn_last_consultation).setOnClickListener {
            getLastConsultation()
        }

        fetchMainUserIdAndLoad()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadConsultations(showInitialSpinner: Boolean) {
        val resolvedUserId = userIdInput.text?.toString()?.trim()?.toIntOrNull()
        if (resolvedUserId == null || resolvedUserId == 0) {
            progress.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            recycler.visibility = View.GONE
            empty.visibility = View.GONE
            showStatus("User ID missing.", true)
            toolbar.subtitle = "User ID required"
            return
        }
        userId = resolvedUserId
        toolbar.subtitle = getString(R.string.consultation_list_user_subtitle, resolvedUserId)
        hideStatus()

        if (showInitialSpinner) {
            progress.visibility = View.VISIBLE
            recycler.visibility = View.GONE
            empty.visibility = View.GONE
        }

        try {
        ApiService.getConsultationList(
            page = 1,
            perPage = 50,
            userId = resolvedUserId,
            sort = "-id",
            callback = object : ApiCallback<List<Consultation>> {
                override fun onSuccess(response: List<Consultation>) {
                    runOnUiThread {
                        progress.visibility = View.GONE
                        swipeRefresh.isRefreshing = false
                        hideStatus()
                        adapter.submitList(response)
                        val isEmpty = response.isEmpty()
                        empty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                        recycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
                    }
                }

                override fun onFailure(error: String?) {
                    runOnUiThread {
                        progress.visibility = View.GONE
                        swipeRefresh.isRefreshing = false
                        showStatus(error ?: getString(R.string.app_name), true)
                    }
                }

                override fun onRequestError(error: String?) {
                    onFailure(error)
                }
            },
        )
        } catch (e: Exception) {
            progress.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            empty.visibility = View.GONE
            showStatus(e.message ?: e.toString(), true)
        }
    }

    private fun fetchMainUserIdAndLoad() {
        ApiService.getUsers(object : ApiCallback<List<User>> {
            override fun onSuccess(response: List<User>) {
                val mainUserId = response.firstOrNull()?.id
                runOnUiThread {
                    if (!mainUserId.isNullOrBlank()) {
                        userIdInput.setText(mainUserId)
                        hideStatus()
                        loadConsultations(showInitialSpinner = true)
                    } else {
                        progress.visibility = View.GONE
                        empty.visibility = View.GONE
                        showStatus("No users found to auto-fill User ID.", true)
                        toolbar.subtitle = "No user found"
                    }
                }
            }

            override fun onFailure(error: String?) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    empty.visibility = View.GONE
                    showStatus(error ?: "Failed to fetch users.", true)
                    toolbar.subtitle = "Failed to load user"
                }
            }

            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun getLastConsultation() {
        ApiService.getLastConsultation(object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                if (response.status == "new" || response.status == "in_progress") {
                    startActivity(Intent(applicationContext, WaitingRoomActivity::class.java))
                } else {
                    runOnUiThread {
                        showStatus("Last consultation status: ${response.status}", false)
                    }
                }
            }

            override fun onFailure(error: String?) {
                runOnUiThread {
                    showStatus(error ?: "Failed to fetch last consultation.", true)
                }
            }

            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun openDetails(consultationId: Int) {
        startActivity(
            Intent(this, ConsultationDetailsActivity::class.java)
                .putExtra(ConsultationDetailsActivity.EXTRA_CONSULTATION_ID, consultationId),
        )
    }

    private fun confirmDeleteConsultation(id: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_consultation_title)
            .setMessage(getString(R.string.dialog_delete_consultation_message, id))
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_positive_delete) { _, _ ->
                ApiService.deleteConsultation(id.toString(), object : ApiCallback<Boolean> {
                    override fun onSuccess(response: Boolean) {
                        runOnUiThread {
                            Toast.makeText(
                                this@ConsultationListActivity,
                                R.string.toast_deleted_consultation,
                                Toast.LENGTH_SHORT,
                            ).show()
                            loadConsultations(showInitialSpinner = false)
                        }
                    }

                    override fun onFailure(error: String?) {
                        runOnUiThread {
                            Toast.makeText(
                                this@ConsultationListActivity,
                                error ?: "",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }

                    override fun onRequestError(error: String?) {
                        onFailure(error)
                    }
                })
            }
            .show()
    }

    private fun openMediaUrl(url: String) {
        if (url.isBlank()) {
            Toast.makeText(this, R.string.toast_media_url_missing, Toast.LENGTH_SHORT).show()
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(this, R.string.toast_open_url_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteMedia(mediaId: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_media_title)
            .setMessage(getString(R.string.dialog_delete_media_message, mediaId))
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_positive_delete) { _, _ ->
                ApiService().deleteMedia(mediaId, object : ApiCallback<Boolean> {
                    override fun onSuccess(response: Boolean) {
                        runOnUiThread {
                            Toast.makeText(
                                this@ConsultationListActivity,
                                R.string.toast_deleted_media,
                                Toast.LENGTH_SHORT,
                            ).show()
                            loadConsultations(showInitialSpinner = false)
                        }
                    }

                    override fun onFailure(error: String?) {
                        runOnUiThread {
                            Toast.makeText(
                                this@ConsultationListActivity,
                                error ?: "",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }

                    override fun onRequestError(error: String?) {
                        onFailure(error)
                    }
                })
            }
            .show()
    }

    private fun showStatus(message: String, isError: Boolean) {
        statusView.visibility = View.VISIBLE
        statusView.text = message
        statusView.background = ContextCompat.getDrawable(
            this,
            if (isError) R.drawable.bg_feedback_error else R.drawable.bg_feedback_success,
        )
        statusView.setTextColor(
            ContextCompat.getColor(this, if (isError) R.color.error else R.color.success),
        )
    }

    private fun hideStatus() {
        statusView.visibility = View.GONE
    }
}

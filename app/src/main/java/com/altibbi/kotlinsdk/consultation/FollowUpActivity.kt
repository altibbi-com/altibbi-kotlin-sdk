package com.altibbi.kotlinsdk.consultation

import com.altibbi.kotlinsdk.R
import com.altibbi.kotlinsdk.chat.WaitingRoomActivity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Consultation
import com.altibbi.telehealth.model.ConsultationAvailableShifts
import com.altibbi.telehealth.model.Medium
import com.altibbi.telehealth.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class FollowUpActivity : AppCompatActivity() {

    private lateinit var followUpShiftAdapter: ArrayAdapter<String>

    private var isSubmitting = false
    private var selectedFollowUpShift: String? = null
    private val followUpShiftLabels = mutableListOf("No follow-up shift selected")
    private val followUpShiftValues = mutableListOf<String?>()

    companion object {
        private const val FORCE_WL_PARTNER = "partnerTest"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_up)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = "Follow Up"

        setupShiftSpinner()

        findViewById<View>(R.id.btn_submit_followup).setOnClickListener {
            if (!isSubmitting) submitFollowUp()
        }
        findViewById<View>(R.id.btn_get_shifts).setOnClickListener { fetchShiftsFromInputs() }
        findViewById<View>(R.id.btn_submit_scheduled).setOnClickListener {
            if (!isSubmitting) submitScheduledFollowUp()
        }

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
        findViewById<TextInputEditText>(R.id.et_followup_question).setText("")
        findViewById<TextInputEditText>(R.id.et_followup_id).setText("")
        findViewById<TextInputEditText>(R.id.et_shift_date).setText("")
        hideStatus()
        findViewById<TextInputLayout>(R.id.til_followup_id).error = null

        followUpShiftLabels.clear()
        followUpShiftValues.clear()
        followUpShiftLabels.add("No follow-up shift selected")
        followUpShiftValues.add(null)
        selectedFollowUpShift = null
        if (::followUpShiftAdapter.isInitialized) followUpShiftAdapter.notifyDataSetChanged()

        setSubmitting(false)
        fetchMainUserId()
    }

    private fun fetchMainUserId() {
        ApiService.getUsers(object : ApiCallback<List<User>> {
            override fun onSuccess(response: List<User>) {
                val mainUserId = response.firstOrNull()?.id
                runOnUiThread {
                    if (!mainUserId.isNullOrBlank()) {
                        findViewById<TextInputEditText>(R.id.et_followup_user_id).setText(mainUserId)
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

    private fun setSubmitting(loading: Boolean) {
        isSubmitting = loading
        val submitBtn = findViewById<MaterialButton>(R.id.btn_submit_followup)
        val scheduledBtn = findViewById<MaterialButton>(R.id.btn_submit_scheduled)
        submitBtn.isEnabled = !loading
        scheduledBtn.isEnabled = !loading
        submitBtn.text = if (loading) "Submitting…" else "Submit Follow Up"
        scheduledBtn.text = if (loading) "Submitting…" else "Submit Scheduled Follow Up"
    }

    private fun showStatus(message: String, isError: Boolean) {
        val tv = findViewById<TextView>(R.id.tv_followup_status)
        tv.visibility = View.VISIBLE
        tv.text = message
        tv.background = ContextCompat.getDrawable(this, if (isError) R.drawable.bg_feedback_error else R.drawable.bg_feedback_success)
        tv.setTextColor(ContextCompat.getColor(this, if (isError) R.color.error else R.color.success))
    }

    private fun hideStatus() {
        findViewById<TextView>(R.id.tv_followup_status).visibility = View.GONE
    }

    private fun setupShiftSpinner() {
        followUpShiftValues.add(null)
        val spinner = findViewById<Spinner>(R.id.spinnerFollowUpShift)
        followUpShiftAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, followUpShiftLabels)
        followUpShiftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = followUpShiftAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedFollowUpShift = followUpShiftValues.getOrNull(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { selectedFollowUpShift = null }
        }
    }

    private fun submitFollowUp() {
        val til = findViewById<TextInputLayout>(R.id.til_followup_id)
        val parentId = findViewById<TextInputEditText>(R.id.et_followup_id).text?.toString()?.trim()
        if (parentId.isNullOrBlank()) { til.error = "Required"; return }
        til.error = null

        val question = findViewById<TextInputEditText>(R.id.et_followup_question).text?.toString().orEmpty()
        val userId = findViewById<TextInputEditText>(R.id.et_followup_user_id).text?.toString()?.toIntOrNull() ?: 0

        if (question.length < 10 || userId == 0) {
            showStatus("Question too short or User ID missing.", true)
            return
        }

        hideStatus()
        setSubmitting(true)

        ApiService.createConsultation(
            question = question,
            medium = Medium.chat,
            userID = userId,
            mediaIDs = null,
            followUpId = parentId,
            scheduledTo = null,
            forceWhiteLabelingPartnerName = FORCE_WL_PARTNER,
            callback = object : ApiCallback<Consultation> {
                override fun onSuccess(response: Consultation) {
                    runOnUiThread {
                        setSubmitting(false)
                        if (response.status == "new" || response.status == "scheduled") {
                            startActivity(Intent(applicationContext, WaitingRoomActivity::class.java))
                        } else {
                            showStatus("Follow-up created (status: ${response.status}).", false)
                        }
                    }
                }
                override fun onFailure(error: String?) {
                    runOnUiThread {
                        setSubmitting(false)
                        showStatus(error ?: "Failed to create follow-up.", true)
                    }
                }
                override fun onRequestError(error: String?) = onFailure(error)
            }
        )
    }

    private fun submitScheduledFollowUp() {
        val til = findViewById<TextInputLayout>(R.id.til_followup_id)
        val parentId = findViewById<TextInputEditText>(R.id.et_followup_id).text?.toString()?.trim()
        if (parentId.isNullOrBlank()) { til.error = "Required"; return }
        til.error = null

        val userId = findViewById<TextInputEditText>(R.id.et_followup_user_id).text?.toString()?.toIntOrNull() ?: 0
        val question = findViewById<TextInputEditText>(R.id.et_followup_question).text?.toString().orEmpty()

        if (userId == 0) { showStatus("User ID missing.", true); return }
        if (question.length < 10) { showStatus("Question too short.", true); return }

        val shift = selectedFollowUpShift
        if (shift.isNullOrBlank()) {
            showStatus("No available shift. Tap Get Shifts first.", true)
            return
        }

        hideStatus()
        setSubmitting(true)

        ApiService.createConsultation(
            question = question,
            medium = Medium.chat,
            userID = userId,
            mediaIDs = null,
            followUpId = parentId,
            scheduledTo = shift,
            forceWhiteLabelingPartnerName = FORCE_WL_PARTNER,
            callback = object : ApiCallback<Consultation> {
                override fun onSuccess(response: Consultation) {
                    runOnUiThread {
                        setSubmitting(false)
                        showStatus("Scheduled follow-up created.", false)
                    }
                }
                override fun onFailure(error: String?) {
                    runOnUiThread {
                        setSubmitting(false)
                        showStatus(error ?: "Failed to schedule follow-up.", true)
                    }
                }
                override fun onRequestError(error: String?) = onFailure(error)
            }
        )
    }

    private fun fetchShiftsFromInputs() {
        val parentId = findViewById<TextInputEditText>(R.id.et_followup_id).text?.toString()?.trim()
        if (parentId.isNullOrBlank()) {
            showStatus("Enter Parent Consultation ID first.", true)
            return
        }
        val date = findViewById<TextInputEditText>(R.id.et_shift_date).text?.toString()?.trim()
            ?.takeIf { it.isNotEmpty() } ?: java.time.LocalDate.now().toString()

        ApiService.getConsultationAvailableShifts(parentId, date, object : ApiCallback<ConsultationAvailableShifts> {
            override fun onSuccess(response: ConsultationAvailableShifts) {
                val shifts = response.shifts
                runOnUiThread {
                    followUpShiftLabels.clear()
                    followUpShiftValues.clear()
                    if (shifts.isNotEmpty()) {
                        shifts.forEach {
                            followUpShiftLabels.add(it.displayText())
                            followUpShiftValues.add(it.shiftValue())
                        }
                        selectedFollowUpShift = shifts.first().shiftValue()
                    } else {
                        followUpShiftLabels.add("No shifts available for $date")
                        followUpShiftValues.add(null)
                        selectedFollowUpShift = null
                    }
                    followUpShiftAdapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread { showStatus("Failed to get shifts: $error", true) }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }
}

package com.altibbi.kotlinsdk.consultation

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.altibbi.kotlinsdk.R
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.PredictSpecialty
import com.altibbi.telehealth.model.PredictSummary
import com.altibbi.telehealth.model.Soap
import com.altibbi.telehealth.model.Transcription
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AiSupportActivity : AppCompatActivity() {

    private lateinit var consultationIdInput: TextInputEditText
    private lateinit var statusView: TextView
    private lateinit var transcriptionView: TextView
    private lateinit var soapView: TextView
    private lateinit var predictSummaryView: TextView
    private lateinit var predictSpecialtyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_support)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = "AI Support"

        consultationIdInput = findViewById(R.id.et_ai_consultation_id)
        statusView = findViewById(R.id.tv_ai_status)
        transcriptionView = findViewById(R.id.tv_transcription_result)
        soapView = findViewById(R.id.tv_soap_result)
        predictSummaryView = findViewById(R.id.tv_predict_summary_result)
        predictSpecialtyView = findViewById(R.id.tv_predict_specialty_result)

        findViewById<MaterialButton>(R.id.btn_fetch_all_ai).setOnClickListener { fetchAll() }
        findViewById<MaterialButton>(R.id.btn_fetch_transcription).setOnClickListener { fetchTranscription() }
        findViewById<MaterialButton>(R.id.btn_fetch_soap).setOnClickListener { fetchSoapSummary() }
        findViewById<MaterialButton>(R.id.btn_fetch_predict_summary).setOnClickListener { fetchPredictSummary() }
        findViewById<MaterialButton>(R.id.btn_fetch_predict_specialty).setOnClickListener { fetchPredictSpecialty() }

        clearResults()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requireConsultationId(): String? {
        val consultationId = consultationIdInput.text?.toString()?.trim()
        if (consultationId.isNullOrBlank()) {
            showStatus("Consultation ID is required.", true)
            return null
        }
        hideStatus()
        return consultationId
    }

    private fun fetchAll() {
        val consultationId = requireConsultationId() ?: return
        showStatus("Loading AI support data...", false)
        fetchTranscription(consultationId)
        fetchSoapSummary(consultationId)
        fetchPredictSummary(consultationId)
        fetchPredictSpecialty(consultationId)
    }

    private fun fetchTranscription() {
        val consultationId = requireConsultationId() ?: return
        fetchTranscription(consultationId)
    }

    private fun fetchSoapSummary() {
        val consultationId = requireConsultationId() ?: return
        fetchSoapSummary(consultationId)
    }

    private fun fetchPredictSummary() {
        val consultationId = requireConsultationId() ?: return
        fetchPredictSummary(consultationId)
    }

    private fun fetchPredictSpecialty() {
        val consultationId = requireConsultationId() ?: return
        fetchPredictSpecialty(consultationId)
    }

    private fun fetchTranscription(consultationId: String) {
        transcriptionView.text = "Loading..."
        ApiService.getTranscription(consultationId, object : ApiCallback<Transcription> {
            override fun onSuccess(response: Transcription) {
                runOnUiThread {
                    transcriptionView.text = response.transcript.ifBlank { "No transcription returned." }
                    showStatus("Transcription loaded.", false)
                }
            }

            override fun onFailure(error: String?) {
                showSectionError(transcriptionView, error ?: "Failed to fetch transcription.")
            }

            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun fetchSoapSummary(consultationId: String) {
        soapView.text = "Loading..."
        ApiService.getSoapSummary(consultationId, object : ApiCallback<Soap> {
            override fun onSuccess(response: Soap) {
                runOnUiThread {
                    soapView.text = formatSoap(response)
                    showStatus("SOAP summary loaded.", false)
                }
            }

            override fun onFailure(error: String?) {
                showSectionError(soapView, error ?: "Failed to fetch SOAP summary.")
            }

            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun fetchPredictSummary(consultationId: String) {
        predictSummaryView.text = "Loading..."
        ApiService.getPredictSummary(consultationId, object : ApiCallback<PredictSummary> {
            override fun onSuccess(response: PredictSummary) {
                runOnUiThread {
                    predictSummaryView.text = response.summary.ifBlank { "No predicted summary returned." }
                    showStatus("Predict summary loaded.", false)
                }
            }

            override fun onFailure(error: String?) {
                showSectionError(predictSummaryView, error ?: "Failed to fetch predict summary.")
            }

            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun fetchPredictSpecialty(consultationId: String) {
        predictSpecialtyView.text = "Loading..."
        ApiService.getPredictSpecialty(consultationId, object : ApiCallback<List<PredictSpecialty>> {
            override fun onSuccess(response: List<PredictSpecialty>) {
                runOnUiThread {
                    predictSpecialtyView.text = formatPredictSpecialties(response)
                    showStatus("Predicted specialties loaded.", false)
                }
            }

            override fun onFailure(error: String?) {
                showSectionError(predictSpecialtyView, error ?: "Failed to fetch predicted specialties.")
            }

            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun formatSoap(response: Soap): String {
        val summary = response.summary
        return buildString {
            appendLine("Subjective")
            appendLine("Symptoms: ${summary.subjective.symptoms.orPlaceholder()}")
            appendLine("Concerns: ${summary.subjective.concerns.orPlaceholder()}")
            appendLine()
            appendLine("Objective")
            appendLine("Laboratory Results: ${summary.objective.laboratoryResults.orPlaceholder()}")
            appendLine("Physical Examination Findings: ${summary.objective.physicalExaminationFindings.orPlaceholder()}")
            appendLine()
            appendLine("Assessment")
            appendLine("Diagnosis: ${summary.assessment.diagnosis.orPlaceholder()}")
            appendLine("Differential Diagnosis: ${summary.assessment.differentialDiagnosis.orPlaceholder()}")
            appendLine()
            appendLine("Plan")
            appendLine("Non-Pharmacological Intervention: ${summary.plan.nonPharmacologicalIntervention.orPlaceholder()}")
            appendLine("Medications: ${summary.plan.medications.orPlaceholder()}")
            appendLine("Referrals: ${summary.plan.referrals.orPlaceholder()}")
            append("Follow-Up Instructions: ${summary.plan.followUpInstructions.orPlaceholder()}")
        }
    }

    private fun formatPredictSpecialties(items: List<PredictSpecialty>): String {
        if (items.isEmpty()) return "No specialties returned."
        return items.joinToString("\n\n") { specialty ->
            val subCategories = if (specialty.subCategories.isEmpty()) {
                "None"
            } else {
                specialty.subCategories.joinToString("\n") { subCategory ->
                    "- ${subCategory.subCategoryId}: ${subCategory.nameEn} / ${subCategory.nameAr}"
                }
            }
            "Specialty ID: ${specialty.specialtyId}\nSubcategories:\n$subCategories"
        }
    }

    private fun clearResults() {
        transcriptionView.text = "No transcription loaded yet."
        soapView.text = "No SOAP summary loaded yet."
        predictSummaryView.text = "No predict summary loaded yet."
        predictSpecialtyView.text = "No predicted specialties loaded yet."
        hideStatus()
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

    private fun showSectionError(view: TextView, message: String) {
        runOnUiThread {
            view.text = message
            showStatus(message, true)
        }
    }

    private fun String?.orPlaceholder(): String = this?.takeIf { it.isNotBlank() } ?: "N/A"
}

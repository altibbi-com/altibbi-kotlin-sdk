package com.altibbi.kotlinsdk.consultation

import com.altibbi.kotlinsdk.R
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Consultation
import com.altibbi.telehealth.model.RecommendationData
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class ConsultationDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONSULTATION_ID = "consultation_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation_details)

        val toolbar = findViewById<MaterialToolbar>(R.id.details_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val consultationId = intent.getIntExtra(EXTRA_CONSULTATION_ID, -1)
        if (consultationId == -1) { finish(); return }

        toolbar.title = "Consultation Details"
        toolbar.subtitle = "ID: $consultationId"

        loadConsultation(consultationId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun loadConsultation(id: Int) {
        ApiService.getConsultationInfo(id.toString(), object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                runOnUiThread { bindConsultation(response) }
            }

            override fun onFailure(error: String?) {
                runOnUiThread {
                    findViewById<ProgressBar>(R.id.details_progress).visibility = View.GONE
                    Toast.makeText(
                        this@ConsultationDetailsActivity,
                        error ?: "Failed to load consultation",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun bindConsultation(data: Consultation) {
        val statusTv = findViewById<TextView>(R.id.detail_status)
        statusTv.text = data.status?.uppercase(Locale.getDefault()) ?: "N/A"
        statusTv.setTextColor(statusColor(data.status.orEmpty()))

        setText(R.id.detail_medium, data.medium?.uppercase(Locale.getDefault()))
        setText(R.id.detail_created_at, data.createdAt)
        setText(R.id.detail_closed_at, null)
        setText(R.id.detail_question, data.question)

        data.user?.let { user ->
            findViewById<MaterialCardView>(R.id.card_user_info).visibility = View.VISIBLE
            setText(R.id.detail_user_name, user.name)
            setText(R.id.detail_user_phone, user.phoneNumber)
            setText(R.id.detail_user_gender, user.gender?.name)
            setText(R.id.detail_user_dob, user.dateOfBirth)
            setText(R.id.detail_user_insurance, user.insuranceId)
            setText(R.id.detail_user_policy, user.policyNumber)
        }

        data.recommendation?.data?.let { recData ->
            val card = findViewById<MaterialCardView>(R.id.card_recommendation)
            val container = findViewById<LinearLayout>(R.id.rec_container)
            card.visibility = View.VISIBLE
            buildRecommendation(container, recData)
        }

        findViewById<ProgressBar>(R.id.details_progress).visibility = View.GONE
        findViewById<NestedScrollView>(R.id.details_scroll).visibility = View.VISIBLE
    }

    private fun buildRecommendation(container: LinearLayout, data: RecommendationData) {
        val icd10 = data.icd10
        if ((icd10?.diagnosis?.isNotEmpty() == true) || (icd10?.symptom?.isNotEmpty() == true)) {
            addSubSectionTitle(container, "Diagnosis & Symptoms")
            icd10.diagnosis?.forEach { d -> addDetailRow(container, "Diagnosis", "${d.name} (${d.code})") }
            icd10.symptom?.forEach { s -> addDetailRow(container, "Symptom", "${s.name} (${s.code})") }
            addSubSectionDivider(container)
        }

        val drugs = data.drug?.fdaDrug
        if (!drugs.isNullOrEmpty()) {
            addSubSectionTitle(container, "Medications")
            drugs.forEach { d ->
                container.addView(buildDrugBlock(
                    name = d.name, tradeName = d.tradeName,
                    dosage = d.dosage, frequency = d.frequency,
                    duration = d.duration?.toString(), route = d.routeOfAdministration,
                    howToUse = d.howToUse, foodRelation = d.relationWithFood,
                    instructions = d.specialInstructions
                ))
            }
            addSubSectionDivider(container)
        }

        val labs = data.lab
        if ((labs?.lab?.isNotEmpty() == true) || (labs?.panel?.isNotEmpty() == true)) {
            addSubSectionTitle(container, "Laboratory Tests")
            labs.lab?.forEach { l -> addDetailRow(container, "Lab Test", l.name) }
            labs.panel?.forEach { p -> addDetailRow(container, "Panel", p.name) }
            addSubSectionDivider(container)
        }

        val followUp = data.followUp
        if (!followUp.isNullOrEmpty()) {
            addSubSectionTitle(container, "Follow Up")
            followUp.forEach { f -> addDetailRow(container, "Instruction", f.name) }
            addSubSectionDivider(container)
        }

        data.doctorReferral?.let { ref ->
            addSubSectionTitle(container, "Referral")
            addDetailRow(container, "Specialist", ref.name)
        }
    }

    private fun addSubSectionTitle(parent: LinearLayout, title: String) {
        val dp = resources.displayMetrics.density
        parent.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (12 * dp).toInt() }
        })
    }

    private fun addSubSectionDivider(parent: LinearLayout) {
        val dp = resources.displayMetrics.density
        parent.addView(View(this).apply {
            setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            ).also { it.topMargin = (4 * dp).toInt(); it.bottomMargin = (20 * dp).toInt() }
        })
    }

    private fun addDetailRow(parent: LinearLayout, label: String, value: String?) {
        val dp = resources.displayMetrics.density
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (12 * dp).toInt() }
        }
        row.addView(TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.gray))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        row.addView(TextView(this).apply {
            text = value ?: "N/A"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        parent.addView(row)
    }

    private fun buildDrugBlock(
        name: String?, tradeName: String?,
        dosage: String?, frequency: String?,
        duration: String?, route: String?,
        howToUse: String?, foodRelation: String?, instructions: String?
    ): LinearLayout {
        val dp = resources.displayMetrics.density
        val block = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.background))
            setPadding((12 * dp).toInt(), (12 * dp).toInt(), (12 * dp).toInt(), (12 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (16 * dp).toInt() }
        }
        block.addView(TextView(this).apply {
            text = if (tradeName != null) "$name ($tradeName)" else name ?: "—"
            textSize = 15f
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (12 * dp).toInt() }
        })
        block.addView(buildGridRow(Pair("Dosage", dosage), Pair("Frequency", frequency)))
        block.addView(buildGridRow(
            Pair("Duration", if (duration != null) "$duration days" else null),
            Pair("Route", route)
        ))
        if (!howToUse.isNullOrBlank()) addSmallTextRow(block, "How to use:", howToUse)
        if (!foodRelation.isNullOrBlank()) addSmallTextRow(block, "Food Relation:", foodRelation)
        if (!instructions.isNullOrBlank()) addSmallTextRow(block, "Instructions:", instructions)
        return block
    }

    private fun buildGridRow(vararg cells: Pair<String, String?>): LinearLayout {
        val dp = resources.displayMetrics.density
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (8 * dp).toInt() }
        }
        cells.forEach { (label, value) ->
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            cell.addView(TextView(this).apply {
                text = label.uppercase(Locale.getDefault())
                textSize = 11f
                setTextColor(ContextCompat.getColor(context, R.color.gray))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            cell.addView(TextView(this).apply {
                text = value ?: "—"
                textSize = 13f
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            row.addView(cell)
        }
        return row
    }

    private fun addSmallTextRow(parent: LinearLayout, label: String, value: String) {
        val dp = resources.displayMetrics.density
        parent.addView(TextView(this).apply {
            text = "$label $value"
            textSize = 13f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = (4 * dp).toInt() }
        })
    }

    private fun setText(viewId: Int, value: String?) {
        findViewById<TextView>(viewId).text = value?.takeIf { it.isNotBlank() } ?: "N/A"
    }

    private fun statusColor(status: String): Int = ContextCompat.getColor(
        this,
        when (status.lowercase(Locale.getDefault())) {
            "closed" -> R.color.gray
            else -> R.color.text_primary
        }
    )
}

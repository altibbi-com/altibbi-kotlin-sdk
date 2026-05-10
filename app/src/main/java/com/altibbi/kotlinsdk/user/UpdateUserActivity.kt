package com.altibbi.kotlinsdk.user

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.altibbi.kotlinsdk.R
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Gender
import com.altibbi.telehealth.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class UpdateUserActivity : AppCompatActivity() {

    private var isSubmitting = false
    private var loadedUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = "Update User"

        findViewById<View>(R.id.btn_load).setOnClickListener { loadUser() }
        findViewById<View>(R.id.btn_submit).setOnClickListener {
            if (!isSubmitting) submitUpdate()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun loadUser() {
        val id = et(R.id.et_user_id)
        val til = til(R.id.til_user_id)
        if (id.isBlank()) { til.error = "Required"; return }
        til.error = null

        val loadBtn = findViewById<MaterialButton>(R.id.btn_load)
        loadBtn.isEnabled = false
        loadBtn.text = "Loading…"
        hideStatus()
        hideForm()

        ApiService.getUser(id, object : ApiCallback<User> {
            override fun onSuccess(response: User) {
                runOnUiThread {
                    loadBtn.isEnabled = true
                    loadBtn.text = "Load"
                    loadedUserId = response.id ?: id
                    populateForm(response)
                    showForm()
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread {
                    loadBtn.isEnabled = true
                    loadBtn.text = "Load"
                    showStatus(error ?: "User not found.", true)
                }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun populateForm(user: User) {
        setEt(R.id.et_name, user.name)
        setEt(R.id.et_phone, user.phoneNumber)
        setEt(R.id.et_email, user.email)
        setEt(R.id.et_dob, user.dateOfBirth)
        setEt(R.id.et_nationality, user.nationalityNumber)
        setEt(R.id.et_insurance, user.insuranceId)
        setEt(R.id.et_policy, user.policyNumber)
        setEt(R.id.et_tpa_code, user.tpaCode)
        setEt(R.id.et_payer_name, user.payerName)

        val rgGender = findViewById<RadioGroup>(R.id.rg_gender)
        if (user.gender == Gender.FEMALE) rgGender.check(R.id.rb_female)
        else rgGender.check(R.id.rb_male)
    }

    private fun submitUpdate() {
        clearErrors()

        val name = et(R.id.et_name)
        val phone = et(R.id.et_phone)
        val email = et(R.id.et_email)
        val dob = et(R.id.et_dob)
        val insurance = et(R.id.et_insurance)
        val policy = et(R.id.et_policy)
        val nationality = et(R.id.et_nationality)
        val tpaCode = et(R.id.et_tpa_code)
        val payerName = et(R.id.et_payer_name)
        val userId = loadedUserId ?: return

        var valid = true
        if (name.isBlank()) { til(R.id.til_name).error = "Required"; valid = false }
        if (phone.isBlank()) { til(R.id.til_phone).error = "Required"; valid = false }
        if (email.isBlank()) { til(R.id.til_email).error = "Required"; valid = false }
        if (dob.isBlank()) { til(R.id.til_dob).error = "Required (yyyy-MM-dd)"; valid = false }
        if (insurance.isBlank()) { til(R.id.til_insurance).error = "Required"; valid = false }
        if (policy.isBlank()) { til(R.id.til_policy).error = "Required"; valid = false }
        if (!valid) return

        val gender = when (findViewById<RadioGroup>(R.id.rg_gender).checkedRadioButtonId) {
            R.id.rb_female -> Gender.FEMALE
            else -> Gender.MALE
        }

        setSubmitting(true)
        hideStatus()

        val user = User(
            id = userId,
            name = name,
            phoneNumber = phone,
            email = email,
            dateOfBirth = dob,
            gender = gender,
            insuranceId = insurance,
            policyNumber = policy,
            nationalityNumber = nationality.takeIf { it.isNotBlank() },
            tpaCode = tpaCode.takeIf { it.isNotBlank() },
            payerName = payerName.takeIf { it.isNotBlank() }
        )

        ApiService.updateUser(user, userId, object : ApiCallback<User> {
            override fun onSuccess(response: User) {
                runOnUiThread {
                    setSubmitting(false)
                    showStatus("User updated — ${response.name}", false)
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread {
                    setSubmitting(false)
                    showStatus(error ?: "Failed to update user.", true)
                }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun showForm() {
        findViewById<View>(R.id.layout_form).visibility = View.VISIBLE
    }

    private fun hideForm() {
        findViewById<View>(R.id.layout_form).visibility = View.GONE
    }

    private fun clearErrors() {
        listOf(R.id.til_name, R.id.til_phone, R.id.til_email, R.id.til_dob,
            R.id.til_insurance, R.id.til_policy)
            .forEach { til(it).error = null }
    }

    private fun setSubmitting(loading: Boolean) {
        isSubmitting = loading
        val btn = findViewById<MaterialButton>(R.id.btn_submit)
        btn.isEnabled = !loading
        btn.text = if (loading) "Updating…" else "Update User"
    }

    private fun showStatus(message: String, isError: Boolean) {
        val tv = findViewById<TextView>(R.id.tv_status)
        tv.visibility = View.VISIBLE
        tv.text = message
        tv.background = ContextCompat.getDrawable(this, if (isError) R.drawable.bg_feedback_error else R.drawable.bg_feedback_success)
        tv.setTextColor(ContextCompat.getColor(this, if (isError) R.color.error else R.color.success))
    }

    private fun hideStatus() {
        findViewById<TextView>(R.id.tv_status).visibility = View.GONE
    }

    private fun et(id: Int) = findViewById<TextInputEditText>(id).text?.toString().orEmpty().trim()
    private fun setEt(id: Int, value: String?) = findViewById<TextInputEditText>(id).setText(value.orEmpty())
    private fun til(id: Int) = findViewById<TextInputLayout>(id)
}

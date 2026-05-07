package com.altibbi.kotlinsdk.user

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.altibbi.kotlinsdk.R
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class UserToolsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_tools)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = "User Tools"

        findViewById<View>(R.id.btn_get_user).setOnClickListener { getUser() }
        findViewById<View>(R.id.btn_delete_user).setOnClickListener { deleteUser() }
        findViewById<View>(R.id.btn_get_all).setOnClickListener { getAllUsers() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun getUser() {
        val id = et(R.id.et_get_id)
        if (id.isBlank()) { showStatus("Enter a User ID.", true); return }
        hideStatus()
        hideUserDetail()

        ApiService.getUser(id, object : ApiCallback<User> {
            override fun onSuccess(response: User) {
                runOnUiThread { showUserDetail(response) }
            }
            override fun onFailure(error: String?) {
                runOnUiThread { showStatus(error ?: "User not found.", true) }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun showUserDetail(user: User) {
        val layout = findViewById<View>(R.id.layout_user_detail)
        layout.visibility = View.VISIBLE
        setText(R.id.tv_user_name, user.name ?: "N/A")
        setText(R.id.tv_user_phone, "Phone: ${user.phoneNumber ?: "N/A"}")
        setText(R.id.tv_user_email, "Email: ${user.email ?: "N/A"}")
        setText(R.id.tv_user_dob, "DOB: ${user.dateOfBirth ?: "N/A"}")
        setText(R.id.tv_user_insurance, "Insurance: ${user.insuranceId ?: "N/A"}  |  Policy: ${user.policyNumber ?: "N/A"}")
    }

    private fun hideUserDetail() {
        findViewById<View>(R.id.layout_user_detail).visibility = View.GONE
    }

    private fun deleteUser() {
        val id = et(R.id.et_delete_id)
        if (id.isBlank()) { showStatus("Enter a User ID to delete.", true); return }
        hideStatus()

        ApiService.deleteUser(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) {
                runOnUiThread {
                    showStatus("User $id deleted successfully.", false)
                    findViewById<TextInputEditText>(R.id.et_delete_id).setText("")
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread { showStatus(error ?: "Failed to delete user.", true) }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun getAllUsers() {
        val countTv = findViewById<TextView>(R.id.tv_users_count)
        countTv.visibility = View.GONE
        hideStatus()

        ApiService.getUsers(object : ApiCallback<List<User>> {
            override fun onSuccess(response: List<User>) {
                runOnUiThread {
                    countTv.visibility = View.VISIBLE
                    countTv.text = "${response.size} user(s) found"
                    if (response.isNotEmpty()) {
                        showStatus("First user — ID: ${response[0].id}, Name: ${response[0].name}", false)
                    }
                }
            }
            override fun onFailure(error: String?) {
                runOnUiThread { showStatus(error ?: "Failed to fetch users.", true) }
            }
            override fun onRequestError(error: String?) = onFailure(error)
        })
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
    private fun setText(id: Int, value: String) = findViewById<TextView>(id).let { it.text = value }
}

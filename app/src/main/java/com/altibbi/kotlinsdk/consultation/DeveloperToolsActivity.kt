package com.altibbi.kotlinsdk.consultation

import com.altibbi.kotlinsdk.R
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class DeveloperToolsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_tools)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = "Developer Tools"

        findViewById<View>(R.id.btn_cancel).setOnClickListener { cancelConsultation() }
        findViewById<View>(R.id.btn_delete).setOnClickListener { deleteConsultation() }
        findViewById<View>(R.id.btn_prescription).setOnClickListener { getPrescription() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun cancelConsultation() {
        val id = findViewById<TextInputEditText>(R.id.et_cancel_id).text?.toString() ?: return
        ApiService.cancelConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) = println("cancelConsultation: $response")
            override fun onFailure(error: String?) = println("cancelConsultation error: $error")
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun deleteConsultation() {
        val id = findViewById<TextInputEditText>(R.id.et_delete_id).text?.toString() ?: return
        ApiService.deleteConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) = println("deleteConsultation: $response")
            override fun onFailure(error: String?) = println("deleteConsultation error: $error")
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun getPrescription() {
        val id = findViewById<TextInputEditText>(R.id.et_prescription_id).text?.toString() ?: return
        ApiService.getPrescription(id, object : ApiCallback<Response> {
            override fun onSuccess(response: Response) {
                response.body?.byteStream()?.let { savePdfToFile(it, "prescription_$id.pdf") }
            }
            override fun onFailure(error: String?) = println("getPrescription error: $error")
            override fun onRequestError(error: String?) = onFailure(error)
        })
    }

    private fun savePdfToFile(inputStream: InputStream, fileName: String): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, fileName)
        try {
            FileOutputStream(file).use { out ->
                val buf = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buf).also { read = it } != -1) out.write(buf, 0, read)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream.close()
        }
        return file
    }
}

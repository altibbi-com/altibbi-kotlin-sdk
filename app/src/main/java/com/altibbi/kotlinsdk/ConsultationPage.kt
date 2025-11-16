package com.altibbi.kotlinsdk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.model.Media
import com.altibbi.telehealth.model.Medium
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.model.Consultation
import com.altibbi.telehealth.TBISocket
import com.altibbi.telehealth.model.PredictSpecialty
import com.altibbi.telehealth.model.PredictSummary
import com.altibbi.telehealth.model.Soap
import com.altibbi.telehealth.model.Transcription
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream


class ConsultationPage : AppCompatActivity() {
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation_page)
        val spinner = findViewById<Spinner>(R.id.spinner1)
        println("333333333323")
        val values = listOf("chat", "gsm", "video", "voip")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        galleryActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleGalleryResult(result.resultCode, result.data)
        }
        val tbiSocket = TBISocket()

        val createConsultationButton = findViewById<Button>(R.id.button2);

        val uploadImageButton = findViewById<Button>(R.id.button13);
        uploadImageButton.setOnClickListener{
            showImagePicker()
        }

        createConsultationButton.setOnClickListener {
            createConsultationFun(tbiSocket, applicationContext)
        }

        val cancelButton = findViewById<Button>(R.id.button5);
        cancelButton.setOnClickListener{
            cancelConsultationFun()
        }

        val getConsultationByIdButton = findViewById<Button>(R.id.button7);
        getConsultationByIdButton.setOnClickListener{
            getConsultation()
        }

        val getConsultationListButton = findViewById<Button>(R.id.button8);
        getConsultationListButton.setOnClickListener{
            getConsultationListFun()
        }

        val deleteConsultationByIdButton = findViewById<Button>(R.id.button9);
        deleteConsultationByIdButton.setOnClickListener{
            deleteConsultationFun()
        }

        val getPrescriptionButton = findViewById<Button>(R.id.button14);
        getPrescriptionButton.setOnClickListener{
            getPrescriptionFun(applicationContext)
        }

        val getLastConsultationButton = findViewById<Button>(R.id.button18)
        getLastConsultationButton.setOnClickListener {
            getLastConsultation()
        }
        val getAiButton = findViewById<Button>(R.id.button19)
        getAiButton.setOnClickListener {
            println("$!@#123123123")
            getAiSupport()
        }
    }

    private fun getLastConsultation(){
        ApiService.getLastConsultation(object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                println("getLastConsultation response: $response")
                if (response.status == "new" || response.status == "in_progress"){
                    val intent = Intent(applicationContext, WaitingRoom::class.java)
                    startActivity(intent)
                }
            }

            override fun onFailure(error: String?) {
                println("$error")
            }

            override fun onRequestError(error: String?) {
                println("$error")
            }


        })
    }

    private fun getPrescriptionFun( context: Context) {
        val consultationToGet: EditText = findViewById(R.id.textInputEditText15)
        val id: String = consultationToGet.text.toString()

        ApiService.getPrescription(id, object : ApiCallback<Response> {
            override fun onSuccess(response: Response) {
                val inputStream = response.body?.byteStream()
                if (inputStream != null) {
                    savePdfToFile(inputStream, "newPDF_231.pdf")
                }
            }
            override fun onFailure(error: String?) {
                println("onFailure error: $error")
            }
            override fun onRequestError(error: String?) {
            }
        })
    }

    private fun savePdfToFile(inputStream: InputStream, fileName: String): File {
        val downloadDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val downloadPath = downloadDirectory.absolutePath
        val outputFile = File(downloadPath, fileName)
        try {
            if (!outputFile.exists()) {
                outputFile.createNewFile()
            }
            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(4 * 1024) // Adjust buffer size as needed
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream.close()
        }
        return outputFile
    }


    private fun uriToFile(uri: Uri): File? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(filePathColumn[0])
            val filePath = it.getString(columnIndex)
            return File(filePath)
        }
        return null
    }

    private fun handleGalleryResult(resultCode: Int, data: Intent?) {
        val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
        Log.d("ImagePicker", "handleGalleryResult called")
        if (resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data

            if (imageUri != null) {
                val imageFile: File? = uriToFile(imageUri)

                if (imageFile != null && imageFile.exists()) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ){

                        ApiService.uploadMedia(imageFile, object : ApiCallback<Media> {
                            override fun onSuccess(response: Media) {
                                println("uploadMedia onSuccess : $response")
                            }
                            override fun onFailure(error: String?) {
                                println(error)
                            }
                            override fun onRequestError(error: String?) {
                                println(error)
                            }
                        })
                    } else{
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                        )
                    }
                }
            }
        }
    }


    private fun showImagePicker(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher?.launch(galleryIntent)
    }

    private fun getConsultationListFun() {
        val consultationCallback = object : ApiCallback<List<Consultation>> {
            override fun onSuccess(response: List<Consultation>) {
                println("Consultation.size ${response.size}")
            }

            override fun onFailure(error: String?) {
                println(error)
            }

            override fun onRequestError(error: String?) {
                println(error)
            }


        }
        ApiService.getConsultationList(callback = consultationCallback)
    }


    private fun getConsultation() {
        val consultationToGet: EditText = findViewById(R.id.textInputEditText4)
        val id: String = consultationToGet.text.toString()
        ApiService.getConsultationInfo(id, object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                println("get consultation info response is -> $response")
            }

            override fun onFailure(error: String?) {
                println(error)
            }

            override fun onRequestError(error: String?) {
                println(error)
            }
        })
    }


    private fun getAiSupport() {
        ApiService.getTranscription("146", object : ApiCallback<Transcription> {
            override fun onSuccess(response: Transcription) {
              println(response.transcript)
            }

            override fun onFailure(error: String?) {
                println(error)
            }

            override fun onRequestError(error: String?) {
                println("2$error")
            }

        })
        ApiService.getSoapSummary("147", object : ApiCallback<Soap> {
            override fun onSuccess(response: Soap) {
                println(" getSoapSummary ${response.summary.objective.laboratoryResults}")
            }

            override fun onFailure(error: String?) {
                println("4$error")
            }

            override fun onRequestError(error: String?) {
                println("3$error")
            }

        })
        ApiService.getPredictSummary("148", object : ApiCallback<PredictSummary> {
            override fun onSuccess(response: PredictSummary) {
                println("getPredictSummary ${ response.summary }")
            }

            override fun onFailure(error: String?) {
                println("11$error")
            }

            override fun onRequestError(error: String?) {
               println("33$error")
            }

        })
        ApiService.getPredictSpecialty("149", object : ApiCallback<List<PredictSpecialty>> {
            override fun onSuccess(response: List<PredictSpecialty>) {
                println("getPredictSpecialty ${response[0].specialtyId}")
            }

            override fun onFailure(error: String?) {
                println("122$error")
            }

            override fun onRequestError(error: String?) {
                println("2231$error")
            }
        })
    }


    private fun cancelConsultationFun() {
        val consultationId: EditText = findViewById(R.id.textInputEditText3)
        val id: String = consultationId.text.toString()

        ApiService.cancelConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) {
                println("cancelConsultation response: $response")
            }

            override fun onFailure(error: String?) {
                println(error)
            }

            override fun onRequestError(error: String?) {
                println(error)
            }
        })
    }


    private fun createConsultationFun(tbiSocket: TBISocket, context: Context) {
        val spinner = findViewById<Spinner>(R.id.spinner1)
        val values = listOf("chat", "call", "video")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        val textInputEditText: EditText = findViewById(R.id.textInputEditText)
        val parentConsId: EditText = findViewById(R.id.textInputEditText6)

//        mediaIDs = arrayOf("c8617c16-98ef-11ee-9bc6-9600009a97a9"),

        ApiService.createConsultation(
            question = textInputEditText.text.toString(),
            medium = Medium.chat,
            userID = 2,
            object : ApiCallback<Consultation> {
                override fun onSuccess(response: Consultation) {
                    println("createConsultation response is -> $response")
                    if(response.status == "new"){
                        val intent = Intent(applicationContext, WaitingRoom::class.java)
                        startActivity(intent)
                    }
                }

                override fun onFailure(error: String?) {
                    println(error)
                }

                override fun onRequestError(error: String?) {
                    println(error)
                }
            }
                    )
    }

    private fun deleteConsultationFun() {
        val deleteConsId: EditText = findViewById(R.id.textInputEditText5)
        val id: String = deleteConsId.text.toString()

        ApiService.deleteConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) {
                println("deleteConsultation response: $response")
            }

            override fun onFailure(error: String?) {
                println(error)
            }

            override fun onRequestError(error: String?) {
                println(error)
            }
        })
    }

    private fun rateConsultation() {
        val deleteConsId: EditText = findViewById(R.id.textInputEditText5)
        val id: String = deleteConsId.text.toString()

        ApiService.rateConsultation(id, 4.0, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) {
                println("rated consultation successfully: $response")
            }

            override fun onFailure(error: String?) {
                println(error)
            }

            override fun onRequestError(error: String?) {
                println(error)
            }
        })
    }


    fun attachAsCSV(jsonData: List<Map<String, String>>, callback: ApiCallback<Media>) {
        try {
            val fileName = "attach-consultation-${System.currentTimeMillis()}.csv"
            val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsPath, fileName)

            FileWriter(file).use { writer ->
                if (jsonData.isNotEmpty()) {
                    writer.append(jsonData.first().keys.joinToString(",")).append("\n") // Add headers
                    jsonData.forEach { row ->
                        writer.append(row.values.joinToString(",")).append("\n") // Add rows
                    }
                }
            }

            ApiService.uploadMedia(file, object : ApiCallback<Media> {
                override fun onSuccess(response: Media) {
                    println("uploadMedia onSuccess : $response")
                }
                override fun onFailure(error: String?) {
                    println(error)
                }
                override fun onRequestError(error: String?) {
                    println(error)
                }
            })

        } catch (e: IOException) {
            e.printStackTrace()
            callback.onFailure("Failed to attach CSV: ${e.message}")
        }
    }
}
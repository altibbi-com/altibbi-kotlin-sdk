package com.example.kotlinsdk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.example.altibbi.ApiService
import com.example.altibbi.Consultation
import java.io.File
import com.example.altibbi.TBISocket


class ConsultationPage : AppCompatActivity() {
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation_page)
        val spinner = findViewById<Spinner>(R.id.spinner1)

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
            createConsultationFun(tbiSocket)
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
    }

    private fun getPrescriptionFun( context: Context) {
        println("context is -> ${context.packageName}")
        val consultationToGet: EditText = findViewById(R.id.textInputEditText15)
        val id: String = consultationToGet.text.toString()

        ApiService.getPrescription(id, context, object : Consultation.DownloadPrescriptionCallback {
            override fun onSuccess(filePath: String) {
                println("filePath in onSuccess -> $filePath")
            }

            override fun onError(errorMessage: String) {
                println("errorMessage is -> $errorMessage")
            }
        })
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
            println("imageUri is -> $imageUri")

            if (imageUri != null) {
                val imageFile: File? = uriToFile(imageUri)

                if (imageFile != null && imageFile.exists()) {
                    println("imageFile is $imageFile")

                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ){
                        ApiService.uploadMedia(imageFile, object :
                            Consultation.UploadCallback {
                            override fun onSuccess(response: Consultation.UploadMediaResponse) {
                                println("uploadMedia response is -> $response")
                                if(response is Consultation.UploadMediaResponse){
                                    println("uploadMedia response all data is is -> $response")
                                }
                            }

                            override fun onError(error: Any) {
                                println("uploadMedia error Any is -> $error")
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
        ApiService.getConsultationList(object : Consultation.GetConsultationListCallBack{
            override fun onSuccess(response: List<Consultation.ConsultationResponse>) {
                println("getConsultationList all data is  -> $response")
            }

            override fun onError(error: Any) {
                println("getConsultationList error -> $error")
            }
        })
    }


    private fun getConsultation() {
        val consultationToGet: EditText = findViewById(R.id.textInputEditText4)
        val id: String = consultationToGet.text.toString()

        ApiService.getConsultation(id, object : Consultation.GetConsultationByIdCallBack{
            override fun onSuccess(response: Consultation.GetConsultationByIdResponse) {
                if(response is Consultation.GetConsultationByIdResponse){
                    println("GetConsultationByIdResponse all data is -> $response")
                }
            }

            override fun onError(error: Any) {
                println("error is in GetConsultationByIdNotFoundResponse -> $error")
            }

            override fun onErrorObj(error: Consultation.ConsultationNotFound) {
                if(error is Consultation.ConsultationNotFound){
                    println("error is in GetConsultationByIdNotFoundResponse 123 -> $error")
                }
            }
        })

    }


    private fun cancelConsultationFun() {
        val consultationId: EditText = findViewById(R.id.textInputEditText3)
        val id: String = consultationId.text.toString()

        ApiService.cancelConsultation(
            id,
            object : Consultation.CancelConsultationCallBack{
                override fun onSuccess(response: Consultation.CancelConsultationResponse){
                    println("Cancel Consultation Response not all data -> $response")
                    if(response is Consultation.CancelConsultationResponse){
                        println("Cancel Consultation Response all data is -> $response")
                    }
                }
                override fun onError(error: Any ) {
                    println("Received Error Any in callback cancelConsultationFun: $error")
                }

                override fun onErrorObj(error: Consultation.ConsultationNotFound){
                    if (error is Consultation.ConsultationNotFound){
                        println("error all data in onErrorObj is -> $error")
                    }
                }
            }
        )
    }


    private fun createConsultationFun(tbiSocket: TBISocket) {
        val spinner = findViewById<Spinner>(R.id.spinner1)
        val values = listOf("chat", "call", "video")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val textInputEditText: EditText = findViewById(R.id.textInputEditText)
        val parentConsId: EditText = findViewById(R.id.textInputEditText6)
        val parentConsultationId = parentConsId.text.toString().toIntOrNull()

        val consultationParams = Consultation.ConsultationData(
            question = textInputEditText.text.toString(),
            medium = (spinner.selectedItem as String),
            userID = 64,
            mediaIDs = arrayOf("c8617c16-98ef-11ee-9bc6-9600009a97a9"),
            followUpId = parentConsultationId
        )
        ApiService.createConsultation(
            consultationParams,
            object : Consultation.CreateConsultationCallback {
                override fun onSuccess(response: Consultation.ConsultationResponse) {
                    println("Received response in callback createConsultation: $response")
                    if(response is Consultation.ConsultationResponse){
                        println("Received response in callback createConsultation all data is :${response}")

                        val pusherData = TBISocket.PusherParams(
                            pusherAppKey = response.pusherAppKey,
                            pusherChannel = response.pusherChannel
                        )
                        tbiSocket.initiateSocket(pusherData, object : TBISocket.InitiateSocketCallBack{
                            override fun onConnect(status: String) {
                                println("onConnect status -> $status")
                            }
                        })
                    }
                }
                override fun onError(error: Any) {
                    println("Received Error in callback createConsultation: $error")
                }
            }
        )
    }

    private fun deleteConsultationFun() {
        val deleteConsId: EditText = findViewById(R.id.textInputEditText5)
        val id: String = deleteConsId.text.toString()
        ApiService.deleteConsultation(id, object : Consultation.DeleteConsultationCallBack{
            override fun onSuccess(response: Any) {
                println("deleteConsultation onSuccess response is -> $response")
            }

            override fun onError(error: Any) {
                println("deleteConsultation onError response is -> $error")
            }
        })
    }
}
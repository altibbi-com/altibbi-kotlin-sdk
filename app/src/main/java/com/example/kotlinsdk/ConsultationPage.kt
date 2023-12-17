package com.example.kotlinsdk

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.altibbi.Consultation
import java.io.File


class ConsultationPage : AppCompatActivity() {
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation_page)
        val spinner = findViewById<Spinner>(R.id.spinner1)

        val values = listOf("chat", "call", "video")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        galleryActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleGalleryResult(result.resultCode, result.data)
        }
        val consultation = Consultation()

        val createConsultationButton = findViewById<Button>(R.id.button2);

        val uploadImageButton = findViewById<Button>(R.id.button13);
        uploadImageButton.setOnClickListener{
            showImagePickerFun()
        }

        createConsultationButton.setOnClickListener {
            createConsultationFun(consultation)
        }

        val cancelButton = findViewById<Button>(R.id.button5);
        cancelButton.setOnClickListener{
            cancelConsultationFun(consultation)
        }

        val getConsultationByIdButton = findViewById<Button>(R.id.button7);
        getConsultationByIdButton.setOnClickListener{
            getConsultationByIdFun(consultation)
        }

        val getConsultationListButton = findViewById<Button>(R.id.button8);
        getConsultationListButton.setOnClickListener{
            getConsultationListFun(consultation)
        }

        val deleteConsultationByIdButton = findViewById<Button>(R.id.button9);
        deleteConsultationByIdButton.setOnClickListener{
            deleteConsultationFun(consultation)
        }

        val getPrescriptionButton = findViewById<Button>(R.id.button14);
        getPrescriptionButton.setOnClickListener{
            getPrescriptionFun(consultation, applicationContext)
        }
    }

    private fun getPrescriptionFun(consultation: Consultation, context: Context) {
        println("context is -> ${context.packageName}")
        val consultationToGet: EditText = findViewById(R.id.textInputEditText15)
        val id: String = consultationToGet.text.toString()

        consultation.getPrescription(id, context, object : Consultation.DownloadPrescriptionCallback {
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
        println("in handleGalleryResult")
        if (resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            println("imageUri is -> $imageUri")

            if (imageUri != null) {
                val imageFile: File? = uriToFile(imageUri)

                if (imageFile != null && imageFile.exists()) {
                    println("imageFile is $imageFile")
                    val consultation = Consultation()

                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ){
                        consultation.uploadMedia("https://tawuniya.altibb.com/v1/media" , imageFile, object :
                            Consultation.UploadCallback {
                            override fun onSuccess(response: Consultation.UploadMediaResponse) {
                                println("uploadMedia response is -> $response")
                                if(response is Consultation.UploadMediaResponse){
                                    println("uploadMedia response all data is is -> $response")
                                }
                            }

                            override fun onError(error: Consultation.ErrorResponse) {
                                if(error is Consultation.ErrorResponse){
                                    println("uploadMedia error all data is -> $error")
                                }
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


    private fun showImagePickerFun(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher?.launch(galleryIntent)
    }

    private fun getConsultationListFun(consultation: Consultation) {
        consultation.getConsultationList(object : Consultation.GetConsultationListCallBack{
            override fun onSuccess(response: List<Consultation.ConsultationResponse>?) {
                if(response is List<Consultation.ConsultationResponse>){
                    println("getConsultationList all data is  -> $response")
                }
            }
        })
    }


    private fun getConsultationByIdFun(consultation: Consultation) {
        val consultationToGet: EditText = findViewById(R.id.textInputEditText4)
        val id: String = consultationToGet.text.toString()

        consultation.getConsultationById(id, object : Consultation.GetConsultationByIdCallBack{
            override fun onSuccess(response: Consultation.GetConsultationByIdResponse) {
                if(response is Consultation.GetConsultationByIdResponse){
                    println("GetConsultationByIdResponse all data is -> $response")
                }
            }

            override fun onError(error: Consultation.GetConsultationByIdNotFoundResponse) {
                if(error is Consultation.GetConsultationByIdNotFoundResponse){
                    println("error is in GetConsultationByIdNotFoundResponse -> $error")
                }
            }

            override fun onErrorObj(error: Consultation.ConsultationNotFound) {
                if(error is Consultation.ConsultationNotFound){
                    println("error is in GetConsultationByIdNotFoundResponse 123 -> $error")
                }
            }
        })

    }


    private fun cancelConsultationFun(consultation: Consultation) {
        val consultationId: EditText = findViewById(R.id.textInputEditText3)
        val id: String = consultationId.text.toString()

        consultation.cancelConsultation(
            id,
            object : Consultation.CancelConsultationCallBack{
                override fun onSuccess(response: Consultation.CancelConsultationResponse){
                    println("Cancel Consultation Response not all data -> $response")
                    if(response is Consultation.CancelConsultationResponse){
                        println("Cancel Consultation Response all data is -> $response")
                    }
                }
                override fun onError(error: Consultation.ErrorResponse ) {
                    println("Received Error in callback createConsultation: $error")
                    if(error is Consultation.ErrorResponse){
                        println("error all data is -> $error")
                    }
                }
                override fun onErrorObj(error: Consultation.ConsultationNotFound){
                    if (error is Consultation.ConsultationNotFound){
                        println("error all data in onErrorObj is -> $error")
                    }
                }
            }
        )
    }


    private fun createConsultationFun(consultation: Consultation) {

        val spinner = findViewById<Spinner>(R.id.spinner1)

        val values = listOf("chat", "call", "video")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val textInputEditText: EditText = findViewById(R.id.textInputEditText)
        val parentConsId: EditText = findViewById(R.id.textInputEditText6)
        val parentConsultationId = parentConsId.text.toString().toIntOrNull()

        val consultationData = Consultation.ConsultationData(
            question = textInputEditText.text.toString(),
            medium = (spinner.selectedItem as String),
            userID = 64,
            mediaIDs = listOf("c8617c16-98ef-11ee-9bc6-9600009a97a9"),
            followUpId = parentConsultationId
        )
        consultation.createConsultation(
            consultationData,
            object : Consultation.CreateConsultationCallback {
                override fun onSuccess(response: Consultation.ConsultationResponse) {
                    println("Received response in callback createConsultation: $response")
                    if(response is Consultation.ConsultationResponse){
                        println("Received response in callback createConsultation all data is :${response}")
                    }
                }
                override fun onError(error: String) {
                    println("Received Error in callback createConsultation: $error")
                }
            }
        )
    }


    private fun deleteConsultationFun(consultation: Consultation) {
        val deleteConsId: EditText = findViewById(R.id.textInputEditText5)
        val id: String = deleteConsId.text.toString()
        consultation.deleteConsultation(id, object : Consultation.DeleteConsultationCallBack{
            override fun onSuccess(response: Any?) {
                println("deleteConsultation onSuccess response is -> $response")
            }

            override fun onError(error: Consultation.ConsultationNotFound) {
                if(error is Consultation.ConsultationNotFound){
                    println("deleteConsultation onError response is -> $error")
                }
            }
        })
    }
}
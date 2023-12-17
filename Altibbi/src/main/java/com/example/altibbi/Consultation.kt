package com.example.altibbi

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Serializable
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL


class Consultation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation)
    }

    private val expandValues = "pusherAppKey,parentConsultation,consultations,user,media,pusherChannel,chatConfig,chatHistory,voipConfig,videoConfig,recommendation"

    data class ConsultationResponse(
        val id: Int,
        @SerializedName("user_id") val userId: Int?,
        @SerializedName("doctor_name") val doctorName: String?,
        val question: String,
        val medium: String,
        val status: String,
        @SerializedName("is_fulfilled") val isFulfilled: Int?,
        @SerializedName("parent_consultation_id") val parentConsultationId: Int?,
        @SerializedName("accepted_at") val acceptedAt: String?,
        @SerializedName("closed_at") val closedAt: String?,
        @SerializedName("created_at") val createdAt: String?,
        @SerializedName("updated_at") val updatedAt: String?,
        @SerializedName("doctor_avatar") val doctorAvatar: String?,
        val user: User.UserResponse,
        val parentConsultation: Any?,
        val consultations: List<Any>,
        val media: List<Media.Data>,
        val pusherChannel: String,
        val chatConfig: ChatConfig.Data,
        val chatHistory: ChatHistory.Data,
        val voipConfig: VoipConfig.Data,
        val videoConfig: VoipConfig.Data
    )

    interface CreateConsultationCallback {
        fun onSuccess(response: ConsultationResponse)
        fun onError(error: String)
    }

    data class ConsultationData(
        val question: String,
        val medium: String,
        val userID: Int,
        val mediaIDs: List<String>? = null,
        val followUpId: Int? = null
    )

    fun createConsultation(
        createConsultationRequest: ConsultationData,
        callback: CreateConsultationCallback) {
        println("createConsultationRequest object ---> $createConsultationRequest")
        Thread {
            try {
                val url = URL("https://tawuniya.altibb.com/v1/consultations?expand=$expandValues")
                val connection = url.openConnection() as HttpURLConnection

                println("the url is -> $url")

                connection.requestMethod = "POST"
                connection.doOutput = true

                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")
                var params = "question=${URLEncoder.encode(createConsultationRequest.question, "UTF-8")}" +
                        "&medium=${URLEncoder.encode(createConsultationRequest.medium, "UTF-8")}" +
                        "&user_id=${createConsultationRequest.userID}"
                if(createConsultationRequest.followUpId != null){
                    params += "&parent_consultation_id=${createConsultationRequest.followUpId}"
                }
//                if(createConsultationRequest.mediaIDs != null){
//                    params += "&media_ids=${createConsultationRequest.mediaIDs}"
//                }

                println("params before call api $params")

                val outputStream: OutputStream = connection.outputStream
                val writer = OutputStreamWriter(outputStream)

                writer.write(params)
                writer.flush()

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage

                println("responseMessage => $responseMessage")
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 201) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val responseStringBuilder = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseStringBuilder.append(line).append("\n")
                    }

                    reader.close()
                    inputStream.close()

                    val response = responseStringBuilder.toString()
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, ConsultationResponse::class.java)

                    callback.onSuccess(responseObject)
                    println("Create Consultation Response: $response")
                } else {
                    println("Error: $responseCode")
                    val errorStream = connection.errorStream
                    val errorReader = BufferedReader(InputStreamReader(errorStream))
                    val errorResponseStringBuilder = StringBuilder()

                    var errorLine: String?
                    while (errorReader.readLine().also { errorLine = it } != null) {
                        errorResponseStringBuilder.append(errorLine).append("\n")
                    }

                    errorReader.close()
                    errorStream.close()

                    val errorResponse = errorResponseStringBuilder.toString()
                    callback.onError(errorResponse)
                    println("Error Response: $errorResponse")
                }

                println("create consultation response is => $responseCode")

            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }.start()
    }

    interface DownloadPrescriptionCallback {
        fun onSuccess(file: String)
        fun onError(errorMessage: String)
    }


    fun getPrescription(consultationId: String, context: Context, callback: DownloadPrescriptionCallback){
        Thread {
            val url = URL("https://tawuniya.altibb.com/v1/consultations/$consultationId/download-prescription")

            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val disposition = connection.getHeaderField("Content-Disposition")
                    val fileName = "prescription_$consultationId.pdf"
                    println("fileName -> $fileName")

                    val outputFile = File.createTempFile(fileName, null, context.cacheDir)
                    val outputStream = FileOutputStream(outputFile)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }

                    callback.onSuccess(outputFile.path)
                } else {
                    callback.onError("Failed to download prescription. HTTP response code: $responseCode")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                callback.onError("Error occurred: ${ex.message}")
            } finally {
                connection.disconnect()
            }
        }.start()
    }

    data class CancelConsultationResponse(
        @SerializedName("consultation_id") val consultationId: Int?,
        val status: String,
    )
    
    
        interface CancelConsultationCallBack {
            fun onSuccess(response: CancelConsultationResponse)
            fun onError(error: ErrorResponse)
            fun onErrorObj(error: ConsultationNotFound)
        }

    data class ErrorResponse(
        val field: String,
        val message: String
    )

    data class ConsultationNotFound(
        val name: String,
        val message: String,
        val code: Int,
        val status: Int,
        val type: String
    )

    fun cancelConsultation(consultationId: String, callback: CancelConsultationCallBack){
        println("consultationId is -> $consultationId")
        Thread {
            val url = URL("https://tawuniya.altibb.com/v1/consultations/$consultationId/cancel")
            println("url is -> $url")

            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.doOutput = true

                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")

                val responseCode = connection.responseCode

                val inputStream: InputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val reader = BufferedReader(InputStreamReader(inputStream))
                val responseStringBuilder = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    responseStringBuilder.append(line).append("\n")
                }

                reader.close()
                inputStream.close()

                val response = responseStringBuilder.toString()
                println("cancel cons response in SDK -> $response")

                val jsonReader = JsonReader(StringReader(response))
                val gson = Gson()


                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, CancelConsultationResponse::class.java)
                    callback.onSuccess(responseObject)
                } else {
                    val jsonElement = JsonParser.parseReader(jsonReader)
                    if (jsonElement.isJsonArray) {
                        val errorArray = gson.fromJson(jsonElement, Array<ErrorResponse>::class.java)
                        val firstError = errorArray.firstOrNull()
                        if (firstError != null) {
                            callback.onError(firstError)
                        } else {
                            callback.onError(ErrorResponse("unexpected", "Empty error array"))
                        }
                    } else {
                        val gson = Gson()
                        val responseObject = gson.fromJson(response, ConsultationNotFound::class.java)
                        if(responseObject is ConsultationNotFound){
                            callback.onErrorObj(responseObject)
                        }else{
                            callback.onError(ErrorResponse("unexpected", "Unexpected JSON structure"))
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                connection.disconnect()
            }
        }.start()
    }



    data class GetConsultationByIdResponse(
        val id: Int,
        @SerializedName("user_id") val userId: Int?,
        @SerializedName("doctor_name") val doctorName: String?,
        val question: String,
        val medium: String,
        val status: String,
        @SerializedName("is_fulfilled") val isFulfilled: Int?,
        @SerializedName("parent_consultation_id") val parentConsultationId: Int?,
        @SerializedName("accepted_at") val acceptedAt: String?,
        @SerializedName("closed_at") val closedAt: String?,
        @SerializedName("created_at") val createdAt: String?,
        @SerializedName("updated_at") val updatedAt: String?,
        @SerializedName("doctor_avatar") val doctorAvatar: String?,
        val user: User.UserResponse,
        val parentConsultation: Any?,
        val consultations: List<Any>,
        val media: List<Media.Data>,
        val pusherChannel: String,
        val chatConfig: ChatConfig.Data,
        val chatHistory: ChatHistory.Data,
        val voipConfig: VoipConfig.Data,
        val videoConfig: VoipConfig.Data,
    )

    data class GetConsultationByIdNotFoundResponse(
        val name: String,
        val message: String,
        val code: Int,
        val status: Int,
        val type: String
    )

    interface GetConsultationByIdCallBack {
        fun onSuccess(response: GetConsultationByIdResponse)
        fun onError(error: GetConsultationByIdNotFoundResponse)
        fun onErrorObj(error: ConsultationNotFound)
    }

    fun getConsultationById(consultationId: String, callback: GetConsultationByIdCallBack){

        Thread{
            val url = URL("https://tawuniya.altibb.com/v1/consultations/${consultationId}?expand=$expandValues")

            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val responseStringBuilder = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseStringBuilder.append(line).append("\n")
                    }
                    reader.close()
                    inputStream.close()
                    val response = responseStringBuilder.toString()
                    println("Get Consultation By Id Response String ->: $response")
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, GetConsultationByIdResponse::class.java)
                    callback.onSuccess(responseObject)
                } else {
                    val errorStream = connection.errorStream
                    val reader = BufferedReader(InputStreamReader(errorStream))
                    val responseStringBuilder = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseStringBuilder.append(line).append("\n")
                    }
                    reader.close()
                    errorStream.close()
                    val errorResponse = responseStringBuilder.toString()
                    val gson = Gson()
                    val errorObject = gson.fromJson(errorResponse, ConsultationNotFound::class.java)
                    callback.onErrorObj(errorObject)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                connection.disconnect()
            }
        }.start()

    }

    interface GetConsultationListCallBack {
        fun onSuccess(response: List<ConsultationResponse>?)
//        fun onError(error: GetConsultationByIdNotFoundResponse)
//        fun onErrorObj(error: ConsultationNotFound)
    }

    fun getConsultationList(callback: GetConsultationListCallBack){
        Thread{
            val url = URL("https://tawuniya.altibb.com/v1/consultations?expand=$expandValues")
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val responseStringBuilder = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseStringBuilder.append(line).append("\n")
                    }
                    reader.close()
                    inputStream.close()
                    val response = responseStringBuilder.toString()
                    println("response string is -> $response")
                    val gson = Gson()
                    val consultationsList: List<ConsultationResponse> = gson.fromJson(response, object : TypeToken<List<ConsultationResponse>>() {}.type)
                    callback.onSuccess(consultationsList)
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                connection.disconnect()
            }
        }.start()
    }

    interface DeleteConsultationCallBack{
        fun onSuccess(response: Any?)
        fun onError(response: ConsultationNotFound)
    }

    fun deleteConsultation(consultationId: String?, callback: DeleteConsultationCallBack){
        Thread{
            val url = URL("https://tawuniya.altibb.com/v1/consultations/$consultationId")
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")

                val responseCode = connection.responseCode
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val responseStringBuilder = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    responseStringBuilder.append(line).append("\n")
                }

                reader.close()
                inputStream.close()

                val response = responseStringBuilder.toString()
                println("Response Body: $response")
                if(responseCode == 204){
                    callback.onSuccess("success")
                }else{
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, ConsultationNotFound::class.java)
                    callback.onError(responseObject)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                connection.disconnect()
            }
        }.start()
    }

    data class UploadMediaResponse(
        val id: String,
        val type: String,
        val name: String,
        val path: String,
        val extension: String,
        val size: Int,
        val url: String
    )

    interface UploadCallback {
        fun onSuccess(response: UploadMediaResponse)
        fun onError(error: ErrorResponse)
    }

    fun uploadMedia(apiUrl: String, file: File?, callback: UploadCallback) {
        Thread{
            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection

                println("url in uploadMedia $url")
                println("Absolute File Path: ${file?.absolutePath}")

                // Set connection properties
                connection.doOutput = true
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****")


                if (file != null) {
                    val outputStream = DataOutputStream(connection.outputStream)

                    // Add file parameter
                    val fieldName = "file"
                    val fileName = file?.name
                    val lineEnd = "\r\n"
                    val twoHyphens = "--"
                    val boundary = "*****"

                    println("File Name: $fileName, File Size: ${file?.length()}")

                    val fileInputStream = FileInputStream(file)
                    val bytesAvailable = fileInputStream.available()
                    val bufferSize = bytesAvailable.coerceAtMost(1024)
                    val buffer = ByteArray(bufferSize)

                    // Write start boundary
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"$fieldName\";filename=\"$fileName\"$lineEnd")
                    outputStream.writeBytes(lineEnd)

                    // Read file and write to output stream
                    var bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                    while (bytesRead > 0) {
                        outputStream.write(buffer, 0, bytesRead)
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                    }

                    // Write end boundary
                    outputStream.writeBytes(lineEnd)
                    outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                    // Close streams
                    fileInputStream.close()
                    outputStream.flush()
                    outputStream.close()

                    // Get the response
                    val responseCode = connection.responseCode
                    println("responseCode is -> $responseCode")
                    val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val responseStringBuilder = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseStringBuilder.append(line).append("\n")
                    }

                    reader.close()
                    inputStream.close()

                    val response = responseStringBuilder.toString()
                    println("Response is -> : $response")

                    // Handle the response based on your requirements
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val gson = Gson()
                        val responseObject = gson.fromJson(response, UploadMediaResponse::class.java)
                        callback.onSuccess(responseObject)
                    } else {
                        val gson = Gson()
                        val jsonReader = JsonReader(StringReader(response))
                        val jsonElement = JsonParser.parseReader(jsonReader)
                        if (jsonElement.isJsonArray) {
                            val errorArray = gson.fromJson(jsonElement, Array<ErrorResponse>::class.java)
                            val firstError = errorArray.firstOrNull()
                            if (firstError != null) {
                                callback.onError(firstError)
                            } else {
                                callback.onError(ErrorResponse("unexpected", "Empty error array"))
                            }
                        }
                    }
                }

            } catch (ex: Exception) {
                ex.printStackTrace() }

        }.start()
    }


}
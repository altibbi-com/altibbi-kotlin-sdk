package com.example.altibbi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import com.google.gson.Gson


class AltibbiSdk : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_altibbi_sdk)
    }

    data class ConsultationResponse(
        val id: Int,
        val user_id: Int,
        val question: String,
        val medium: String,
        val status: String,
        val parent_consultation_id: String
    )

    data class UserResponse(
        val id: Int,
        val name: String,
        val medium: String,
        val nationality_number: String,
        val phone_number: String,
        val date_of_birth: String,
        val gender: String,
        val insurance_id: String,
        val policy_number: String,
        val avatar_media_id: String,
        val height: Int,
        val weight: Int,
        val blood_type: String,
        val smoker: Boolean,
        val alcoholic: String,
        val marital_status: String,
        val created_at: String,
        val updated_at: String,
    )

    private fun getParamsString(params: Map<String, Any>): String {
        val result = StringBuilder()
        var first = true

        for ((key, value) in params) {
            if (first) {
                first = false
            } else {
                result.append("&")
            }

            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
//            result.append(URLEncoder.encode(value, "UTF-8"))
        }

        return result.toString()
    }

    fun createConsultation(
        createConsultationRequest: Map<String, Any>,
        callback: CreateConsultationCallback) {
        println("createConsultationRequest object ---> $createConsultationRequest")
        Thread {
            try {
                val url = URL("https://tawuniya.altibb.com/v1/consultations")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.doOutput = true

                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")

                val postData = "question=${URLEncoder.encode(createConsultationRequest["questionBody"].toString(), "UTF-8")}" +
                        "&medium=${URLEncoder.encode(createConsultationRequest["type"].toString(), "UTF-8")}" +
                        "&attachments_ids=" +
                        "&user_id=${createConsultationRequest["userId"]}"

                val outputStream: OutputStream = connection.outputStream
                val writer = OutputStreamWriter(outputStream)

                writer.write(postData)
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

    interface CreateConsultationCallback {
        fun onSuccess(response: ConsultationResponse)
        fun onError(error: String)
    }

    interface GetUserCallback {
        fun onSuccess(response: UserResponse)
        fun onError(errorCode: Int)
    }

    fun getPhrById(phrId: String, callback: GetUserCallback) {
        println("phrId in getPhtById fun => $phrId")
        Thread {
            try {
                val url = URL("https://tawuniya.altibb.com/v1/users/$phrId")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage

                println("responseMessage => $responseMessage")
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
                    println("Response: $response")

                    val gson = Gson()
                    val responseObject = gson.fromJson(response, UserResponse::class.java)

                    callback.onSuccess(responseObject)
                } else {
                    println("Error: $responseCode")
                    callback.onError(responseCode)
                }

                println("get phr by id response is => $responseCode")

            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }.start()
    }
}
package com.example.altibbi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

class User : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
    }

    data class UserNotFound(
        val name: String,
        val message: String,
        val code: Int,
        val status: Int,
        val type: String
    )

    data class UserResponse(
        val id: Int,
        val name: String,
        @SerializedName("nationality_number") val nationalityNumber: String?,
        @SerializedName("phone_number") val phoneNumber: String?,
        val email: String?,
        @SerializedName("date_of_birth") val dateOfBirth: String?,
        val gender: String,
        @SerializedName("insurance_id") val insuranceId: String?,
        @SerializedName("policy_number") val policyNumber: String?,
        val height: String?,
        val weight: String?,
        @SerializedName("blood_type") val bloodType: String?,
        val smoker: String?,
        val alcoholic: String?,
        @SerializedName("marital_status") val maritalStatus: String?,
        @SerializedName("created_at") val createdAt: String?,
        @SerializedName("updated_at") val updatedAt: String?
    )

    interface GetUserCallback {
        fun onSuccess(response: UserResponse)
        fun onError(error: UserNotFound)
    }

    fun getUser(phrId: String, callback: GetUserCallback) {
        Thread {
            try {
                val url = URL("https://tawuniya.altibb.com/v1/users/$phrId")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
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
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, UserResponse::class.java)
                    callback.onSuccess(responseObject)
                } else {
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, UserNotFound::class.java)
                    callback.onError(responseObject)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.start()
    }

    interface DeleteUserCallBack{
        fun onSuccess(response: Any?)
        fun onError(response: UserNotFound)
    }

    fun deleteUser(id: String?, callback: DeleteUserCallBack){
        Thread{
            val url = URL("https://tawuniya.altibb.com/v1/users/$id")
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")

                val responseCode = connection.responseCode
                println("deleteUser responseCode -> $responseCode")

                if (responseCode == 204) {
                    callback.onSuccess("success")
                } else {
                    val inputStream = connection.errorStream // Use errorStream to get error response
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val responseStringBuilder = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseStringBuilder.append(line).append("\n")
                    }

                    reader.close()
                    inputStream.close()

                    val response = responseStringBuilder.toString()
                    println("deleteUser error response Body: $response")

                    val gson = Gson()
                    val errorResponse = gson.fromJson(response, UserNotFound::class.java)
                    callback.onError(errorResponse)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                connection.disconnect()
            }
        }.start()
    }

    interface GetUsersCallback {
        fun onSuccess(response: List<UserResponse>)
        fun onError(error: UserNotFound)
    }

    fun getUsers(callback: GetUsersCallback) {
        Thread {
            try {
                val url = URL("https://tawuniya.altibb.com/v1/users")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
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
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val gson = Gson()
                    val userResponse: List<UserResponse> = gson.fromJson(response, object : TypeToken<List<UserResponse>>() {}.type)
                    callback.onSuccess(userResponse)
                } else {
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, UserNotFound::class.java)
                    callback.onError(responseObject)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.start()
    }

    private val paramsList = mutableListOf<String>()
    private fun addParam(name: String, value: Any?) {
        value?.let { paramsList.add("$name=${it.toString()}") }
    }

    interface CreateUserCallBack{
        fun onSuccess(response: UserResponse)
        fun onError(response: UserNotFound)
    }

    fun createUser(userParams: Map<String, Any>,callback: CreateUserCallBack){
        Thread{
            try {
                val url = URL("https://tawuniya.altibb.com/v1/users")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")
                userParams.forEach { (key, value) -> addParam(key, value) }
                val params = paramsList.joinToString("&")

                val outputStream: OutputStream = connection.outputStream
                val writer = OutputStreamWriter(outputStream)

                writer.write(params)
                writer.flush()
                val responseCode = connection.responseCode
                val inputStream: InputStream = if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 201) {
                    connection.inputStream
                } else {
                    connection.errorStream ?: ByteArrayInputStream("".toByteArray())
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
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 201) {
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, UserResponse::class.java)
                    callback.onSuccess(responseObject)
                } else {
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, UserNotFound::class.java)
                    callback.onError(responseObject)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }.start()
    }

    interface UpdateUserCallback {
        fun onSuccess(response: UserResponse)
        fun onError(error: UserNotFound)
    }

    fun updateUser(userParams: Map<String, Any>,callback: UpdateUserCallback){
        println("userParams in updateUser -> $userParams")

        Thread{
            try {
                val url = URL("https://tawuniya.altibb.com/v1/users/${userParams["id"]}")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Authorization", "Bearer egq_2RRrH1fDEUWM-g6HINSibhqOYVM-")
                userParams.forEach { (key, value) -> addParam(key, value) }
                val params = paramsList.joinToString("&")
                val outputStream: OutputStream = connection.outputStream
                val writer = OutputStreamWriter(outputStream)

                writer.write(params)
                writer.flush()

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
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, UserResponse::class.java)
                    callback.onSuccess(responseObject)
                } else {
                    val gson = Gson()
                    val responseObject = gson.fromJson(response, UserNotFound::class.java)
                    callback.onError(responseObject)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.start()
    }



}
package com.altibbi.telehealth

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class NetworkRequest : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    class ApiManager {
        interface ApiCallback<T, E> {
            fun onSuccess(response: T)
            fun onError(error: Any)
        }
        companion object {
            fun <T, E> getRequest(
                url: String,
                params: Map<String, Any>? = null,
                isPdf: Boolean?,
                callback: ApiCallback<T, E>,
                responseType: Class<T>,
            ) {
                println("url before call the api   --> $url")
                println("params in getRequest --> $params")
                performHttpRequest("GET", url, params, isPdf, null, callback, responseType)
            }

            fun <T, E> postRequest(
                url: String,
                params: Map<String, Any?>? = null,
                file: File? = null,
                callback: ApiCallback<T, E>,
                responseType: Class<T>
            ) {
                println("params in postRequest ----> $params")
                performHttpRequest("POST", url, params, false, file ,callback, responseType)
            }

            fun <T, E> putRequest(
                url: String,
                params: Map<String, Any?>? = null,
                callback: ApiCallback<T, E>,
                responseType: Class<T>
            ) {
                performHttpRequest("PUT", url, params, false,null,callback, responseType)
            }

            fun <T, E> deleteRequest(
                url: String,
                params: Map<String, Any>? = null,
                callback: ApiCallback<T, E>,
                responseType: Class<T>
            ) {
                performHttpRequest("DELETE", url, params,false, null, callback, responseType)
            }

            private fun <T, E> performHttpRequest(
                method: String,
                url: String,
                params: Map<String, Any?>? = null,
                isPdf: Boolean? = false,
                file: File? = null,
                callback: ApiCallback<T, E>,
                responseType: Class<T>,
            ) {
                Thread {
                    try {
                        val connection = URL("${Constants.ENDPOINT}${url}${buildQueryString(params)}").openConnection() as HttpURLConnection
                        connection.requestMethod = method
                        connection.doOutput = method == "POST" || method == "PUT"

                        if (isPdf == true){
                            connection.setRequestProperty("Content-Type", "application/pdf")
                        }else{
                            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                        }
                        connection.setRequestProperty("Authorization", "Bearer ${Constants.AUTH}")

                        if (file == null) {
                            if (params != null && (method == "POST" || method == "PUT")) {
                                val encodedParams = buildEncodedParams(params)
                                val outputStream: OutputStream = connection.outputStream
                                val writer = OutputStreamWriter(outputStream)

                                writer.write(encodedParams)
                                writer.flush()
                            }
                        } else {
                            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****")
                            val outputStream = DataOutputStream(connection.outputStream)
                            val fieldName = "file"
                            val fileName = file.name
                            val lineEnd = "\r\n"
                            val twoHyphens = "--"
                            val boundary = "*****"
                            outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                            outputStream.writeBytes("Content-Disposition: form-data; name=\"$fieldName\";filename=\"$fileName\"$lineEnd")
                            outputStream.writeBytes(lineEnd)

                            val fileInputStream = FileInputStream(file)
                            val buffer = ByteArray(1024)
                            var bytesRead: Int
                            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                            outputStream.writeBytes(lineEnd)
                            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                            fileInputStream.close()
                            outputStream.flush()
                            outputStream.close()
                        }

                        handleResponse(connection, callback, responseType, isPdf)

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        callback.onError("Exception: ${ex.message}")
                    }
                }.start()
            }
            private fun <T, E> handleResponse(
                connection: HttpURLConnection,
                callback: ApiCallback<T, E>,
                responseType: Class<T>,
                isPdf: Boolean?
            ) {
                try {
                    val responseCode = connection.responseCode
                    println("responseCode is -> $responseCode")

                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == 204) {
                        if (isPdf == true) {
                            // Handle PDF response
                            val pdfInputStream = connection.inputStream
                            // Process the PDF stream as needed (e.g., save it to a file)
                            // You can also provide the InputStream to the callback if necessary
                            println("pdfInputStream is -> $pdfInputStream")
                            val pdfFile = savePdfToFile(pdfInputStream, "prescription_386.pdf")
                            println("pdfFile is -> $pdfFile")
//                            callback.onSuccess(pdfInputStream)
                            return
                        }
                        val inputStream = connection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val responseStringBuilder = StringBuilder()

                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            responseStringBuilder.append(line).append("\n")
                        }

                        reader.close()
                        inputStream.close()
                        if (responseCode == 204){
//                            callback.onSuccess("Success");
                        }

                        val response = responseStringBuilder.toString()
                        val gson = Gson()
                        val responseObject = gson.fromJson(response, responseType)

                        println("responseObject in getRequest $responseObject")

                        callback.onSuccess(responseObject)
                    } else {
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
                        println("in sdk Error Response: $errorResponse")
                    }
                } finally {
                    connection.disconnect()
                }
            }

            private fun buildEncodedParams(params: Map<String, Any?>?): String {
                return params?.map { (key, value) ->
                    "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value.toString(), "UTF-8")}"
                }!!.joinToString("&")
            }

            private fun buildQueryString(params: Map<String, Any?>?): String {
                return if (params != null) {
                    "?" + buildEncodedParams(params)
                } else {
                    ""
                }
            }

            private fun savePdfToFile(inputStream: InputStream, fileName: String): File {
                val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val downloadPath = downloadDirectory.absolutePath

                val outputFile = File(downloadPath, fileName)
                try {
                    if (!outputFile.exists()) {
                        outputFile.createNewFile()
                    }
                    println("outputFile is -> $outputFile")
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
        }
    }
}

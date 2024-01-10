package com.altibbi.telehealth

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


interface ApiCallback<T> {
    fun onSuccess(response: T)
    fun onFailure(error: String?)
    fun onRequestError(error: String?)
}
class ApiService {
    private val okHttpClient = OkHttpClient()
    private fun callApi(
        endpoint: String,
        method: String,
        body: Map<String, Any?> = emptyMap(),
        file: File? = null,
        page: Int? = null,
        perPage: Int? = null
    ): Call {
        val token = AltibbiService.authToken
        val baseURL = AltibbiService.url
        val lang = AltibbiService.lang
        if (token == null) {
            throw IOException("Token is missing or invalid.")
        }
        val url: String = when (method) {
            "GET" -> {
                val queryParameters = body.entries.associate { (key, value) ->
                    key to value.toString()
                }.toMutableMap()
                if (perPage != null && page != null) {
                    queryParameters["per-page"] = perPage.toString()
                    queryParameters["page"] = page.toString()
                }
                "$baseURL/v1/$endpoint?${queryParameters.entries.joinToString("&") { "${it.key}=${it.value}" }}"
            }
            else -> "$baseURL/v1/$endpoint"
        }
        val requestBuilder = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("accept-language", lang)
        if (file != null) {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",  // The key for the file field in the form
                    file.name,  // The name of the file
                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
                .build()
            requestBuilder.post(requestBody)
            val request: Request = requestBuilder.build()
            return okHttpClient.newCall(request)
        }
        requestBuilder.header("Content-Type", "application/json")
        val formBuilder = FormBody.Builder()
        for ((key, value) in body) {
            formBuilder.add(key, value.toString())
        }
        val requestBody: RequestBody = formBuilder.build()
        when (method) {
            "GET" -> requestBuilder.get()
            "POST" -> requestBuilder.post(requestBody)
            "PUT" -> requestBuilder.put(requestBody)
            "DELETE" -> requestBuilder.delete()
        }
        val request: Request = requestBuilder.build()
        return okHttpClient.newCall(request)
    }
    fun getUser(userId: String, callback: ApiCallback<User>) {
        val response: Call = callApi("users/$userId", "GET");
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    callback.onSuccess(Gson().fromJson(responseBody, User::class.java))
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun getUsers(callback: ApiCallback<List<User>>) {
        val response: Call = callApi("users", "GET");
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    val userListType = object : TypeToken<List<User>>() {}.type
                    callback.onSuccess(Gson().fromJson(responseBody, userListType))
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun createUser(user: User, callback: ApiCallback<User>) {
        val response: Call = callApi("users", "POST", user.toJson());
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 201) {
                    val responseBody = response.body?.string()
                    callback.onSuccess(Gson().fromJson(responseBody, User::class.java))
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun updateUser(user: User, userId: String?, callback: ApiCallback<User>) {
        val response: Call = callApi("users/${userId}", "PUT", user.toJson());
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    callback.onSuccess(Gson().fromJson(responseBody, User::class.java))
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun deleteUser(userId: String, callback: ApiCallback<Boolean>) {
        val response: Call = callApi("users/${userId}", "DELETE");
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 204) {
                    callback.onSuccess(true)
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun getConsultationList(
        page: Int? = 1,
        perPage: Int? = 20,
        userId: Int? = null,
        callback: ApiCallback<List<Consultation>>
    ) {
        var body: MutableMap<String, Any> = mutableMapOf(
            "expand" to "pusherAppKey,parentConsultation,consultations,user,media,pusherChannel," +
                    "chatConfig,chatHistory,voipConfig,videoConfig,recommendation"
        )
        if (userId != null) {
            body = body.toMutableMap()
            body["filter[user_id]"] = userId
        }
        val response: Call = callApi(
            endpoint = "consultations",
            method = "GET",
            body = body,
            page = page,
            perPage = perPage
        );
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    val consultations: List<Consultation> = Gson().fromJson(
                        responseBody,
                        object : TypeToken<List<Consultation>>() {}.type
                    )
                    callback.onSuccess(consultations)
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun getLastConsultation(callback: ApiCallback<Consultation>) {
        val body: MutableMap<String, Any> = mutableMapOf(
            "expand" to "pusherAppKey,parentConsultation,consultations,user,media,pusherChannel," +
                    "chatConfig,chatHistory,voipConfig,videoConfig,recommendation",
            "sort" to "-id",
            "per-page" to 1
        )
        val response: Call = callApi(
            endpoint = "consultations",
            method = "GET",
            body = body,
        );
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    val consultations: List<Consultation> = Gson().fromJson(
                        responseBody,
                        object : TypeToken<List<Consultation>>() {}.type
                    )
                    callback.onSuccess(consultations[0])
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun getConsultationInfo(consultationId: String, callback: ApiCallback<Consultation>) {
        val body: MutableMap<String, Any> = mutableMapOf(
            "expand" to "pusherAppKey,parentConsultation,consultations,user,media,pusherChannel," +
                    "chatConfig,chatHistory,voipConfig,videoConfig,recommendation"
        )
        val response: Call = callApi(
            endpoint = "consultations/${consultationId}",
            method = "GET",
            body = body,
        );
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    callback.onSuccess(Gson().fromJson(responseBody, Consultation::class.java))
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun createConsultation(
        question: String,
        medium: Medium,
        userID: Int,
        mediaIDs: List<String>? = null,
        followUpId: String? = null,
        callback: ApiCallback<Consultation>
    ) {
        if (!Medium.values().contains(medium)) {
            throw Exception("Invalid medium value")
        }
        val body: MutableMap<String, Any> = mutableMapOf(
            "question" to question,
            "medium" to medium.toString(),
            "user_id" to userID,
            "expand" to "pusherAppKey,parentConsultation,consultations,user,media,pusherChannel," +
                    "chatConfig,chatHistory,voipConfig,videoConfig,recommendation",
        )
        if(mediaIDs != null){
            body["media_ids"] = mediaIDs
        }
        if(followUpId != null){
            body["followUpId"] = followUpId
        }
        val response = callApi(
            endpoint = "consultations",
            method = "POST",
            body = body
        )
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 201) {
                    val responseBody = response.body?.string()
                    callback.onSuccess(Gson().fromJson(responseBody, Consultation::class.java))
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun deleteConsultation(consultationId: String, callback: ApiCallback<Boolean>) {
        val response: Call = callApi("consultations/${consultationId}", "DELETE");
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 204) {
                    callback.onSuccess(true)
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun cancelConsultation(consultationId: String, callback: ApiCallback<Boolean>) {
        val response: Call = callApi("consultations/${consultationId}/cancel", "POST");
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    callback.onSuccess(true)
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun uploadMedia(file: File, callback: ApiCallback<Media>) {
        val response: Call = callApi(endpoint = "media", method = "POST", file = file);
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    callback.onSuccess(Gson().fromJson(responseBody, Media::class.java))
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
    fun getPrescription(consultationId: String, callback: ApiCallback<Response>) {
        val response: Call = callApi(
            endpoint = "consultations/$consultationId/download-prescription",
            method = "GET"
        );
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    callback.onSuccess(response)
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
}
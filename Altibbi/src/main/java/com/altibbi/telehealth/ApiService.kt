package com.altibbi.telehealth

import com.altibbi.telehealth.model.Article
import com.altibbi.telehealth.model.Consultation
import com.altibbi.telehealth.model.Media
import com.altibbi.telehealth.model.Medium
import com.altibbi.telehealth.model.PredictSpecialty
import com.altibbi.telehealth.model.PredictSummary
import com.altibbi.telehealth.model.Soap
import com.altibbi.telehealth.model.Transcription
import com.altibbi.telehealth.model.User
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
    companion object {
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

                    val baseLink = if (baseURL?.contains("rest-api") == true) baseURL else "$baseURL/v1/$endpoint"
                    "$baseLink?${queryParameters.entries.joinToString("&") { "${it.key}=${it.value}" }}"
                }

                else -> (if (baseURL?.contains("rest-api") == true) baseURL else "$baseURL/v1/$endpoint").toString()
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
            callback: ApiCallback<Consultation>,
            mediaIDs: List<String>? = null,
            followUpId: String? = null,
            forceWhiteLabelingPartnerName: String? = null,
            consultationCategoryId: Int? = null,
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
            if (mediaIDs != null) {
                body["media_ids"] = mediaIDs
            }
            if (followUpId != null) {
                body["parent_consultation_id"] = followUpId
            }
            if (forceWhiteLabelingPartnerName != null && forceWhiteLabelingPartnerName.length > 3) {
                body["question"] = "${body["question"]} ~${forceWhiteLabelingPartnerName}~"
            }
            if (consultationCategoryId != null) {
                body["consultation_category_id"] = consultationCategoryId
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

        fun rateConsultation(consultationId: String, score: Double, callback: ApiCallback<Boolean>) {
            val body: MutableMap<String, Any> = mutableMapOf(
                "score" to score,
            )
            val response: Call = callApi(
                endpoint = "consultations/$consultationId/rate",
                method = "POST",
                body = body,
            );
            response.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onRequestError(e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        callback.onSuccess(response = true)
                    } else {
                        callback.onFailure(response.body?.string())
                    }
                }
            })
        }

        fun getPredictSummary(consultationId: String, callback: ApiCallback<PredictSummary>) {
            val response: Call = callApi(
                endpoint = "consultations/${consultationId}/predict-summary",
                method = "GET"
            );
            response.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onRequestError(e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        val responseBody = response.body?.string()
                        callback.onSuccess(Gson().fromJson(responseBody, PredictSummary::class.java))
                    } else {
                        callback.onFailure(response.body?.string())
                    }
                }
            })
        }
        fun getTranscription(consultationId: String, callback: ApiCallback<Transcription>) {
            val response: Call = callApi(
                endpoint = "consultations/${consultationId}/transcription",
                method = "GET"
            );
            response.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onRequestError(e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        val responseBody = response.body?.string()
                        callback.onSuccess(Gson().fromJson(responseBody, Transcription::class.java))
                    } else {
                        callback.onFailure(response.body?.string())
                    }
                }
            })
        }
        fun getSoapSummary(consultationId: String, callback: ApiCallback<Soap>) {
            val response: Call = callApi(
                endpoint = "consultations/${consultationId}/soap-summary",
                method = "GET"
            );
            response.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onRequestError(e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        val responseBody = response.body?.string()
                        callback.onSuccess(Gson().fromJson(responseBody, Soap::class.java))
                    } else {
                        callback.onFailure(response.body?.string())
                    }
                }
            })
        }
        fun getPredictSpecialty(consultationId: String,  callback: ApiCallback<List<PredictSpecialty>>) {
            val response: Call = callApi(
                endpoint = "consultations/${consultationId}/predict-specialty",
                method = "GET"
            );
            response.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onRequestError(e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        val responseBody = response.body?.string()
                        val predictSpecialtyListType = object : TypeToken<List<PredictSpecialty>>() {}.type
                        callback.onSuccess(Gson().fromJson(responseBody, predictSpecialtyListType))
                    } else {
                        callback.onFailure(response.body?.string())
                    }
                }
            })
        }
    }

    fun getMediaList(
        page: Int? = 1,
        perPage: Int? = 20,
        callback: ApiCallback<List<Media>>
    ) {
        val response: Call = callApi(
            endpoint = "media",
            method = "GET",
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
                    val mediaList: List<Media> = Gson().fromJson(
                        responseBody,
                        object : TypeToken<List<Media>>() {}.type
                    )
                    callback.onSuccess(mediaList)
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }

    fun deleteMedia(mediaId: String, callback: ApiCallback<Boolean>) {
        val response: Call = callApi("media/${mediaId}", "DELETE");
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

    fun getArticlesList(
        subcategoryIds: List<Int>,
        page: Int? = 1,
        perPage: Int? = 20,
        callback: ApiCallback<List<Article>>
    ) {
        val body: Map<String, Any> = mapOf(
            "filter[sub_category_id][in]" to subcategoryIds.joinToString(","),
            "sort" to "-article_id"
        )

        val response: Call = callApi(
            endpoint = "https://rest-api.altibbi.com/active/v1/articles",
            method = "GET",
            page = page,
            perPage = perPage,
            body= body
        );
        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onRequestError(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    val articlesList: List<Article> = Gson().fromJson(
                        responseBody,
                        object : TypeToken<List<Article>>() {}.type
                    )
                    callback.onSuccess(articlesList)
                } else {
                    callback.onFailure(response.body?.string())
                }
            }
        })
    }
}
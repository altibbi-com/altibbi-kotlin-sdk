package com.altibbi.telehealth

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File

class ApiService {
    companion object {
        private const val expandValues = "pusherAppKey,parentConsultation,consultations,user,media,pusherChannel,chatConfig,chatHistory,voipConfig,videoConfig,recommendation"
        fun createConsultation(createConsultationRequest: Consultation.ConsultationData, callback: Consultation.CreateConsultationCallback) {
            val params = mutableMapOf(
                "question" to createConsultationRequest.question,
                "medium" to createConsultationRequest.medium,
                "user_id" to createConsultationRequest.userID,
//                "media_ids" to  createConsultationRequest.mediaIDs
            )

//            params["media_ids"] = createConsultationRequest.mediaIDs

            createConsultationRequest.followUpId?.let {
                params["parent_consultation_id"] = it
            }

            createConsultationRequest.mediaIDs?.let { mediaIDs ->
                val mediaIDsString = "[\"${mediaIDs.joinToString("\",\"")}\"]"
                params["media_ids"] = mediaIDsString
            }
            println("params in createConsultation is - > $params")
            NetworkRequest.ApiManager.postRequest("consultations?expand=$expandValues",
                params, null, object : NetworkRequest.ApiManager.ApiCallback<Consultation.ConsultationResponse, Any>{
                    override fun onSuccess(response: Consultation.ConsultationResponse) {
                        println("create consultation success response $response")
                        callback.onSuccess(response)
                    }

                    override fun onError(error: Any) {
                        println("create consultation onError response $error")
                        callback.onError(error)
                    }
                }, Consultation.ConsultationResponse::class.java)
        }


        fun getPrescription(consultationId: String, context: Context, callback: Consultation.DownloadPrescriptionCallback) {
            val params = null
            NetworkRequest.ApiManager.getRequest("consultations/$consultationId/download-prescription",
                params, true, object : NetworkRequest.ApiManager.ApiCallback<Any, Any> {
                    override fun onSuccess(response: Any) {
//                    callback.onSuccess(response)
                    }

                    override fun onError(error: Any) {
//                    callback.onError(error)

                    }
                }, Any::class.java
            )
        }

        fun cancelConsultation(consultationId: String, callback: Consultation.CancelConsultationCallBack){
            val params = null
            NetworkRequest.ApiManager.postRequest("consultations/$consultationId/cancel",
                params, null, object : NetworkRequest.ApiManager.ApiCallback<Consultation.CancelConsultationResponse, Any>{
                    override fun onSuccess(response: Consultation.CancelConsultationResponse) {
                        callback.onSuccess(response)
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                }, Consultation.CancelConsultationResponse::class.java)
        }


        fun getConsultation(consultationId: Any, callback: Consultation.GetConsultationByIdCallBack){
            val params = null
            NetworkRequest.ApiManager.getRequest("consultations/${consultationId}?expand=$expandValues",
                params, false, object : NetworkRequest.ApiManager.ApiCallback<Consultation.GetConsultationByIdResponse, Any>{
                    override fun onSuccess(response: Consultation.GetConsultationByIdResponse) {
                        callback.onSuccess(response)
                    }

                    override fun onError(error: Any) {
                        if(error is Consultation.ConsultationNotFound){
                            callback.onErrorObj(error)
                        } else{
                            callback.onError(error)
                        }
                    }
                }, Consultation.GetConsultationByIdResponse::class.java)
        }


        fun getLastConsultation(callback: Consultation.GetLastConsultationCallback){
            val params = mapOf<String, Any>(
                "per-page" to 1,
                "sort" to "-id"
            )
            NetworkRequest.ApiManager.getRequest("consultations?expand=$expandValues",
                params, false, object : NetworkRequest.ApiManager.ApiCallback<Any, Any>{
                    override fun onSuccess(response: Any) {
                        try {
                            val gson = Gson()
                            val jsonResponse = gson.toJson(response)
                            val listType = object : TypeToken<List<Consultation.ConsultationResponse>>() {}.type
                            val responseObject: List<Consultation.ConsultationResponse>? = try {
                                gson.fromJson(jsonResponse, listType)
                            } catch (e: JsonSyntaxException) {
                                e.printStackTrace()
                                null
                            }

                            if (responseObject != null && responseObject.isNotEmpty()) {
                                val firstConsultation: Consultation.ConsultationResponse = responseObject[0]
                                callback.onSuccess(firstConsultation)
                            } else {
                                println("Failed to parse JSON response or empty list.")
                            }

                        }catch (e: Exception){

                        }
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                }, Any::class.java)
        }


        fun getConsultationList(callback: Consultation.GetConsultationListCallBack){
            val params = null
            NetworkRequest.ApiManager.getRequest("consultations?expand=$expandValues",
                params ,false, object : NetworkRequest.ApiManager.ApiCallback<Any, Any>{
                    override fun onSuccess(response: Any) {
                        try {
                            val gson = Gson()
                            val jsonResponse = gson.toJson(response)
                            val listType = object : TypeToken<List<Consultation.ConsultationResponse>>() {}.type
                            val responseObject: List<Consultation.ConsultationResponse>? = try {
                                gson.fromJson(jsonResponse, listType)
                            } catch (e: JsonSyntaxException) {
                                e.printStackTrace()
                                null
                            }
                            if (responseObject != null) {
                                callback.onSuccess(responseObject)
                            } else {
                                println("Failed to parse JSON response.")
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                },  Any::class.java)
        }



        fun deleteConsultation(consultationId: String?, callback: Consultation.DeleteConsultationCallBack){
            val params = null
            NetworkRequest.ApiManager.deleteRequest("consultations/$consultationId",
                params ,object : NetworkRequest.ApiManager.ApiCallback<Any, Any>{
                    override fun onSuccess(response: Any) {
                        callback.onSuccess("success")
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                },Any::class.java)
        }


        fun uploadMedia(file: File?, callback: Consultation.UploadCallback) {
            val params = null
            NetworkRequest.ApiManager.postRequest("media",
                params, file ,object : NetworkRequest.ApiManager.ApiCallback<Consultation.UploadMediaResponse, Any>{
                    override fun onSuccess(response: Consultation.UploadMediaResponse) {
                        callback.onSuccess(response)
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                }, Consultation.UploadMediaResponse::class.java)
        }

        fun getUser(phrId: String, callback: User.GetUserCallback) {
            val params = null
            NetworkRequest.ApiManager.getRequest("users/$phrId",
                params, false, object : NetworkRequest.ApiManager.ApiCallback<User.UserResponse, Any>{
                    override fun onSuccess(response: User.UserResponse) {
                        callback.onSuccess(response)
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                }, User.UserResponse::class.java)
        }

        fun deleteUser(id: String?, callback: User.DeleteUserCallBack){
            val params = null
            NetworkRequest.ApiManager.deleteRequest("users/$id",
                params ,object : NetworkRequest.ApiManager.ApiCallback<Any, Any>{
                    override fun onSuccess(response: Any) {
                        callback.onSuccess("success")
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                },Any::class.java)
        }

        fun getUsers(callback: User.GetUsersCallback) {
            val params = null
            NetworkRequest.ApiManager.getRequest("users",
                params ,false ,object : NetworkRequest.ApiManager.ApiCallback<Any, Any>{
                    override fun onSuccess(response: Any) {
                        try {
                            val gson = Gson()
                            val jsonResponse = gson.toJson(response)
                            val listType = object : TypeToken<List<User.UserResponse>>() {}.type
                            val responseObject: List<User.UserResponse>? = try {
                                gson.fromJson(jsonResponse, listType)
                            } catch (e: JsonSyntaxException) {
                                e.printStackTrace()
                                null
                            }
                            if (responseObject != null) {
                                callback.onSuccess(responseObject)
                            } else {
                                println("Failed to parse JSON response.")
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                },  Any::class.java )
        }

        fun createUser(userParams: User.CreateUserData, callback: User.CreateUserCallBack){
            val params = mutableMapOf<String, Any?>()

            params.putAll(userParams.extractNonNullValues())
            val filteredParams = params.filterValues { it != null }
            NetworkRequest.ApiManager.postRequest("users",
                filteredParams,null, object : NetworkRequest.ApiManager.ApiCallback<User.UserResponse, Any>{
                    override fun onSuccess(response: User.UserResponse) {
                        callback.onSuccess(response)
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                }, User.UserResponse::class.java)
        }

        fun updateUser(userParams: User.UpdateUserData, callback: User.UpdateUserCallback){
            val params = mutableMapOf<String, Any?>()
            params.putAll(userParams.extractNonNullValues())
            val filteredParams = params.filterValues { it != null }
            NetworkRequest.ApiManager.putRequest("users/${userParams.id}",
                filteredParams, object : NetworkRequest.ApiManager.ApiCallback<User.UserResponse, Any>{
                    override fun onSuccess(response: User.UserResponse) {
                        callback.onSuccess(response)
                    }

                    override fun onError(error: Any) {
                        callback.onError(error)
                    }
                }, User.UserResponse::class.java)
        }
    }

}
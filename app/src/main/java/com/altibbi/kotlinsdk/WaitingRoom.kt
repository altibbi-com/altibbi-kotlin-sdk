package com.altibbi.kotlinsdk

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.Consultation
import com.altibbi.telehealth.TBISocket
import com.altibbi.telehealth.TBISocketEventListener
import com.altibbi.telehealth.TBISubscribeEventListener
import org.json.JSONObject

class WaitingRoom : AppCompatActivity() {
    val socket = TBISocket();

    var currentConsultation: Consultation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)

        val cancelConsultation = findViewById<Button>(R.id.button16);

        cancelConsultation.setOnClickListener {
            cancelConsultation(currentConsultation?.id.toString())
        }

        getConsultation(applicationContext)
    }

    private fun getConsultation(context: Context){
        ApiService.getLastConsultation(object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                currentConsultation = response
                if(response.status == "in_progress"){
                    if(response.videoConfig != null){
                        val intent = Intent(applicationContext, Video::class.java)
                        intent.putExtra("apiKey",response.videoConfig?.apiKey)
                        intent.putExtra("callId",response.videoConfig?.callId)
                        intent.putExtra("token",response.videoConfig?.token)
                        startActivity(intent)
                    }
                    if(response.voipConfig != null){
                        val intent = Intent(applicationContext, Video::class.java)
                        intent.putExtra("apiKey",response.videoConfig?.apiKey)
                        intent.putExtra("callId",response.videoConfig?.callId)
                        intent.putExtra("token",response.videoConfig?.token)
                        intent.putExtra("voip",true)
                        startActivity(intent)
                    }
                    if (response.chatConfig != null){
                        val intent = Intent(context, Chat::class.java)
                        val bundle = Bundle()
                        bundle.putString("consultationId", response.id.toString())
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                } else{
                    initSocket(response, context)
                }

            }

            override fun onFailure(error: String?) {
                println("onFailure $error")
            }

            override fun onRequestError(error: String?) {
                println("onError $error")
            }


        })
    }

    private fun initSocket(response: Consultation, context: Context){
        socket.init(
            channelName = response.socketChannel!!,
            appKey = response.appKey!!,
            connectionCallback = object : TBISocketEventListener {
                override fun onConnectionStateChange(
                    previousState: String?,
                    currentState: String?
                ) {
                    if(currentState == "CONNECTED"){
                        socket.subscribe("call-status", object : TBISubscribeEventListener {
                            override fun onEvent(event: JSONObject) {
                                val status = event.getString("status")
                                if (status == "in_progress"){
                                    ApiService.getLastConsultation(object : ApiCallback<Consultation> {
                                        override fun onSuccess(response: Consultation) {
                                            println("getLastConsultation response is -> $response")
                                            if(response.videoConfig != null){
                                                val intent = Intent(applicationContext, Video::class.java)
                                                intent.putExtra("apiKey",response.videoConfig?.apiKey)
                                                intent.putExtra("callId",response.videoConfig?.callId)
                                                intent.putExtra("token",response.videoConfig?.token)
                                                startActivity(intent)
                                            }
                                            if(response.voipConfig != null){
                                                val intent = Intent(applicationContext, Video::class.java)
                                                intent.putExtra("apiKey",response.videoConfig?.apiKey)
                                                intent.putExtra("callId",response.videoConfig?.callId)
                                                intent.putExtra("token",response.videoConfig?.token)
                                                intent.putExtra("voip",true)
                                                startActivity(intent)
                                            }
                                            if (response.chatConfig != null){
                                                val intent = Intent(context, Chat::class.java)
                                                val bundle = Bundle()
                                                bundle.putString("consultationId", response.id.toString())
                                                intent.putExtras(bundle)
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
                                }else if (status == "closed"){
                                    finish()
                                }
                            }
                            override fun onAuthenticationFailure(
                                message: String?,
                                e: Exception?
                            ) {
                                print("onAuthenticationFailure $message")
                            }
                            override fun onSubscriptionSucceeded(channelName: String) {
                                println("onSubscriptionSucceeded 1$channelName")
                            }
                        })
                    }
                }
                override fun onError(
                    message: String,
                    code: String?,
                    e: Exception?
                ) {
                }
            },
            subscribeCallback = object : TBISubscribeEventListener {
                override fun onEvent(event: JSONObject) {
                }
                override fun onAuthenticationFailure(
                    message: String?,
                    e: Exception?
                ) {
                }
                override fun onSubscriptionSucceeded(channelName: String) {
                }
            }
        )


    }

    private fun cancelConsultation(id: String){

        ApiService.cancelConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) {
                if (response){
                    finish()
                }
            }

            override fun onFailure(error: String?) {
                println("onFailure $error")
            }

            override fun onRequestError(error: String?) {
                println("onError $error")
            }


        })
    }

}



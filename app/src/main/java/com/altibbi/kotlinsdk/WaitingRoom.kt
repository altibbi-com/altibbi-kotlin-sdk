package com.altibbi.kotlinsdk

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.Consultation
import com.altibbi.telehealth.TBISocket

class WaitingRoom : AppCompatActivity() {
    private val tbiSocket = TBISocket()

    var currentConsultation: Consultation.ConsultationResponse? = null
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
        ApiService.getLastConsultation(object : Consultation.GetLastConsultationCallback{
            override fun onSuccess(response: Consultation.ConsultationResponse) {
                println("getLastConsultation response in WAITINGROOM -> $response")
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
                        println("before call sendbird 1")
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

            override fun onError(error: Any) {
            }

        }) }

    private fun initSocket(response: Consultation.ConsultationResponse, context: Context){
        val pusherData = TBISocket.PusherParams(
            pusherAppKey = response.pusherAppKey,
            pusherChannel = response.pusherChannel
        )

        tbiSocket.initiateSocket(pusherData, object : TBISocket.InitiateSocketCallBack{
            override fun onConnect(status: String) {
                println("onConnect status -> $status")
                println("create con response is -> $response")
            }

            override fun onError(status: String) {
                finish()
            }

            override fun onStatusChange(status: String) {
                println("status in onStatusChange is -> $status")
                println("response is -> $response")
                if(status == "in_progress") {
                    ApiService.getConsultation(response.id, object : Consultation.GetConsultationByIdCallBack{
                        override fun onSuccess(response: Consultation.GetConsultationByIdResponse) {
                            if(response is Consultation.GetConsultationByIdResponse){
                                println("GetConsultationByIdResponse all data is -> $response")
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
                                    println("before call sendbird 1")
                                    val intent = Intent(context, Chat::class.java)
                                    val bundle = Bundle()
                                    bundle.putString("consultationId", response.id.toString())
                                    intent.putExtras(bundle)
                                    startActivity(intent)
                                }
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
                } else if (status == "closed"){
                    println("the status is closed make an action")
                                    finish()
                }

            }
        })

    }

    private fun cancelConsultation(id: String){
        ApiService.cancelConsultation(
            id,
            object : Consultation.CancelConsultationCallBack{
                override fun onSuccess(response: Consultation.CancelConsultationResponse){
                    println("Cancel Consultation Response not all data -> $response")
                    if(response is Consultation.CancelConsultationResponse){
                        println("Cancel Consultation Response all data is -> $response")
                        finish()
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


}



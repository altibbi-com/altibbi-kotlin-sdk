package com.example.altibbi

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpChannelAuthorizer
import org.json.JSONObject
class TBISocket : Service() {
    private var pusher: Pusher? = null
    override fun onBind(intent: Intent): IBinder? {
        return  null
    }
    data class PusherParams(
        val pusherAppKey: String,
        val pusherChannel: String,
    )

    interface InitiateSocketCallBack {
        fun onConnect(status: String)
        fun onStatusChange(status: String)
    }

    fun initiateSocket(params: PusherParams, callBack: InitiateSocketCallBack){
        println("params in initiateSocket is -> $params")
        val channelAuthorizer = HttpChannelAuthorizer("${Constants.ENDPOINT}auth/pusher?access-token=${Constants.AUTH}")
        val options = PusherOptions().setCluster("eu").setChannelAuthorizer(channelAuthorizer)
         pusher = Pusher(params.pusherAppKey, options)

        pusher!!.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                println("Connection state changed to ${change.currentState}")
                if (change.currentState == ConnectionState.CONNECTED) {
                    // Handle successful connection
                    callBack.onConnect("connected")
                    println("im in this if change.currentState == ConnectionState.CONNECTED")
                }
            }

            override fun onError(message: String, code: String, e: Exception) {
                println("Error: $message")
                e.printStackTrace()
            }
        },ConnectionState.ALL)

        println("before declare channel")
        val channel: PrivateChannel = pusher!!.subscribePrivate(params.pusherChannel, object :
            PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                println("onEvent event name 1 ${event?.data}")
            }
            override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {
                println("onAuthenticationFailure message 1 $message")
            }
            override fun onSubscriptionSucceeded(channelName: String) {}
        })
        println("channel is -> $channel")
        channel.bind("call-status", object : PrivateChannelEventListener {
            override fun onAuthenticationFailure(message: String, e: Exception) {
                println("message is -> $message")
            }
            override fun onEvent(event: PusherEvent?) {
                val json = JSONObject(event?.data.toString())
                val status = json.getString("status")
                println("status is -> $status" );
                callBack.onStatusChange(status)
            }
            override fun onSubscriptionSucceeded(channelName: String) {
                println("channelName -> $channelName")
            }
        })
    }
}
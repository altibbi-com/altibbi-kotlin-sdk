package com.altibbi.telehealth

import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpChannelAuthorizer
import org.json.JSONObject
import java.io.IOException

class TBISocket {
    private var pusher: Pusher? = null
    private var channel: PrivateChannel? = null
    fun init (
        channelName: String ,
        appKey : String,
        connectionCallback : TBISocketEventListener,
        subscribeCallback : TBISubscribeEventListener,
    ) {
        val token : String? = AltibbiService.authToken
        val url : String? = AltibbiService.url
        if(token.isNullOrEmpty()){
            throw IOException("Token is missing or invalid.")
        }
        if(url.isNullOrEmpty()){
            throw IOException("baseUrl is missing or invalid.")
        }
        if(channelName.isEmpty()){
            throw IOException("channelName is missing or invalid.")
        }
        if(appKey.isEmpty()){
            throw IOException("appKey is missing or invalid.")
        }
        val authEndPoint = "${url}/v1/auth/pusher?access-token=${token}"
        val options = PusherOptions();
        val channelAuthorizer = HttpChannelAuthorizer(authEndPoint);
        options.setCluster("eu")
        options.isUseTLS = true
        options.maxReconnectionAttempts = 10
        options.channelAuthorizer = channelAuthorizer
        pusher = Pusher(appKey, options)
        pusher!!.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                connectionCallback.onConnectionStateChange(
                    previousState = change.previousState.toString() ,
                    currentState = change.currentState.toString() ,
                )
            }
            override fun onError(message: String, code: String?, e: Exception?) {
                connectionCallback.onError(message,code,e)
            }
        })
        channel = pusher!!.subscribePrivate(channelName, object :
            PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                val json = JSONObject(event?.data.toString())
                subscribeCallback.onEvent(json)
            }
            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                subscribeCallback.onAuthenticationFailure(message,e)
            }
            override fun onSubscriptionSucceeded(channelName: String) {
                subscribeCallback.onSubscriptionSucceeded(channelName)
            }
        })
    }
    fun unsubscribe( channelName: String){
        if(pusher != null){
            pusher!!.unsubscribe(channelName)
        }
    }
    fun disconnect(){
        if(pusher != null){
            pusher!!.disconnect()
        }
    }
    fun subscribe(eventName : String , subscribeCallback : TBISubscribeEventListener){
        channel?.bind(eventName, object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                val json = JSONObject(event?.data.toString())
                val status = json.getString("status")
                subscribeCallback.onEvent(json)
            }
            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                subscribeCallback.onAuthenticationFailure(message,e)
            }
            override fun onSubscriptionSucceeded(channelName: String) {
                subscribeCallback.onSubscriptionSucceeded(channelName)
            }
        })
    }
}
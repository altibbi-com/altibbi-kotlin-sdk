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

    companion object {
        private const val TAG = "TBISocket"
    }

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
        AltibbiService.log(TAG,"init — channel=$channelName")
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
                AltibbiService.log(TAG,"connectionState — ${change.previousState} → ${change.currentState}")
                connectionCallback.onConnectionStateChange(
                    previousState = change.previousState.toString() ,
                    currentState = change.currentState.toString() ,
                )
            }
            override fun onError(message: String, code: String?, e: Exception?) {
                AltibbiService.logError(TAG,"connectionError — code=$code message=$message", e)
                connectionCallback.onError(message,code,e)
            }
        })
        channel = pusher!!.subscribePrivate(channelName, object :
            PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                AltibbiService.log(TAG,"channelEvent — data=${event?.data}")
                val json = JSONObject(event?.data.toString())
                subscribeCallback.onEvent(json)
            }
            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                AltibbiService.logError(TAG,"channelAuthFailure — $message", e)
                subscribeCallback.onAuthenticationFailure(message,e)
            }
            override fun onSubscriptionSucceeded(channelName: String) {
                AltibbiService.log(TAG,"channelSubscribed — $channelName")
                subscribeCallback.onSubscriptionSucceeded(channelName)
            }
        })
    }
    fun unsubscribe( channelName: String){
        AltibbiService.log(TAG,"unsubscribe — $channelName")
        if(pusher != null){
            pusher!!.unsubscribe(channelName)
        }
    }
    fun disconnect(){
        AltibbiService.log(TAG,"disconnect")
        if(pusher != null){
            pusher!!.disconnect()
        }
    }
    fun subscribe(eventName : String , subscribeCallback : TBISubscribeEventListener){
        AltibbiService.log(TAG,"subscribe — event=$eventName")
        channel?.bind(eventName, object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                AltibbiService.log(TAG,"event[$eventName] — data=${event?.data}")
                val json = JSONObject(event?.data.toString())
                val status = json.getString("status")
                subscribeCallback.onEvent(json)
            }
            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                AltibbiService.logError(TAG,"event[$eventName] authFailure — $message", e)
                subscribeCallback.onAuthenticationFailure(message,e)
            }
            override fun onSubscriptionSucceeded(channelName: String) {
                AltibbiService.log(TAG,"event[$eventName] subscriptionSucceeded — $channelName")
                subscribeCallback.onSubscriptionSucceeded(channelName)
            }
        })
    }
}
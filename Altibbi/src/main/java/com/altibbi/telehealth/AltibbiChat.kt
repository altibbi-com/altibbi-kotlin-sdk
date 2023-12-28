package com.altibbi.telehealth

import android.content.Context
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import com.sendbird.android.SendBird.ChannelHandler
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.SendBirdException
import com.sendbird.android.handlers.InitResultHandler

class AltibbiChat {
    companion object{
        fun init(
    appId: String,
    context: Context,
    userId: String,
    accessToken: String,
) {
            SendBird.init(appId, context, false, object : InitResultHandler {
                override fun onMigrationStarted() {
                }

                override fun onInitFailed(e: SendBirdException) {
                    // This won't be called if useLocalCaching is set to false.
                    println("onInitFailed erroe $e")
                }

                override fun onInitSucceed() {
                    println("Called when initialization is completed")


                    println("userId is ->$userId")
                    println("accessToken is ->$accessToken")
                    SendBird.connect(userId, accessToken, object : ConnectHandler {
                        override fun onConnected(p0: com.sendbird.android.User?, p1: SendBirdException?) {
                            println("The user is connected to Sendbird server.")
                        }
                    })

                }
            })
        }

        interface ChannelCallback {
            fun onChannelReceived(channel: GroupChannel?)
        }

        fun getChannel(channelName: String, callback: ChannelCallback) {
            GroupChannel.getChannel(channelName) { groupChannel, error ->
                if (error == null) {
                    callback.onChannelReceived(groupChannel)
            } } }

        fun addChannelHandler(identifier: String, handler: ChannelHandler){
            SendBird.addChannelHandler(identifier,handler)
        }

    }}
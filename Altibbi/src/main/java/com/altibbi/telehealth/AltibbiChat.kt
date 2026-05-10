package com.altibbi.telehealth

import android.content.Context
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import com.sendbird.android.SendBird.ChannelHandler
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.SendBirdException
import com.sendbird.android.handlers.InitResultHandler

class AltibbiChat {
    companion object {
        private const val TAG = "AltibbiChat"

        fun init(
            appId: String,
            context: Context,
            userId: String,
            accessToken: String,
            onConnected: (() -> Unit)? = null,
        ) {
            AltibbiService.log(TAG,"init — appId=$appId userId=$userId")
            SendBird.init(appId, context, false, object : InitResultHandler {
                override fun onMigrationStarted() {
                    AltibbiService.log(TAG,"onMigrationStarted")
                }

                override fun onInitFailed(e: SendBirdException) {
                    AltibbiService.logError(TAG,"onInitFailed: $e")
                }

                override fun onInitSucceed() {
                    AltibbiService.log(TAG,"onInitSucceed — connecting userId=$userId")
                    SendBird.connect(userId, accessToken, object : ConnectHandler {
                        override fun onConnected(user: com.sendbird.android.User?, e: SendBirdException?) {
                            if (e != null) {
                                AltibbiService.logError(TAG,"connect failed: $e")
                            } else {
                                AltibbiService.log(TAG,"connected — userId=${user?.userId}")
                                onConnected?.invoke()
                            }
                        }
                    })
                }
            })
        }

        interface ChannelCallback {
            fun onChannelReceived(channel: GroupChannel?)
        }

        fun getChannel(channelName: String, callback: ChannelCallback) {
            AltibbiService.log(TAG,"getChannel — channelName=$channelName")
            GroupChannel.getChannel(channelName) { groupChannel, error ->
                if (error != null) {
                    AltibbiService.logError(TAG,"getChannel failed: $error")
                    callback.onChannelReceived(null)
                } else {
                    AltibbiService.log(TAG,"getChannel success — channelUrl=${groupChannel?.url}")
                    callback.onChannelReceived(groupChannel)
                }
            }
        }

        fun addChannelHandler(identifier: String, handler: ChannelHandler) {
            SendBird.addChannelHandler(identifier, handler)
        }
    }
}
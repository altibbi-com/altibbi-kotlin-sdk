package com.altibbi.kotlinsdk

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.altibbi.telehealth.AltibbiChat
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.Consultation
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.PreviousMessageListQuery.MessageListQueryResult
import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdException
import com.sendbird.android.User
import com.sendbird.android.UserMessage
class Chat : AppCompatActivity() {
    var currentChannel: GroupChannel? = null
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private val apiService = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val bundle = intent.extras
        val consultationId = bundle?.getString("consultationId")
        val buttonSendMessage = findViewById<Button>(R.id.buttonSendMessage1)
        val cancelConsultationButton = findViewById<Button>(R.id.button17)
        val messageInput: EditText = findViewById(R.id.messageInput)


        messageAdapter = MessageAdapter{scrollToLastMessage()}
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (consultationId != null) {
            getConsultation(this, consultationId)
            cancelConsultationButton.setOnClickListener{
                cancelConsultation(consultationId)
            }
        }

        buttonSendMessage.setOnClickListener {
            val message = messageInput.text.toString()
            if (message != null){
                currentChannel?.sendUserMessage(message, object : BaseChannel.SendUserMessageHandler {
                    override fun onSent(userMessage: UserMessage?, e: SendBirdException?) {
                        if (e == null) {
                            userMessage?.let { messageAdapter.addMessage(it) }
                            messageInput.text.clear()
                            scrollToLastMessage()
                        } else {
                            println("Error sending message: ${e.message}")
                        }
                    }
                })
            }
        }

        val channelHandler = MyChannelHandler(
            activity = this,
            onChannelMessageReceived = { message: BaseMessage ->
                runOnUiThread {
                    messageAdapter.addMessage(message)
                    scrollToLastMessage()
                }
            }
        )
        AltibbiChat.addChannelHandler("myChannelHandler",channelHandler)
    }


    override fun onStart() {
        super.onStart()

        val channelHandler = MyChannelHandler(
            activity = this,
            onChannelMessageReceived = { message: BaseMessage ->
                runOnUiThread {
                    messageAdapter.addMessage(message)
                    scrollToLastMessage()
                }
            }
        )
        AltibbiChat.addChannelHandler("myChannelHandler",channelHandler)
    }

//    override fun onResume() {
//        super.onResume()
//
//        val channelHandler = MyChannelHandler(
//            activity = this,
//            onChannelMessageReceived = { message: BaseMessage ->
//                runOnUiThread {
//                    messageAdapter.addMessage(message)
//                    scrollToLastMessage()
//                }
//            }
//        )
//        AltibbiChat.addChannelHandler("myChannelHandler",channelHandler)
//    }

    class MessageAdapter(private val scrollToLastMessage: () -> Unit) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
        private val messages: MutableList<BaseMessage> = mutableListOf()

        fun addMessage(message: BaseMessage) {
            messages.add(message)
            notifyItemInserted(messages.size - 1)
        }

        fun addMessages(newMessages: List<BaseMessage>) {
            messages.addAll(newMessages)
            notifyDataSetChanged()
            scrollToLastMessage()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.bind(messages[position])
        }

        override fun getItemCount(): Int = messages.size

        inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(message: BaseMessage) {
                val messageText: TextView = itemView.findViewById(R.id.messageText)
                val isCurrentUserMessage = message.sender.userId == SendBird.getCurrentUser().userId

                val gravity = if (isCurrentUserMessage) Gravity.END else Gravity.START
                messageText.gravity = gravity

                when (message) {
                    is UserMessage -> messageText.text = message.message
                    // Handle other message types if needed
                }
            }
        }
    }

    class MyChannelHandler(
        private val activity: Activity,
        val onChannelMessageReceived: (BaseMessage) -> Unit
    ) : SendBird.ChannelHandler() {

        override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
            if (message.message.isNotEmpty()) {
                onChannelMessageReceived(message)
            }
        }
        override fun onTypingStatusUpdated(channel: GroupChannel?) {
            println("typing started from Dr side")
        }
        override fun onUserLeft(channel: GroupChannel?, user: User?) {
            println("Chat finished")
            activity.finish()
        }
    }

    private fun getConsultation(context: Context, consId: String) {

        apiService.getConsultationInfo(consId, object : ApiCallback<Consultation> {
            override fun onSuccess(response: Consultation) {
                println("get consultation info response is -> $response")
                response.chatConfig?.appId?.let { response.chatConfig!!.chatUserId?.let { it1 ->
                    AltibbiChat.init(it, context, it1, response.chatConfig!!.chatUserToken!!)
                    AltibbiChat.getChannel("channel_${response.chatConfig!!.groupId}", object :
                        AltibbiChat.Companion.ChannelCallback {
                        override fun onChannelReceived(channel: GroupChannel?) {
                            currentChannel = channel
                            currentChannel?.createPreviousMessageListQuery()?.load(100, false, object : MessageListQueryResult{
                                override fun onResult(
                                    p0: MutableList<BaseMessage>?,
                                    p1: SendBirdException?
                                ) {
                                    p0?.let { messageAdapter.addMessages(it) }
                                }
                            })
                        }
                    })

                } }
            }

            override fun onFailure(error: String?) {
                println(error)
            }

            override fun onRequestError(error: String?) {
                println(error)
            }


        })
    }

    private fun scrollToLastMessage() {
        val lastItemPosition = messageAdapter.itemCount - 1
        if (lastItemPosition >= 0) {
            recyclerView.scrollToPosition(lastItemPosition)
        }
    }

    private fun cancelConsultation(id: String){

        apiService.cancelConsultation(id, object : ApiCallback<Boolean> {
            override fun onSuccess(response: Boolean) {
                println("cancelConsultation response : $response")
                if (response){
                    finish()
                }
            }
            override fun onFailure(error: String?) {
                println(error)
            }
            override fun onRequestError(error: String?) {
                println(error)
            }
        })
    }
}


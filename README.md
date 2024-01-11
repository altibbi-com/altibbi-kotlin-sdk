<img src="https://cdn.altibbi.com/theme/altibbi/icons/tbi-brand.svg" width="50%" height="50%">

# Altibbi Android SDK

This Android SDK provides integration for the Altibbi services, including video consultation, text consultation, push
notification, and many other features. This guide will walk you through the steps to integrate it into your Android
project.

## Features
- **Video and VOIP Consultation:** Facilitate live video and VOIP sessions between patients and healthcare professionals.
- **GSM Consultation:** Facilitate GSM(Phone calls) sessions between patients and healthcare professionals.
- **Text Consultation:** Offer real-time text messaging for healthcare inquiries.
- **User Management:** Easily manage user information with our comprehensive API.
- **Real-time Notifications:** Keep users updated with push notifications and server to server real time callbacks.


## Installation
Install the SDK :

```sh
implementation("com.altibbi.telehealth:AltibbiTelehealth:0.1.1")
```

## Initialization
Initialize the Altibbi SDK with the following parameters:
- **PARTNER_ENDPOINT:** Your partner endpoint (will be shared with you upon request).
- **token:** Authentication token from your backend.
- **language:** Preferred language for responses either Arabic (default) or English.

```kotlin
AltibbiService.init(
    token = "USER_TOKEN",
    baseUrl = "PARTNER_ENDPOINT",
    language = "Language" // 'ar' || 'en'
)
```


## Usage


### After Initialize Altibbi Service You Can Use Altibbi API :
#### Using the API Service:
```kotlin
val apiService = ApiService()
```

### User API
Manage users with functions like `createUser`, `updateUser`,`getUser`, `getUsers`, and `deleteUser`. Below are examples of how to use these functions:


### USER API

| APi        | params             |
|------------|--------------------|
| getUser    | USER_ID (required) |
| getUsers   | page , perPage     |
| createUser | user data          |
| updateUser | userId             |
| deleteUser | userId             |


#### Create New User :

You Have To Pass User Object

```kotlin
val user =  User(name = "user_name", email = "example@gmail.com")
apiService.createUser(user, object : ApiCallback<User> {
    override fun onSuccess(response: User) {
    }

    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }
})
```

#### Get User Info By ID :

```kotlin
apiService.getUser(userId, object : ApiCallback<User> {
    override fun onSuccess(response: User) {
        // Response is User info
    }

    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }
})
```

#### Users List Related To Member :

you can pass page && perPage defaults 1 && 20

```kotlin
apiService.getUsers(object : ApiCallback<List<User>> {
    override fun onSuccess(response: List<User>) {
        // Response is Users list
    }
    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }

})

```

#### Update User Info :

```kotlin
val user =  User(name = "user_name", email = "example@gmail.com", id = 1)
apiService.updateUser(user,user.id,object : ApiCallback<User> {
    override fun onSuccess(response: User) {
    }

    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }
})

```

#### Delete User :

```kotlin
 apiService.deleteUser(id, object : ApiCallback<Boolean> {
    override fun onSuccess(response: Boolean) {
        // Response = True means User is deleted 
    }

    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }

})
```

### Consultation API
Create and manage consultations using our suite of functions:


| APi                 | params                                                                               |
|---------------------|--------------------------------------------------------------------------------------|
| createConsultation  | question (required)  , medium (required) , userId (required) , mediaIds , followUpId |
| getConsultationInfo | consultationId                                                                       |
| getLastConsultation |                                                                                      |
| getConsultationList | userId (required), page, perPage                                                     |
| deleteConsultation  | consultationId                                                                       |
| cancelConsultation  | consultationId                                                                       |


#### Create Consultation :

```kotlin
apiService.createConsultation(
    question = "YOUR QUESTION",
    medium = Medium.chat, // chat, voip, video, gsm
    userID = Int, 
    mediaIDs =  null ,
    followUpId = String,
    object : ApiCallback<Consultation> {
        override fun onSuccess(response: Consultation) {
        }

        override fun onFailure(error: String?) {
        }

        override fun onRequestError(error: String?) {
        }
    }
)
```

#### Note That You Can Pass "followUpId" In Case Consultation Is FollowUpConsultation

#### Consultation List:

you can pass page && perPage defaults 1 && 20

```kotlin
 val consultationCallback = object : ApiCallback<List<Consultation>> {
    override fun onSuccess(response: List<Consultation>) {
    }

    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }
 }
apiService.getConsultationList(callback = consultationCallback)

```

#### Consultation Info By ID :

```kotlin
apiService.getConsultationInfo(id, object : ApiCallback<Consultation> {
    override fun onSuccess(response: Consultation) {
        // Response is the Consultation info
    }

    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }
})

```

#### Last Consultation Info :

```kotlin
apiService.getLastConsultation(object : ApiCallback<Consultation> {
    override fun onSuccess(response: Consultation) {
        // Response is the last consultation info
    }

    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }


})

```

#### Delete Consultation :

```kotlin
apiService.deleteConsultation(id, object : ApiCallback<Boolean> {
    override fun onSuccess(response: Boolean) {
        // Response = True means Consultation is deleted
    }

    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }
})

```

#### Cancel Consultation :

```kotlin
apiService.cancelConsultation(id, object : ApiCallback<Boolean> {
    override fun onSuccess(response: Boolean) {
        // Response = True means Consultation is canceled
    }

    override fun onFailure(error: String?) {
    }

    override fun onRequestError(error: String?) {
    }
})

```

### Prescription API

#### download the Prescription 
note : if the prescription for the consultation is generated it will return else it will be null 

```kotlin
apiService.getPrescription(id, object : ApiCallback<Response> {
    override fun onSuccess(response: Response) {
        val inputStream = response.body?.byteStream()
        if (inputStream != null) {
            // if its not equal null you can save the PDF to your files 
        }
    }
    override fun onFailure(error: String?) {
    }
    override fun onRequestError(error: String?) {
    }
})

```

### Media API

#### Upload Media That Can Be Used In Consultation (Image , PDF)

```kotlin
apiService.uploadMedia(imageFile, object : ApiCallback<Media> {
    override fun onSuccess(response: Media) {
    }
    override fun onFailure(error: String?) {
    }
    override fun onRequestError(error: String?) {
    }
})

```
### Use Pusher Service To Listen To Consultation Event

#### Initializing the Pusher Service:

```kotlin
socket.init(
    channelName = response.socketChannel!!,
    appKey = response.appKey!!,
    connectionCallback = object : TBISocketEventListener {
        override fun onConnectionStateChange(
            previousState: String?,
            currentState: String?
        ) {
            if(currentState == "CONNECTED"){
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

```

#### You Can Listen To Event Using Funtion "onEvent" Passed In Pusher().init :

```kotlin
socket.subscribe("call-status", object : TBISubscribeEventListener { // call-status its the Event name 
    override fun onEvent(event: JSONObject) {
        val status = event.getString("status")
        if (status == "in_progress"){
        }else if (status == "closed"){
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

```

### Initializing the Video Service && Granted Permission :

```kotlin
    private fun checkAndRequestPermissions() {
    val permissionsToRequest = mutableListOf<String>()

    // Check for Internet permission
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissionsToRequest.add(Manifest.permission.INTERNET)
    }

    // Check for Camera permission
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissionsToRequest.add(Manifest.permission.CAMERA)
    }

    // Check for Record Audio permission
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
    }

    // Request permissions if needed
    if (permissionsToRequest.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            this,
            permissionsToRequest.toTypedArray(),
            PERMISSIONS_REQUEST_CODE
        )
    } else {
        initializeSession(apiKey!!, callId!!, token!!)
    }

    private fun initializeSession(apiKey: String, sessionId: String, token: String) {
        session = Session.Builder(this, apiKey, sessionId).build().also {
            it.setSessionListener(sessionListener)
            it.connect(token)
        }
    }

}
```

### Displaying Altibbi Video:
Use VideoView Widget
```kotlin
val intent = Intent(applicationContext, Video::class.java)
intent.putExtra("apiKey",response.videoConfig?.apiKey)
intent.putExtra("callId",response.videoConfig?.callId)
intent.putExtra("token",response.videoConfig?.token)
intent.putExtra("voip",true)
startActivity(intent)
```


### Initializing Chat:
Use AltibbiChat Widget
```kotlin
AltibbiChat.init(response.chatConfig!!.appId!!, context, response.chatConfig!!.chatUserId!!, response.chatConfig!!.chatUserToken!!)
```


### Chat listeners:
Assigning Chat listeners
```kotlin
AltibbiChat.addChannelHandler("myChannelHandler",channelHandler)

```


### Sending Chat message:
Sending a text Chat
```kotlin
AltibbiChat.getChannel("channel_${response.chatConfig!!.groupId}", object :
    AltibbiChat.Companion.ChannelCallback {
    override fun onChannelReceived(channel: GroupChannel?) {
        currentChannel = channel
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
})
```


## Example

An example Android application using the Altibbi SDK can be found in the `example` directory.

Please see the `example` directory for a complete sample app using Altibbi Android SDK.
```kotlin
1- Creating a consultation page
2- Waiting room page
3- Chat page page
4- Video conference page
5- VOIP conference page
6- Creating user page
```



## Support

If you need support you can contact: [mobile@altibbi.com](mobile@altibbi.com). Please
ensure that you are referencing the latest version of our SDK to access all available features and improvements.

## License

The Altibbi Android SDK is licensed under the [Altibbi License](#https://altibbi.com/).

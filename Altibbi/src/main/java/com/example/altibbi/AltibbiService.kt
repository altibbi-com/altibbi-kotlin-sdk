package com.example.altibbi

class AltibbiService {
    companion object{
        fun init (endPoint: String, token: String, lang: String){
            Constants.ENDPOINT = endPoint
            Constants.AUTH = token
            Constants.LANG = lang
        }
    }
}
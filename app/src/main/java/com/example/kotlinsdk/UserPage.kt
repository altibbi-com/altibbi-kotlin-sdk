package com.example.kotlinsdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.altibbi.User

class UserPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)
        val user = User()

        val getUserIdButton = findViewById<Button>(R.id.button4);
        val textInputEditText: EditText = findViewById(R.id.textInputEditText2)

        getUserIdButton.setOnClickListener{
            val userId = textInputEditText.text.toString()
            user.getUser(userId, object : User.GetUserCallback{
                override fun onSuccess(response: User.UserResponse) {
                    if(response is User.UserResponse){
                        println("Received response in callback getPhrById all data is: $response")
                    }
                }

                override fun onError(error: User.UserNotFound) {
                    if(error is User.UserNotFound){
                        println("Error is User.UserNotFound all data : $error")
                    }
                }
            })
        }

        val updateUserButton = findViewById<Button>(R.id.button6);
        val userName: EditText = findViewById(R.id.textInputEditText8)
        val email: EditText = findViewById(R.id.textInputEditText7)
        val phoneNumber: EditText = findViewById(R.id.textInputEditText10)
        val dateOfBirth: EditText = findViewById(R.id.textInputEditText11)
        val policyNumber: EditText = findViewById(R.id.textInputEditText12)
        val id: EditText = findViewById(R.id.textInputEditText13)

        updateUserButton.setOnClickListener{
            val userParams = mutableMapOf<String, Any>()

            phoneNumber.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["phone_number"] = it
            }

            userName.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["name"] = it
            }

            id.text.toString().takeIf { it.isNotBlank() }?.toIntOrNull()?.let {
                userParams["id"] = it
            }

            email.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["email"] = it
            }

            dateOfBirth.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["date_of_birth"] = it
            }

            policyNumber.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["policy_number"] = it
            }

            println("userParams before call api -> $userParams")

            user.updateUser(userParams, object : User.UpdateUserCallback{
                override fun onSuccess(response: User.UserResponse) {
                    if (response is User.UserResponse){
                        println("response obj is $response")
                    }
                }

                override fun onError(error: User.UserNotFound) {
                    if (error is User.UserNotFound){
                        println("error is $error")
                    }
                }
            })
        }

        val getAllUsersButton = findViewById<Button>(R.id.button10);
        getAllUsersButton.setOnClickListener{
            user.getUsers(object : User.GetUsersCallback{
                override fun onSuccess(response: List<User.UserResponse>) {
                    println("all users response is -> $response")
                }

                override fun onError(error: User.UserNotFound) {
                    println("all users error is -> $error")
                }
            })
        }

        val deleteUserButton = findViewById<Button>(R.id.button11);
        val deleteUserId: EditText = findViewById(R.id.textInputEditText14)

        deleteUserButton.setOnClickListener{
            val id = deleteUserId.text.toString()
            user.deleteUser(id, object : User.DeleteUserCallBack{
                override fun onSuccess(response: Any?) {
                    println("response in onSuccess -> $response")
                }
                override fun onError(error: User.UserNotFound) {
                    println("error in onSuccess -> $error")
                }
            })
        }


        val createUserButton = findViewById<Button>(R.id.button12);
        createUserButton.setOnClickListener{
            val userParams = mutableMapOf<String, Any>()

            phoneNumber.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["phone_number"] = it
            }

            userName.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["name"] = it
            }

            id.text.toString().takeIf { it.isNotBlank() }?.toIntOrNull()?.let {
                userParams["id"] = it
            }

            email.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["email"] = it
            }

            dateOfBirth.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["date_of_birth"] = it
            }

            policyNumber.text.toString().takeIf { it.isNotBlank() }?.let {
                userParams["policy_number"] = it
            }

            user.createUser(userParams, object : User.CreateUserCallBack{
                override fun onSuccess(response: User.UserResponse) {
                    if (response is User.UserResponse){
                        println("createUser response all data is  -> $response")
                    }
                }

                override fun onError(error: User.UserNotFound) {
                    if (error is User.UserNotFound) {
                        println("createUser error all data is  -> $error")
                    }
                }
            })
        }
    }
}
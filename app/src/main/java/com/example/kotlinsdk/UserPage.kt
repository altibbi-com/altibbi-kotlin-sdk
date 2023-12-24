package com.example.kotlinsdk

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.altibbi.ApiService
import com.example.altibbi.User


class UserPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)
        val getUserIdButton = findViewById<Button>(R.id.button4);
        val textInputEditText: EditText = findViewById(R.id.textInputEditText2)

        getUserIdButton.setOnClickListener{
            val userId = textInputEditText.text.toString()
            ApiService.getUser(userId, object : User.GetUserCallback{
                override fun onSuccess(response: User.UserResponse) {
                    if(response is User.UserResponse){
                        println("Received response in callback getPhrById all data is: $response")
                    }
                }

                override fun onError(error: Any) {
                    println("getUser Error all data : $error")

                }
            })
        }

        val updateUserButton = findViewById<Button>(R.id.button6);
        val userName: EditText = findViewById(R.id.textInputEditText8)
        val email: EditText = findViewById(R.id.textInputEditText7)
        val height: EditText = findViewById(R.id.textInputEditText10)
        val dateOfBirth: EditText = findViewById(R.id.textInputEditText11)
        val policyNumber: EditText = findViewById(R.id.textInputEditText12)
        val id: EditText = findViewById(R.id.textInputEditText13)
        val insuranceNumber: EditText = findViewById(R.id.textInputEditText9)
        val gender: EditText = findViewById(R.id.textInputEditText22)

        updateUserButton.setOnClickListener{
            val updateUserData = User.UpdateUserData(
                name = userName.text.toString(),
                email = email.text.toString(),
                dateOfBirth = null,
                gender = gender.text.toString(),
                insuranceId = insuranceNumber.text.toString(),
                policyNumber = policyNumber.text.toString(),
                height = null,
                weight = null,
                bloodType = null,
                smoker = null,
                alcoholic = null,
                nationalityNumber = null,
                maritalStatus = null,
                id = id.text.toString()
            )

            println("userParams before call api -> $updateUserData")
            ApiService.updateUser(updateUserData, object : User.UpdateUserCallback{
                override fun onSuccess(response: User.UserResponse) {
                    if (response is User.UserResponse){
                        println("response obj is $response")
                    }
                }

                override fun onError(error: Any) {
                    println("error in example is $error")
                }
            })
        }

        val getAllUsersButton = findViewById<Button>(R.id.button10);
        getAllUsersButton.setOnClickListener{
            ApiService.getUsers(object : User.GetUsersCallback{
                override fun onSuccess(response: List<User.UserResponse>) {
                    println("all users response is -> $response")
                }

                override fun onError(error: Any) {
                    println("all users error is -> $error")
                }
            })
        }

        val deleteUserButton = findViewById<Button>(R.id.button11);
        val deleteUserId: EditText = findViewById(R.id.textInputEditText14)

        deleteUserButton.setOnClickListener{
            val id = deleteUserId.text.toString()
            ApiService.deleteUser(id, object : User.DeleteUserCallBack{
                override fun onSuccess(response: Any?) {
                    println("response in onSuccess -> $response")
                }
                override fun onError(error: Any) {
                    println("error in deleteUser onError -> $error")
                }
            })
        }


        val createUserButton = findViewById<Button>(R.id.button12);
        createUserButton.setOnClickListener{
            val createUserData = User.CreateUserData(
                name = userName.text.toString(),
                email = email.text.toString(),
                dateOfBirth = null,
                gender = gender.text.toString(),
                insuranceId = insuranceNumber.text.toString(),
                policyNumber = policyNumber.text.toString(),
                height = null,
                weight = null,
                bloodType = null,
                smoker = null,
                alcoholic = null,
                nationalityNumber = null,
                maritalStatus = null
            )

            ApiService.createUser(createUserData, object : User.CreateUserCallBack{
                override fun onSuccess(response: User.UserResponse) {
                    if (response is User.UserResponse){
                        println("createUser response all data is  -> $response")
                    }
                }

                override fun onError(error: Any) {
                    println("createUser error in example is  -> $error")
                }
            })
        }
    }
}
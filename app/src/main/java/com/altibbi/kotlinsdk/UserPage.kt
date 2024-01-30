package com.altibbi.kotlinsdk

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.altibbi.telehealth.ApiCallback
import com.altibbi.telehealth.ApiService
import com.altibbi.telehealth.User


class UserPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)
        val getUserIdButton = findViewById<Button>(R.id.button4);
        val textInputEditText: EditText = findViewById(R.id.textInputEditText2)


        getUserIdButton.setOnClickListener{
            val userId = textInputEditText.text.toString()
           ApiService.getUser(userId,   object : ApiCallback<User> {
               override fun onSuccess(response: User) {
                   println("Successful response: ${response.name}")
                   println("Successful response: ${response.id}")
               }

               override fun onFailure(error: String?) {
                   println(error)
               }

               override fun onRequestError(error: String?) {
                   println(error)
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

            val user = User(
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

            println("update user data -> $user")
            ApiService.updateUser(user,user.id,object : ApiCallback<User> {
                override fun onSuccess(response: User) {
                    println("Successful response: ${response.name}")
                    println("Successful response: ${response.id}")
                }

                override fun onFailure(error: String?) {
                    println(error)
                }

                override fun onRequestError(error: String?) {
                    println(error)
                }
            })
        }

        val getAllUsersButton = findViewById<Button>(R.id.button10);

        getAllUsersButton.setOnClickListener{
            ApiService.getUsers(object : ApiCallback<List<User>> {
                override fun onSuccess(response: List<User>) {
                    println("Successful response: ${response[5].id}")
                }
                override fun onFailure(error: String?) {
                    println(error)
                }

                override fun onRequestError(error: String?) {
                    println(error)
                }

            })
        }

        val deleteUserButton = findViewById<Button>(R.id.button11);
        val deleteUserId: EditText = findViewById(R.id.textInputEditText14)

        deleteUserButton.setOnClickListener{
            val id = deleteUserId.text.toString()
            ApiService.deleteUser(id, object : ApiCallback<Boolean> {
                override fun onSuccess(response: Boolean) {
                }

                override fun onFailure(error: String?) {
                    println(error)
                }

                override fun onRequestError(error: String?) {
                    println(error)
                }

            })
        }


        val createUserButton = findViewById<Button>(R.id.button12);
        createUserButton.setOnClickListener{
            val user = User(
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

            ApiService.createUser(user, object : ApiCallback<User> {
                override fun onSuccess(response: User) {
                    println("Successful response: ${response.name}")
                    println("Successful response: ${response.id}")
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
}
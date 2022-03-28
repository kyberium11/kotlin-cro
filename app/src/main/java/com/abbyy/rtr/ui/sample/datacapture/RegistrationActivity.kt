package com.abbyy.rtr.ui.sample.datacapture

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    var databaseReference : DatabaseReference? = null
    var database: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("profile")

        register()
    }

    private fun register() {
        registerButton.setOnClickListener {
            if(TextUtils.isEmpty(firstnameInput.text.toString())) {
                firstnameInput.setError("Please enter your first name!")
                return@setOnClickListener
            } else if(TextUtils.isEmpty(lastnameInput.text.toString())) {
                firstnameInput.setError("Please enter your last name!")
                return@setOnClickListener
            } else if(TextUtils.isEmpty(usernameRegInput.text.toString())) {
                usernameRegInput.setError("Please enter your email!")
                return@setOnClickListener
            } else if(TextUtils.isEmpty(passwordRegInput.text.toString())) {
                passwordRegInput.setError("Please enter your password!")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(usernameRegInput.text.toString(), passwordRegInput.text.toString())
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        val currentUser = auth.currentUser
                        val currentUserDb = databaseReference?.child((currentUser?.uid!!))
                        currentUserDb?.child( "firstname")?.setValue(firstnameInput.text.toString())
                        currentUserDb?.child( "lastname")?.setValue(lastnameInput.text.toString())

                        Toast.makeText(this@RegistrationActivity, "Registration successful!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegistrationActivity, "Registration failed! Please try again", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
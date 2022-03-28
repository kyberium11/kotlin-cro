package com.abbyy.rtr.ui.sample.datacapture

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import android.content.Intent

class LoginActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        login()
    }

    private fun login() {
        loginButton.setOnClickListener {
            if(TextUtils.isEmpty(usernameInput.text.toString())) {
                usernameInput.setError("Please enter your username!")
                return@setOnClickListener
            } else if(TextUtils.isEmpty(passwordInput.text.toString())) {
                usernameInput.setError("Please enter your password!")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(usernameInput.text.toString(), passwordInput.text.toString())
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        startActivity(Intent(this@LoginActivity, AppActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed! Please try again", Toast.LENGTH_LONG).show()
                    }
                }
        }

        registerText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
        }
    }
}
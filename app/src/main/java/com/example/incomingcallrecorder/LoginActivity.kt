package com.example.incomingcallrecorder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signup.setOnClickListener {
            if (password.text.isNotEmpty() or email.text.isNotEmpty()) {

                if (Pattern.matches(email.text.toString(), email.text.toString())) {

                    Intent(this, MainActivity::class.java).apply {
                        startActivity(this)
                    }
                } else {

                    email.error = "Does not matches email"
                }

            }
        }

    }
}
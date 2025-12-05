package com.example.soapysignal.landing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.soapysignal.R
import com.example.soapysignal.login.LoginActivity

class LandingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // Set up Get Started button
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
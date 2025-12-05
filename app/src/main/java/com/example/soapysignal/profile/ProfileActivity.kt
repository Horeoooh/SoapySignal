package com.example.soapysignal.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import com.example.soapysignal.R
import com.example.soapysignal.history.HistoryActivity
import com.example.soapysignal.home.HomeActivity
import com.example.soapysignal.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : Activity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        btnLogout = findViewById(R.id.btnLogout)

        // Load user data from Firebase
        loadUserData()

        // Setup edit profile button
        val btnEditPhoto = findViewById<FrameLayout>(R.id.btnEditPhoto)
        btnEditPhoto.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Setup logout button
        btnLogout.setOnClickListener {
            logout()
        }

        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun loadUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Set email from Firebase Auth
            etEmail.setText(currentUser.email)

            // Get additional user data from Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName") ?: ""
                        etFullName.setText(fullName)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        val homeNav = findViewById<LinearLayout>(R.id.homeNav)
        val historyNav = findViewById<LinearLayout>(R.id.historyNav)
        val settingsNav = findViewById<LinearLayout>(R.id.settingsNav)

        homeNav.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        historyNav.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        settingsNav.setOnClickListener {
            // Already on profile/settings screen
        }
    }
}
package com.example.soapysignal.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.soapysignal.R
import com.example.soapysignal.history.HistoryActivity
import com.example.soapysignal.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : Activity() {

    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize views
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)

        // Load user data
        loadUserData()

        // Setup click listeners
        setupClickListeners()

        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun loadUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Set email from Firebase Auth
            tvEmail.text = currentUser.email

            // Get additional user data from Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName") ?: ""
                        tvFullName.text = fullName
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupClickListeners() {
        // Edit photo button
        val btnEditPhoto = findViewById<FrameLayout>(R.id.btnEditPhoto)
        btnEditPhoto.setOnClickListener {
            Toast.makeText(this, "Change profile photo", Toast.LENGTH_SHORT).show()
            // TODO: Implement photo picker
        }

        // Full name row
        val rowFullName = findViewById<LinearLayout>(R.id.rowFullName)
        rowFullName.setOnClickListener {
            Toast.makeText(this, "Edit full name", Toast.LENGTH_SHORT).show()
            // TODO: Open edit name dialog/activity
        }

        // Email row
        val rowEmail = findViewById<LinearLayout>(R.id.rowEmail)
        rowEmail.setOnClickListener {
            Toast.makeText(this, "Edit email address", Toast.LENGTH_SHORT).show()
            // TODO: Open edit email dialog/activity
        }

        // Change password row
        val rowChangePassword = findViewById<LinearLayout>(R.id.rowChangePassword)
        rowChangePassword.setOnClickListener {
            Toast.makeText(this, "Change password", Toast.LENGTH_SHORT).show()
            // TODO: Open change password dialog/activity
        }
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
            // Go back to main profile
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        // Go back to main profile
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
}
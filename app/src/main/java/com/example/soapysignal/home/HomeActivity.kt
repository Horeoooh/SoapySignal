package com.example.soapysignal.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.soapysignal.R
import com.example.soapysignal.dashboard.DashboardActivity
import com.example.soapysignal.history.HistoryActivity
import com.example.soapysignal.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : Activity() {

    // Views
    private lateinit var btnScanForDevices: Button
    private lateinit var btnConnectManually: Button
    private lateinit var homeNav: LinearLayout
    private lateinit var historyNav: LinearLayout
    private lateinit var settingsNav: LinearLayout

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // User data
    private var userFullName: String = ""
    private var householdCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        // Initialize views
        initializeViews()

        // Load user data
        loadUserData()

        // Set up click listeners
        setupClickListeners()

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun initializeViews() {
        btnScanForDevices = findViewById(R.id.btnScanForDevices)
        btnConnectManually = findViewById(R.id.btnConnectManually)
        homeNav = findViewById(R.id.homeNav)
        historyNav = findViewById(R.id.historyNav)
        settingsNav = findViewById(R.id.settingsNav)
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userFullName = document.getString("fullName") ?: "User"
                        householdCode = document.getString("householdCode") ?: ""
                        updateUIWithUserData()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to load user data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun updateUIWithUserData() {
        try {
            // Update household name - check if view exists first
            val tvHouseholdName = findViewById<TextView>(R.id.tvHouseholdName)
            if (tvHouseholdName != null) {
                tvHouseholdName.text = "$householdCode Household"
            }

            // Update greeting with user's first name
            val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
            if (tvGreeting != null) {
                val firstName = userFullName.split(" ").firstOrNull() ?: "User"
                tvGreeting.text = "Hello $firstName!"
            }
        } catch (e: Exception) {
            // Views might not exist in layout, that's okay
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        // Scan for devices button
        btnScanForDevices.setOnClickListener {
            Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show()

            // Simulate a delay then navigate to dashboard
            btnScanForDevices.postDelayed({
                navigateToDashboard()
            }, 1500)
        }

        // Connect manually button
        btnConnectManually.setOnClickListener {
            Toast.makeText(this, "Manual connection coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        homeNav.setOnClickListener {
            Toast.makeText(this, "You are already on Home", Toast.LENGTH_SHORT).show()
        }

        historyNav.setOnClickListener {
            navigateToHistory()
        }

        settingsNav.setOnClickListener {
            showSettingsOptions()
        }
    }

    private fun showSettingsOptions() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Settings")
        builder.setItems(arrayOf("Profile", "Notifications", "Logout")) { _, which ->
            when (which) {
                0 -> Toast.makeText(this, "Profile coming soon!", Toast.LENGTH_SHORT).show()
                1 -> Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show()
                2 -> performLogout()
            }
        }
        builder.show()
    }

    private fun performLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
}
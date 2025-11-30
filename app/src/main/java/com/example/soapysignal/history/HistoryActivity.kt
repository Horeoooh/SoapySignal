package com.example.soapysignal.history

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.soapysignal.R
import com.example.soapysignal.home.HomeActivity
import com.example.soapysignal.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : Activity() {

    // Firebase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var authRepository: AuthRepository

    // Views
    private lateinit var historyContainer: LinearLayout

    // User's household code
    private var householdCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        authRepository = AuthRepository()

        // Check if user is logged in
        if (!authRepository.isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        // Find the container for history items (inside ScrollView)
        findHistoryContainer()

        // Load user data to get household code
        loadUserDataAndHistory()

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun findHistoryContainer() {
        // Find the ScrollView and its child LinearLayout
        val scrollView = findViewById<ScrollView>(android.R.id.content)
            ?.findViewById<ScrollView>(R.id.historyScrollView)

        // If no specific ID, try to find by structure
        // The LinearLayout inside ScrollView in activity_history.xml
        val rootView = findViewById<LinearLayout>(android.R.id.content)?.getChildAt(0) as? LinearLayout

        if (rootView != null) {
            // Find ScrollView child
            for (i in 0 until rootView.childCount) {
                val child = rootView.getChildAt(i)
                if (child is ScrollView) {
                    historyContainer = child.getChildAt(0) as LinearLayout
                    return
                }
            }
        }

        // Fallback: create a new container
        historyContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun loadUserDataAndHistory() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            authRepository.getUserData(
                uid = currentUser.uid,
                onSuccess = { user ->
                    householdCode = user.householdCode
                    runOnUiThread {
                        loadHistoryFromFirestore()
                    }
                },
                onFailure = { exception ->
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Failed to load user data: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    private fun loadHistoryFromFirestore() {
        if (householdCode.isEmpty()) {
            Toast.makeText(this, "Household code not found", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("sessions")
            .whereEqualTo("householdCode", householdCode)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .limit(50)  // Limit to last 50 sessions
            .get()
            .addOnSuccessListener { documents ->
                runOnUiThread {
                    // Clear existing items (except the header and bottom nav)
                    clearHistoryItems()

                    if (documents.isEmpty) {
                        showEmptyState()
                    } else {
                        for (document in documents) {
                            val sessionNumber = document.getLong("sessionNumber")?.toInt() ?: 0
                            val startTime = document.getLong("startTime") ?: 0L
                            val endTime = document.getLong("endTime")
                            val status = document.getString("status") ?: "completed"
                            val userName = document.getString("userName") ?: "Unknown"

                            // Add stopped entry if endTime exists
                            if (endTime != null && endTime > 0) {
                                addHistoryItem(
                                    sessionNumber = sessionNumber,
                                    timestamp = endTime,
                                    isStarted = false,
                                    userName = userName
                                )
                            }

                            // Add started entry
                            addHistoryItem(
                                sessionNumber = sessionNumber,
                                timestamp = startTime,
                                isStarted = true,
                                userName = userName
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Failed to load history: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun clearHistoryItems() {
        // Keep only the first child (header) and last child (bottom nav)
        // This depends on your layout structure
        if (::historyContainer.isInitialized) {
            historyContainer.removeAllViews()
        }
    }

    private fun showEmptyState() {
        val emptyView = TextView(this).apply {
            text = "No history yet.\nStart using your washing machine!"
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
        historyContainer.addView(emptyView)
    }

    private fun addHistoryItem(
        sessionNumber: Int,
        timestamp: Long,
        isStarted: Boolean,
        userName: String
    ) {
        val itemView = createHistoryItemView(sessionNumber, timestamp, isStarted, userName)
        historyContainer.addView(itemView)
    }

    private fun createHistoryItemView(
        sessionNumber: Int,
        timestamp: Long,
        isStarted: Boolean,
        userName: String
    ): View {
        // Create the item layout programmatically
        val itemLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(24, 12, 24, 12)
            }
            setBackgroundColor(resources.getColor(android.R.color.white, null))
            setPadding(36, 36, 36, 36)
            elevation = 4f
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        // Icon
        val iconView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(120, 120)
            if (isStarted) {
                setImageResource(R.drawable.started)
            } else {
                setImageResource(R.drawable.stopped)
            }
            contentDescription = if (isStarted) "Started" else "Stopped"
        }

        // Text container
        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 36
            }
        }

        // Status text
        val statusText = TextView(this).apply {
            text = if (isStarted) "Session $sessionNumber Start" else "Session $sessionNumber Stopped"
            textSize = 16f
            setTextColor(
                if (isStarted)
                    resources.getColor(android.R.color.holo_green_dark, null)
                else
                    resources.getColor(android.R.color.holo_red_dark, null)
            )
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        // Time text
        val timeText = TextView(this).apply {
            text = formatTimestamp(timestamp)
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }

        // User text
        val userText = TextView(this).apply {
            text = "By: $userName"
            textSize = 12f
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }

        textContainer.addView(statusText)
        textContainer.addView(timeText)
        textContainer.addView(userText)

        itemLayout.addView(iconView)
        itemLayout.addView(textContainer)

        return itemLayout
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("M/d/yyyy h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun setupBottomNavigation() {
        try {
            val bottomNav = findViewById<LinearLayout>(R.id.bottomNav)
            if (bottomNav != null && bottomNav.childCount >= 3) {
                // Home
                bottomNav.getChildAt(0).setOnClickListener {
                    navigateToHome()
                }

                // History (current)
                bottomNav.getChildAt(1).setOnClickListener {
                    Toast.makeText(this, "You are already on History", Toast.LENGTH_SHORT).show()
                }

                // Settings
                bottomNav.getChildAt(2).setOnClickListener {
                    Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            // Handle navigation error silently
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, com.example.soapysignal.login.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Companion object for saving sessions (call this from DashboardActivity)
    companion object {
        fun saveSessionToFirestore(
            firestore: FirebaseFirestore,
            householdCode: String,
            sessionNumber: Int,
            userName: String,
            isStart: Boolean,
            onSuccess: () -> Unit = {},
            onFailure: (Exception) -> Unit = {}
        ) {
            val sessionId = "${householdCode}_${sessionNumber}"
            val currentTime = System.currentTimeMillis()

            if (isStart) {
                // Create new session
                val sessionData = hashMapOf(
                    "sessionNumber" to sessionNumber,
                    "startTime" to currentTime,
                    "userName" to userName,
                    "householdCode" to householdCode,
                    "status" to "active"
                )

                firestore.collection("sessions")
                    .document(sessionId)
                    .set(sessionData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            } else {
                // Update existing session with end time
                firestore.collection("sessions")
                    .document(sessionId)
                    .update(
                        mapOf(
                            "endTime" to currentTime,
                            "status" to "completed"
                        )
                    )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            }
        }
    }
}
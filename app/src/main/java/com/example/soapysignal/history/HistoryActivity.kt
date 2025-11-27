package com.example.soapysignal.history

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import com.example.soapysignal.R
import com.example.soapysignal.dashboard.DashboardActivity

class HistoryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<LinearLayout>(R.id.bottomNav)

        // Get the three navigation items (Home, History, Settings)
        val homeNav = bottomNav.getChildAt(0) as LinearLayout
        val historyNav = bottomNav.getChildAt(1) as LinearLayout
        val settingsNav = bottomNav.getChildAt(2) as LinearLayout

        // Set click listeners for navigation
        homeNav.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        historyNav.setOnClickListener {
            // Already on history page, do nothing
        }

        settingsNav.setOnClickListener {
            // Settings not yet implemented
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}
package com.example.soapysignal.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.soapysignal.R
import com.example.soapysignal.dashboard.DashboardActivity
import com.example.soapysignal.history.HistoryActivity

class HomeActivity : Activity(), HomeView {
    private lateinit var tvStatus: TextView
    private lateinit var btnScanForDevices: Button
    private lateinit var btnConnectManually: Button
    private lateinit var presenter: HomePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        tvStatus = findViewById(R.id.tvStatus)
        btnScanForDevices = findViewById(R.id.btnScanForDevices)
        btnConnectManually = findViewById(R.id.btnConnectManually)

        // Initialize model and presenter
        val model = HomeModel(this)

        // Reset connection status when opening HomeActivity
        model.saveDeviceConnectionStatus(false)

        presenter = HomePresenter(this, model)

        // Set click listeners - override presenter behavior
        btnScanForDevices.setOnClickListener {
            Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show()
            simulateDeviceConnection()
        }

        btnConnectManually.setOnClickListener {
            Toast.makeText(this, "Connecting manually...", Toast.LENGTH_SHORT).show()
            simulateDeviceConnection()
        }

        // Check device connection status
        presenter.checkDeviceConnection()

        // Setup bottom navigation
        setupBottomNavigation()
    }

    // --- View Implementation ---
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showDeviceConnected() {
        tvStatus.text = "Device connected"
        navigateToDashboard()
    }

    override fun showDeviceOffline() {
        tvStatus.text = "No device connected"
    }

    override fun showScanDevicesMessage() {
        Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show()
    }

    override fun showManualConnectionMessage() {
        Toast.makeText(this, "Connecting manually...", Toast.LENGTH_SHORT).show()
    }

    private fun simulateDeviceConnection() {
        // Save connection status
        val model = HomeModel(this)
        model.saveDeviceConnectionStatus(true)

        // Navigate to dashboard after short delay
        btnScanForDevices.postDelayed({
            navigateToDashboard()
        }, 1000)
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        try {
            // Try to find individual navigation items if they exist
            val homeNav = findViewById<LinearLayout>(R.id.homeNav)
            val historyNav = findViewById<LinearLayout>(R.id.historyNav)
            val settingsNav = findViewById<LinearLayout>(R.id.settingsNav)

            homeNav?.setOnClickListener {
                // Already on home page
            }

            historyNav?.setOnClickListener {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }

            settingsNav?.setOnClickListener {
                Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
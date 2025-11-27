package com.example.soapysignal.dashboard

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class DashboardModel(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("DashboardPrefs", Context.MODE_PRIVATE)

    data class MachineStatus(
        val status: String,
        val description: String,
        val color: Int
    )

    data class SessionInfo(
        val sessionNumber: Int,
        val startTime: String,
        val userName: String
    )

    // Get current machine status
    fun getMachineStatus(): MachineStatus {
        val status = prefs.getString("machineStatus", "Idle") ?: "Idle"
        val description = prefs.getString("machineDescription", "Your washing machine is idle.")
            ?: "Your washing machine is idle."
        val color = prefs.getInt("machineColor", 0xFF9E9E9E.toInt())

        return MachineStatus(status, description, color)
    }

    // Save machine status
    fun saveMachineStatus(status: String, description: String, color: Int) {
        prefs.edit().apply {
            putString("machineStatus", status)
            putString("machineDescription", description)
            putInt("machineColor", color)
            apply()
        }
    }

    // Get session information
    fun getSessionInfo(): SessionInfo {
        val sessionNumber = prefs.getInt("sessionNumber", 1)
        val startTime = prefs.getString("sessionStartTime", getCurrentTimestamp())
            ?: getCurrentTimestamp()
        val userName = prefs.getString("sessionUserName", "John Santos") ?: "John Santos"

        return SessionInfo(sessionNumber, startTime, userName)
    }

    // Save session information
    fun saveSessionInfo(sessionNumber: Int, startTime: String, userName: String) {
        prefs.edit().apply {
            putInt("sessionNumber", sessionNumber)
            putString("sessionStartTime", startTime)
            putString("sessionUserName", userName)
            apply()
        }
    }

    // Get last updated timestamp
    fun getLastUpdated(): String {
        return prefs.getString("lastUpdated", getCurrentTimestamp()) ?: getCurrentTimestamp()
    }

    // Update last updated timestamp
    fun updateLastUpdated() {
        prefs.edit().apply {
            putString("lastUpdated", getCurrentTimestamp())
            apply()
        }
    }

    // Check if machine is currently spinning
    fun isSpinning(): Boolean {
        val status = prefs.getString("machineStatus", "Idle") ?: "Idle"
        return status.equals("Spinning", ignoreCase = true)
    }

    // Helper function to get current timestamp
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
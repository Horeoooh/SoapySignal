package com.example.soapysignal.home

import android.content.Context
import android.content.SharedPreferences

class HomeModel(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)

    // Mock: Simulate device connection status
    fun isDeviceConnected(): Boolean {
        return prefs.getBoolean("isDeviceConnected", false)
    }

    fun saveDeviceConnectionStatus(isConnected: Boolean) {
        prefs.edit().apply {
            putBoolean("isDeviceConnected", isConnected)
            apply()
        }
    }
}

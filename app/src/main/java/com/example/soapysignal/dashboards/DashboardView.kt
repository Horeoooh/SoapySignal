package com.example.soapysignal.dashboard

interface DashboardView {
    fun showError(message: String)
    fun updateStatus(status: String, description: String, color: Int)
    fun updateSessionInfo(sessionNumber: Int, startTime: String, userName: String)
    fun updateLastUpdated(timestamp: String)
    fun startSpinAnimation()
    fun stopSpinAnimation()
    fun navigateToHome()
    fun navigateToHistory()
    fun navigateToSettings()
}
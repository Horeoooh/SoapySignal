package com.example.soapysignal.dashboard

class DashboardPresenter(
    private val view: DashboardView,
    private val model: DashboardModel
) {

    // Load dashboard data
    fun loadDashboardData() {
        try {
            // Load machine status
            val machineStatus = model.getMachineStatus()
            view.updateStatus(
                machineStatus.status,
                machineStatus.description,
                machineStatus.color
            )

            // Load session info
            val sessionInfo = model.getSessionInfo()
            view.updateSessionInfo(
                sessionInfo.sessionNumber,
                sessionInfo.startTime,
                sessionInfo.userName
            )

            // Load last updated timestamp
            val lastUpdated = model.getLastUpdated()
            view.updateLastUpdated(lastUpdated)

            // Start animation if spinning
            if (model.isSpinning()) {
                view.startSpinAnimation()
            } else {
                view.stopSpinAnimation()
            }

        } catch (e: Exception) {
            view.showError("Failed to load dashboard data: ${e.message}")
        }
    }

    // Refresh dashboard
    fun refreshDashboard() {
        model.updateLastUpdated()
        loadDashboardData()
    }

    // Handle navigation clicks
    fun onHomeClicked() {
        view.navigateToHome()
    }

    fun onHistoryClicked() {
        view.navigateToHistory()
    }

    fun onSettingsClicked() {
        view.navigateToSettings()
    }

    // Simulate status change (for testing)
    fun simulateSpinning() {
        model.saveMachineStatus(
            "Spinning",
            "Your washing machine is currently spinning.",
            0xFF43A047.toInt() // Green
        )
        loadDashboardData()
    }

    fun simulateIdle() {
        model.saveMachineStatus(
            "Idle",
            "Your washing machine is idle.",
            0xFF9E9E9E.toInt() // Gray
        )
        loadDashboardData()
    }
}
package com.example.soapysignal.home

class HomePresenter(
    private val view: HomeView,
    private val model: HomeModel
) {
    fun onScanForDevicesClicked() {
        // Show a message instead of navigating
        view.showScanDevicesMessage()
    }

    fun onConnectManuallyClicked() {
        // Show a message instead of navigating
        view.showManualConnectionMessage()
    }

    fun checkDeviceConnection() {
        // Check device connection status
        if (model.isDeviceConnected()) {
            view.showDeviceConnected()
        } else {
            view.showDeviceOffline()
        }
    }
}

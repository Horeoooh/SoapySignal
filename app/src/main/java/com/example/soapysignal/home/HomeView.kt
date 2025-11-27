package com.example.soapysignal.home

interface HomeView {
    fun showError(message: String)
    fun showDeviceConnected()
    fun showDeviceOffline()
    fun showScanDevicesMessage()
    fun showManualConnectionMessage()
}

package com.example.soapysignal.history

interface HistoryView {
    fun showNoHistory()
    fun showHistory(history: List<String>)
}
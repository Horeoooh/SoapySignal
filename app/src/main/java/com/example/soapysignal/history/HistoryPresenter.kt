package com.example.soapysignal.history

class HistoryPresenter(
    private val view: HistoryView,
    private val model: HistoryModel
) {
    fun loadHistory() {
        val history = model.getHistory()
        if (history.isEmpty()) {
            view.showNoHistory()
        } else {
            view.showHistory(history)
        }
    }
}
package com.example.soapysignal.register

import android.content.Context
import android.content.SharedPreferences

class RegisterModel(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun saveAccount(houseCode: String, email: String, password: String, callback: (Boolean) -> Unit) {
        val editor = prefs.edit()
        editor.putString("houseCode", houseCode)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
        callback(true)
    }
}

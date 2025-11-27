package com.example.soapysignal.login

import android.content.Context
import android.content.SharedPreferences

class LoginModel(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    private val hardcodedHouseCode = "abc123"
    private val hardcodedEmail = "juancruz@gmail.com"
    private val hardcodedPassword = "123456"

    fun validateAccount(houseCode: String, email: String, password: String): Boolean {
        if (houseCode == hardcodedHouseCode &&
            email == hardcodedEmail &&
            password == hardcodedPassword
        ) return true

        val savedHouseCode = prefs.getString("houseCode", null)
        val savedEmail = prefs.getString("email", null)
        val savedPassword = prefs.getString("password", null)

        return (houseCode == savedHouseCode &&
                email == savedEmail &&
                password == savedPassword)
    }

    fun saveAccount(houseCode: String, email: String, password: String) {
        prefs.edit().apply {
            putString("houseCode", houseCode)
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    fun clearAccount() {
        prefs.edit().clear().apply()
    }
}

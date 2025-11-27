package com.example.soapysignal.login

interface LoginView {
    fun showError(message: String)
    fun showLoginSuccess()
    fun showLoginFailed()
    fun showPasswordVisible()
    fun showPasswordHidden()
    fun navigateToRegister()
    fun navigateToHome()
}

package com.example.soapysignal.register

interface RegisterView {
    fun showError(message: String)
    fun showHouseCodeError(message: String)
    fun showFullNameError(message: String)
    fun showEmailError(message: String)
    fun showPasswordError(message: String)
    fun showConfirmPasswordError(message: String)
    fun showRegistrationSuccess()
    fun showGoogleSignUp()
    fun redirectToSignIn()
}
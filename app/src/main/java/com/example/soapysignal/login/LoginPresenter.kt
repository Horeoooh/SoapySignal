package com.example.soapysignal.login

import android.util.Patterns

class LoginPresenter(
    private val view: LoginView,
    private val model: LoginModel
) {
    fun validateInputs(houseCode: String, email: String, password: String) {
        when {
            houseCode.isEmpty() -> view.showError("Household code is required")
            houseCode.length < 6 -> view.showError("Household code must be at least 6 characters")
            email.isEmpty() -> view.showError("Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> view.showError("Invalid email format")
            password.isEmpty() -> view.showError("Password is required")
            password.length < 6 -> view.showError("Password must be at least 6 characters")
            else -> validateLogin(houseCode, email, password)
        }
    }

    private fun validateLogin(houseCode: String, email: String, password: String) {
        if (model.validateAccount(houseCode, email, password)) {
            view.showLoginSuccess()
            view.navigateToHome()
        } else {
            view.showLoginFailed()
        }
    }

    fun togglePasswordVisibility(isVisible: Boolean) {
        if (isVisible) {
            view.showPasswordHidden()
        } else {
            view.showPasswordVisible()
        }
    }

    fun onSignUpClicked() {
        view.navigateToRegister()
    }
}

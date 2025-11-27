package com.example.soapysignal.register

import android.util.Patterns

class RegisterPresenter(
    private val view: RegisterView,
    private val model: RegisterModel
) {

    fun validateInputs(
        houseCode: String,
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        isAgreeChecked: Boolean
    ) {
        when {
            houseCode.isEmpty() -> view.showHouseCodeError("Please enter your household code")
            fullName.isEmpty() -> view.showFullNameError("Please enter your full name")
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                view.showEmailError("Enter a valid email")
            password.length < 6 -> view.showPasswordError("Password must be at least 6 characters")
            confirmPassword != password -> view.showConfirmPasswordError("Passwords do not match")
            !isAgreeChecked -> view.showError("Please agree to the terms first")
            else -> {
                model.saveAccount(houseCode, email, password) { success ->
                    if (success) view.showRegistrationSuccess()
                    else view.showError("Account creation failed")
                }
            }
        }
    }

    fun onGoogleSignUp() {
        view.showGoogleSignUp()
    }

    fun onSignInClick() {
        view.redirectToSignIn()
    }
}
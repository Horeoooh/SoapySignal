package com.example.soapysignal.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.soapysignal.R
import com.example.soapysignal.home.HomeActivity
import com.example.soapysignal.register.RegisterActivity
import com.example.soapysignal.repository.AuthRepository

class LoginActivity : Activity() {

    // Views
    private lateinit var etHouseCode: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvNoAccount: TextView
    private lateinit var btnSignUp: TextView

    // Repository
    private lateinit var authRepository: AuthRepository

    // Password visibility toggle
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize repository
        authRepository = AuthRepository()

        // Check if user is already logged in
        if (authRepository.isUserLoggedIn()) {
            navigateToHome()
            return
        }

        // Initialize views
        initializeViews()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        etHouseCode = findViewById(R.id.etHouseCode)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvNoAccount = findViewById(R.id.tvNoAccount)
        btnSignUp = findViewById(R.id.btnSignUp)
    }

    private fun setupClickListeners() {
        // Login button click
        btnLogin.setOnClickListener {
            performLogin()
        }

        // Forgot password click
        tvForgotPassword.setOnClickListener {
            handleForgotPassword()
        }

        // Sign up click
        btnSignUp.setOnClickListener {
            navigateToRegister()
        }

        tvNoAccount.setOnClickListener {
            navigateToRegister()
        }

        // Password visibility toggle
        etPassword.setOnTouchListener { v, event ->
            val DRAWABLE_END = 2
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                if (event.rawX >= (etPassword.right - etPassword.compoundDrawables[DRAWABLE_END].bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun performLogin() {
        val householdCode = etHouseCode.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validate inputs
        if (!validateInputs(householdCode, email, password)) {
            return
        }

        // Show loading state
        setLoadingState(true)

        // Perform login with Firebase
        authRepository.login(
            email = email,
            password = password,
            householdCode = householdCode,
            onSuccess = { user ->
                runOnUiThread {
                    setLoadingState(false)
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
            },
            onFailure = { exception ->
                runOnUiThread {
                    setLoadingState(false)
                    handleLoginError(exception)
                }
            }
        )
    }

    private fun validateInputs(householdCode: String, email: String, password: String): Boolean {
        // Validate household code
        if (householdCode.isEmpty()) {
            etHouseCode.error = "Household code is required"
            etHouseCode.requestFocus()
            return false
        }

        // Validate email
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email"
            etEmail.requestFocus()
            return false
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun handleLoginError(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("password is invalid") == true ->
                "Invalid password. Please try again."
            exception.message?.contains("no user record") == true ->
                "No account found with this email."
            exception.message?.contains("Invalid household code") == true ->
                "Invalid household code for this account."
            exception.message?.contains("network") == true ->
                "Network error. Please check your connection."
            exception.message?.contains("too many requests") == true ->
                "Too many failed attempts. Please try again later."
            exception.message?.contains("disabled") == true ->
                "This account has been disabled."
            else ->
                "Login failed: ${exception.message}"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun handleForgotPassword() {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Please enter your email first"
            etEmail.requestFocus()
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email"
            etEmail.requestFocus()
            return
        }

        setLoadingState(true)

        authRepository.sendPasswordResetEmail(
            email = email,
            onSuccess = {
                runOnUiThread {
                    setLoadingState(false)
                    Toast.makeText(
                        this,
                        "Password reset email sent to $email",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onFailure = { exception ->
                runOnUiThread {
                    setLoadingState(false)
                    Toast.makeText(
                        this,
                        "Failed to send reset email: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeopen, 0)
        } else {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeclose, 0)
        }

        // Move cursor to end
        etPassword.setSelection(etPassword.text.length)
    }

    private fun setLoadingState(isLoading: Boolean) {
        btnLogin.isEnabled = !isLoading
        etHouseCode.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading

        if (isLoading) {
            btnLogin.text = "Logging in..."
        } else {
            btnLogin.text = "Login"
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
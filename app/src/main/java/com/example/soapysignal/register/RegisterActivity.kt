package com.example.soapysignal.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.soapysignal.R
import com.example.soapysignal.home.HomeActivity
import com.example.soapysignal.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : Activity() {

    // Views
    private lateinit var etHouseCode: EditText
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var cbAgree: CheckBox
    private lateinit var btnCreateAccount: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvSignIn: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
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
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        cbAgree = findViewById(R.id.cbAgree)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvSignIn = findViewById(R.id.tvSignIn)
    }

    private fun setupClickListeners() {
        // Create account button click
        btnCreateAccount.setOnClickListener {
            performRegistration()
        }

        // Google sign up click
        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Sign in click (navigate to login)
        tvSignIn.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun performRegistration() {
        val householdCode = etHouseCode.text.toString().trim()
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val agreedToTerms = cbAgree.isChecked

        // Validate inputs
        if (!validateInputs(householdCode, fullName, email, password, confirmPassword, agreedToTerms)) {
            return
        }

        // Show loading state
        setLoadingState(true)

        // First check or create household
        checkOrCreateHousehold(householdCode) {
            // Create user account
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    if (firebaseUser != null) {
                        // Save user data to Firestore
                        saveUserToFirestore(
                            uid = firebaseUser.uid,
                            fullName = fullName,
                            email = email,
                            householdCode = householdCode
                        )
                    } else {
                        setLoadingState(false)
                        Toast.makeText(this, "Registration failed: User is null", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->
                    setLoadingState(false)
                    handleRegistrationError(exception)
                }
        }
    }

    private fun checkOrCreateHousehold(householdCode: String, onComplete: () -> Unit) {
        firestore.collection("households")
            .document(householdCode)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Create new household
                    val householdData = hashMapOf(
                        "code" to householdCode,
                        "createdAt" to System.currentTimeMillis()
                    )
                    firestore.collection("households")
                        .document(householdCode)
                        .set(householdData)
                        .addOnSuccessListener { onComplete() }
                        .addOnFailureListener { exception ->
                            setLoadingState(false)
                            Toast.makeText(this, "Failed to create household: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    onComplete()
                }
            }
            .addOnFailureListener { exception ->
                setLoadingState(false)
                Toast.makeText(this, "Failed to check household: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserToFirestore(uid: String, fullName: String, email: String, householdCode: String) {
        val userData = hashMapOf(
            "uid" to uid,
            "fullName" to fullName,
            "email" to email,
            "householdCode" to householdCode,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                // Add user to household members
                addUserToHousehold(uid, fullName, householdCode)
            }
            .addOnFailureListener { exception ->
                // Delete auth user if Firestore save fails
                auth.currentUser?.delete()
                setLoadingState(false)
                Toast.makeText(this, "Failed to save user data: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun addUserToHousehold(uid: String, fullName: String, householdCode: String) {
        val memberData = hashMapOf(
            "uid" to uid,
            "fullName" to fullName,
            "joinedAt" to System.currentTimeMillis()
        )

        firestore.collection("households")
            .document(householdCode)
            .collection("members")
            .document(uid)
            .set(memberData)
            .addOnSuccessListener {
                setLoadingState(false)
                Toast.makeText(
                    this,
                    "Account created successfully! Welcome, $fullName!",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToHome()
            }
            .addOnFailureListener { exception ->
                // Still navigate to home even if adding to household fails
                setLoadingState(false)
                Toast.makeText(this, "Account created but failed to join household", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
    }

    private fun validateInputs(
        householdCode: String,
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        agreedToTerms: Boolean
    ): Boolean {
        // Validate household code
        if (householdCode.isEmpty()) {
            etHouseCode.error = "Household code is required"
            etHouseCode.requestFocus()
            return false
        }

        if (householdCode.length < 4) {
            etHouseCode.error = "Household code must be at least 4 characters"
            etHouseCode.requestFocus()
            return false
        }

        // Validate full name
        if (fullName.isEmpty()) {
            etFullName.error = "Full name is required"
            etFullName.requestFocus()
            return false
        }

        if (fullName.length < 2) {
            etFullName.error = "Please enter your full name"
            etFullName.requestFocus()
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

        // Check password strength
        if (!isPasswordStrong(password)) {
            etPassword.error = "Password must contain at least one letter and one number"
            etPassword.requestFocus()
            return false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Please confirm your password"
            etConfirmPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return false
        }

        // Validate terms agreement
        if (!agreedToTerms) {
            Toast.makeText(
                this,
                "Please agree to the Terms of Service and Privacy Policy",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        return true
    }

    private fun isPasswordStrong(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }

    private fun handleRegistrationError(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("email address is already in use") == true ->
                "This email is already registered. Please sign in instead."
            exception.message?.contains("email address is badly formatted") == true ->
                "Please enter a valid email address."
            exception.message?.contains("password is invalid") == true ->
                "Password is too weak. Please use at least 6 characters."
            exception.message?.contains("network") == true ->
                "Network error. Please check your internet connection."
            exception.message?.contains("too many requests") == true ->
                "Too many attempts. Please try again later."
            else ->
                "Registration failed: ${exception.message}"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun setLoadingState(isLoading: Boolean) {
        btnCreateAccount.isEnabled = !isLoading
        btnGoogle.isEnabled = !isLoading
        etHouseCode.isEnabled = !isLoading
        etFullName.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        etConfirmPassword.isEnabled = !isLoading
        cbAgree.isEnabled = !isLoading

        if (isLoading) {
            btnCreateAccount.text = "Creating Account..."
        } else {
            btnCreateAccount.text = "Create Account"
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToLogin()
    }
}
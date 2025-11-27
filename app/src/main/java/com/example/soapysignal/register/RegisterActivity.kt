package com.example.soapysignal.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.soapysignal.login.LoginActivity
import com.example.soapysignal.R

class RegisterActivity : Activity(), RegisterView {

    private lateinit var etHouseCode: EditText
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var cbAgree: CheckBox
    private lateinit var btnCreateAccount: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvSignIn: TextView
    private lateinit var presenter: RegisterPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etHouseCode = findViewById(R.id.etHouseCode)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        cbAgree = findViewById(R.id.cbAgree)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvSignIn = findViewById(R.id.tvSignIn)

        presenter = RegisterPresenter(this, RegisterModel(this))


        btnCreateAccount.setOnClickListener {
            presenter.validateInputs(
                etHouseCode.text.toString().trim(),
                etFullName.text.toString().trim(),
                etEmail.text.toString().trim(),
                etPassword.text.toString().trim(),
                etConfirmPassword.text.toString().trim(),
                cbAgree.isChecked
            )
        }

        btnGoogle.setOnClickListener {
            presenter.onGoogleSignUp()
        }

        tvSignIn.setOnClickListener {
            presenter.onSignInClick()
        }
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showHouseCodeError(message: String) {
        etHouseCode.error = message
    }

    override fun showFullNameError(message: String) {
        etFullName.error = message
    }

    override fun showEmailError(message: String) {
        etEmail.error = message
    }

    override fun showPasswordError(message: String) {
        etPassword.error = message
    }

    override fun showConfirmPasswordError(message: String) {
        etConfirmPassword.error = message
    }

    override fun showRegistrationSuccess() {
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()
        redirectToSignIn()
    }

    override fun showGoogleSignUp() {
        Toast.makeText(this, "Google sign-up", Toast.LENGTH_SHORT).show()
    }

    override fun redirectToSignIn() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
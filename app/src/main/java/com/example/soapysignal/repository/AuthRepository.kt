package com.example.soapysignal.repository

import com.example.soapysignal.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Get current user
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Login with email and password
    fun login(
        email: String,
        password: String,
        householdCode: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    // Verify household code matches
                    verifyHouseholdCode(user.uid, householdCode,
                        onSuccess = {
                            onSuccess(user)
                        },
                        onFailure = { exception ->
                            // Sign out if household code doesn't match
                            auth.signOut()
                            onFailure(exception)
                        }
                    )
                } else {
                    onFailure(Exception("Login failed: User is null"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Register new user
    fun register(
        fullName: String,
        email: String,
        password: String,
        householdCode: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // First check if household code exists or create new one
        checkOrCreateHousehold(householdCode,
            onSuccess = {
                // Create user account
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val firebaseUser = authResult.user
                        if (firebaseUser != null) {
                            // Save user data to Firestore
                            val user = User(
                                oddu = firebaseUser.uid,
                                fullName = fullName,
                                email = email,
                                householdCode = householdCode,
                                createdAt = System.currentTimeMillis()
                            )
                            saveUserToFirestore(user,
                                onSuccess = {
                                    onSuccess(firebaseUser)
                                },
                                onFailure = { exception ->
                                    // Delete auth user if Firestore save fails
                                    firebaseUser.delete()
                                    onFailure(exception)
                                }
                            )
                        } else {
                            onFailure(Exception("Registration failed: User is null"))
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            },
            onFailure = { exception ->
                onFailure(exception)
            }
        )
    }

    // Save user data to Firestore
    private fun saveUserToFirestore(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .document(user.oddu)
            .set(user.toMap())
            .addOnSuccessListener {
                // Also add user to household members
                addUserToHousehold(user.oddu, user.householdCode, user.fullName,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Get user data from Firestore
    fun getUserData(
        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val user = User.fromMap(data)
                        onSuccess(user)
                    } else {
                        onFailure(Exception("User data is empty"))
                    }
                } else {
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Verify household code matches user's registered household
    private fun verifyHouseholdCode(
        uid: String,
        householdCode: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val storedHouseholdCode = document.getString("householdCode")
                    if (storedHouseholdCode == householdCode) {
                        onSuccess()
                    } else {
                        onFailure(Exception("Invalid household code"))
                    }
                } else {
                    onFailure(Exception("User data not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Check if household exists or create new one
    private fun checkOrCreateHousehold(
        householdCode: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("households")
            .document(householdCode)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Create new household
                    val householdData = hashMapOf(
                        "code" to householdCode,
                        "createdAt" to System.currentTimeMillis(),
                        "members" to listOf<String>()
                    )
                    firestore.collection("households")
                        .document(householdCode)
                        .set(householdData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception -> onFailure(exception) }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Add user to household members
    private fun addUserToHousehold(
        uid: String,
        householdCode: String,
        fullName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
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
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    // Logout
    fun logout() {
        auth.signOut()
    }

    // Send password reset email
    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    // Delete user account
    fun deleteAccount(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            // First delete user data from Firestore
            firestore.collection("users")
                .document(user.uid)
                .delete()
                .addOnSuccessListener {
                    // Then delete auth account
                    user.delete()
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception -> onFailure(exception) }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("No user logged in"))
        }
    }
}
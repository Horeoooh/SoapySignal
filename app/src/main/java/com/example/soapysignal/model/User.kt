package com.example.soapysignal.model

data class User(
    val oddu: String = "",
    val fullName: String = "",
    val email: String = "",
    val householdCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Empty constructor needed for Firestore
    constructor() : this("", "", "", "", 0L)

    // Convert to Map for Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to oddu,
            "fullName" to fullName,
            "email" to email,
            "householdCode" to householdCode,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): User {
            return User(
                oddu = map["uid"] as? String ?: "",
                fullName = map["fullName"] as? String ?: "",
                email = map["email"] as? String ?: "",
                householdCode = map["householdCode"] as? String ?: "",
                createdAt = map["createdAt"] as? Long ?: 0L
            )
        }
    }
}
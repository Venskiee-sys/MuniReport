package com.munireport.app.domain.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val address: String = "",
    val purok: String = "",
    val role: UserRole = UserRole.RESIDENT,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_SUBMITTED,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    val firstName: String
        get() = fullName.trim().substringBefore(' ').ifBlank { "Resident" }

    val initials: String
        get() = fullName
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "MR" }

    val isAdmin: Boolean
        get() = role == UserRole.ADMIN

    val isVerified: Boolean
        get() = verificationStatus == VerificationStatus.APPROVED
}

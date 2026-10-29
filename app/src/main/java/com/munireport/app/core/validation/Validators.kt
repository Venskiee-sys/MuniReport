package com.munireport.app.core.validation

import java.util.Locale

data class PasswordStrength(
    val hasMinimumLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
) {
    val score: Int
        get() = listOf(hasMinimumLength, hasUppercase, hasLowercase, hasDigit).count { it }

    val isValid: Boolean
        get() = score == 4
}

object Validators {
    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun email(value: String): String? = when {
        value.isBlank() -> "Enter your email address."
        !emailPattern.matches(value.trim()) -> "Enter a valid email address."
        else -> null
    }

    fun passwordStrength(value: String): PasswordStrength = PasswordStrength(
        hasMinimumLength = value.length >= 8,
        hasUppercase = value.any { it.isUpperCase() },
        hasLowercase = value.any { it.isLowerCase() },
        hasDigit = value.any { it.isDigit() },
    )

    fun password(value: String): String? = when {
        value.isBlank() -> "Enter a password."
        else -> if (passwordStrength(value).isValid) null else "Use 8 or more characters with uppercase, lowercase, and a number."
    }

    fun confirmPassword(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> "Confirm your password."
        password != confirmation -> "Passwords do not match."
        else -> null
    }

    fun required(value: String, fieldName: String): String? =
        if (value.isBlank()) "Enter $fieldName." else null

    fun fullName(value: String): String? = when {
        value.isBlank() -> "Enter your full name."
        value.trim().split(Regex("\\s+")).size < 2 -> "Enter your first and last name."
        value.length > 80 -> "Keep your name under 80 characters."
        else -> null
    }

    fun phone(value: String): String? {
        val digits = value.filter { it.isDigit() }
        return when {
            value.isBlank() -> "Enter your mobile number."
            digits.length !in 10..13 -> "Enter a valid mobile number."
            else -> null
        }
    }

    fun purok(value: String): String? = required(value, "your purok")

    fun address(value: String): String? = when {
        value.isBlank() -> "Enter your house and street."
        value.length < 7 -> "Add a little more detail to your address."
        else -> null
    }

    fun complaintTitle(value: String): String? = when {
        value.trim().length < 8 -> "Use at least 8 characters."
        value.trim().length > 90 -> "Keep the title under 90 characters."
        else -> null
    }

    fun complaintDescription(value: String): String? = when {
        value.trim().length < 20 -> "Describe the concern in at least 20 characters."
        value.trim().length > 1200 -> "Keep the description under 1,200 characters."
        else -> null
    }

    fun idNumber(value: String): String? = when {
        value.isBlank() -> "Enter the ID number."
        value.length < 4 -> "Enter the complete ID number."
        value.length > 40 -> "That ID number looks too long."
        else -> null
    }

    fun yearsOfResidency(value: String): String? {
        val years = value.toIntOrNull()
        return when {
            years == null -> "Enter a whole number."
            years < 1 -> "Residency must be at least one year."
            years > 100 -> "Check the number of years."
            else -> null
        }
    }

    fun normalizeEmail(value: String): String = value.trim().lowercase(Locale.ROOT)
}

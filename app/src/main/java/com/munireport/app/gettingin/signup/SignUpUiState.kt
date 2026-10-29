package com.munireport.app.gettingin.signup

import com.munireport.app.core.validation.PasswordStrength
import com.munireport.app.core.validation.Validators

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val purok: String = "",
    val password: String = "",
    val acceptedTerms: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val purokError: String? = null,
    val passwordError: String? = null,
    val termsError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val passwordStrength: PasswordStrength
        get() = Validators.passwordStrength(password)

    val canSubmit: Boolean
        get() = !isSubmitting
}

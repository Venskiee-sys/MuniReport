package com.munireport.app.gettingin.forgotpassword

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isSubmitting: Boolean = false,
    val isSent: Boolean = false,
    val errorMessage: String? = null,
)

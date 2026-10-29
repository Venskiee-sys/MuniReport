package com.munireport.app.admin.verification

import com.munireport.app.domain.model.ResidencyVerification

data class VerificationDetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val verification: ResidencyVerification? = null,
    val isSubmitting: Boolean = false,
    val actionError: String? = null,
    val completed: Boolean = false,
)

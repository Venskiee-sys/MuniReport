package com.munireport.app.gettingin.verification

import com.munireport.app.domain.model.ResidencyVerification

data class VerificationStatusUiState(
    val isLoading: Boolean = true,
    val verification: ResidencyVerification? = null,
    val errorMessage: String? = null,
)

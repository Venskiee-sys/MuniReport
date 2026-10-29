package com.munireport.app.gettingin.verification

import com.munireport.app.domain.model.IdType
import com.munireport.app.domain.model.VerificationStatus

data class ResidencyFormUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val existingStatus: VerificationStatus? = null,
    val existingNote: String? = null,
    val address: String = "",
    val purok: String = "",
    val yearsOfResidency: String = "",
    val idType: IdType = IdType.PHILIPPINE_ID,
    val idNumber: String = "",
    val documentUri: String? = null,
    val documentName: String? = null,
    val addressError: String? = null,
    val purokError: String? = null,
    val yearsError: String? = null,
    val idNumberError: String? = null,
    val documentError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitted: Boolean = false,
)

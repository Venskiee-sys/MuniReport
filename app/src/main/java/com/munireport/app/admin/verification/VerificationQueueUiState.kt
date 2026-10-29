package com.munireport.app.admin.verification

import com.munireport.app.domain.model.ResidencyVerification
import com.munireport.app.domain.model.VerificationStatus

enum class VerificationQueueFilter(val label: String, val status: VerificationStatus?) {
    PENDING("Pending", VerificationStatus.PENDING),
    APPROVED("Approved", VerificationStatus.APPROVED),
    REJECTED("Needs correction", VerificationStatus.REJECTED),
    ALL("All", null),
}

data class VerificationQueueUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val verifications: List<ResidencyVerification> = emptyList(),
    val filter: VerificationQueueFilter = VerificationQueueFilter.PENDING,
    val query: String = "",
)

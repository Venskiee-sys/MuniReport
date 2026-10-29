package com.munireport.app.resident.home

import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.VerificationStatus

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val recentComplaints: List<Complaint> = emptyList(),
    val myReportsCount: Int = 0,
    val openCount: Int = 0,
    val resolvedCount: Int = 0,
    val followingCount: Int = 0,
    val unreadNotifications: Int = 0,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_SUBMITTED,
)

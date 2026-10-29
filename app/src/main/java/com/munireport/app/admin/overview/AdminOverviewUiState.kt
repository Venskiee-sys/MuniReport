package com.munireport.app.admin.overview

import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ResidencyVerification

data class AdminOverviewUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val totalReports: Int = 0,
    val openReports: Int = 0,
    val pendingReports: Int = 0,
    val reviewingReports: Int = 0,
    val resolvedReports: Int = 0,
    val urgentReports: Int = 0,
    val reportsThisWeek: Int = 0,
    val weeklyChange: Int = 0,
    val olderPendingReports: Int = 0,
    val pendingVerifications: Int = 0,
    val verifiedResidents: Int = 0,
    val unreadNotifications: Int = 0,
    val recentComplaints: List<Complaint> = emptyList(),
    val attentionComplaints: List<Complaint> = emptyList(),
    val verificationPreview: List<ResidencyVerification> = emptyList(),
)

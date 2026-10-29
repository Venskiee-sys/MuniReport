package com.munireport.app.admin.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.domain.repository.NotificationRepository
import com.munireport.app.domain.repository.VerificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit

class AdminOverviewViewModel(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    verificationRepository: VerificationRepository,
    notificationRepository: NotificationRepository,
) : ViewModel() {

    val uiState = combine(
        complaintRepository.observeComplaints(profile.id),
        verificationRepository.observeQueue(),
        notificationRepository.observeNotifications(profile.id),
    ) { complaints, verifications, notifications ->
        val now = System.currentTimeMillis()
        val weekAgo = now - TimeUnit.DAYS.toMillis(7)
        val twoWeeksAgo = now - TimeUnit.DAYS.toMillis(14)
        val threeDaysAgo = now - TimeUnit.DAYS.toMillis(3)

        val open = complaints.filter { it.status.isOpen }
        val pending = complaints.filter { it.status == ComplaintStatus.PENDING }
        val reviewing = complaints.filter { it.status.isBeingReviewed }
        val pendingVerifications = verifications.filter { it.status == VerificationStatus.PENDING }

        val priorityOrder = mapOf(
            ComplaintPriority.URGENT to 0,
            ComplaintPriority.HIGH to 1,
            ComplaintPriority.MEDIUM to 2,
            ComplaintPriority.LOW to 3,
        )
        val attention = open
            .sortedWith(
                compareBy<Complaint> { priorityOrder[it.priority] ?: 9 }
                    .thenBy { it.createdAt },
            )
            .take(3)

        val thisWeek = complaints.count { it.createdAt >= weekAgo }
        val lastWeek = complaints.count { it.createdAt in twoWeeksAgo until weekAgo }

        AdminOverviewUiState(
            isLoading = false,
            totalReports = complaints.size,
            openReports = open.size,
            pendingReports = pending.size,
            reviewingReports = reviewing.size,
            resolvedReports = complaints.count { it.status == ComplaintStatus.RESOLVED },
            urgentReports = open.count { it.priority == ComplaintPriority.URGENT },
            reportsThisWeek = thisWeek,
            weeklyChange = thisWeek - lastWeek,
            olderPendingReports = pending.count { it.createdAt < threeDaysAgo },
            pendingVerifications = pendingVerifications.size,
            verifiedResidents = verifications.count { it.status == VerificationStatus.APPROVED },
            unreadNotifications = notifications.count { !it.isRead },
            recentComplaints = complaints.sortedByDescending { it.createdAt }.take(4),
            attentionComplaints = attention,
            verificationPreview = pendingVerifications.sortedByDescending { it.submittedAt }.take(2),
        )
    }.catch { failure ->
        emit(AdminOverviewUiState(isLoading = false, errorMessage = failure.userMessage()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AdminOverviewUiState(),
    )

    companion object {
        fun factory(
            profile: UserProfile,
            complaintRepository: ComplaintRepository,
            verificationRepository: VerificationRepository,
            notificationRepository: NotificationRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AdminOverviewViewModel(
                    profile = profile,
                    complaintRepository = complaintRepository,
                    verificationRepository = verificationRepository,
                    notificationRepository = notificationRepository,
                )
            }
        }
    }
}

package com.munireport.app.resident.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    notificationRepository: NotificationRepository,
) : ViewModel() {

    val uiState = combine(
        complaintRepository.observeComplaints(profile.id),
        notificationRepository.observeNotifications(profile.id),
    ) { complaints, notifications ->
        val mine = complaints.filter { it.authorId == profile.id }
        HomeUiState(
            isLoading = false,
            recentComplaints = complaints.filter { it.authorId != profile.id }.take(3),
            myReportsCount = mine.size,
            openCount = mine.count {
                it.status in listOf(
                    ComplaintStatus.PENDING,
                    ComplaintStatus.ACKNOWLEDGED,
                    ComplaintStatus.IN_PROGRESS,
                )
            },
            resolvedCount = mine.count { it.status == ComplaintStatus.RESOLVED },
            followingCount = complaints.count { it.followedByMe },
            unreadNotifications = notifications.count { !it.isRead },
            verificationStatus = profile.verificationStatus,
        )
    }.catch { failure ->
        emit(
            HomeUiState(
                isLoading = false,
                errorMessage = failure.userMessage(),
                verificationStatus = profile.verificationStatus,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(verificationStatus = profile.verificationStatus),
    )

    companion object {
        fun factory(
            profile: UserProfile,
            complaintRepository: ComplaintRepository,
            notificationRepository: NotificationRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(profile, complaintRepository, notificationRepository) }
        }
    }
}

package com.munireport.app.resident.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.AppNotification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val profile: UserProfile,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                notificationRepository.observeNotifications(profile.id).collect { notifications ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = notifications,
                            errorMessage = null,
                        )
                    }
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = failure.userMessage())
                }
            }
        }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            runCatching { notificationRepository.markRead(profile.id, notificationId) }
                .onFailure { failure ->
                    _uiState.update { it.copy(errorMessage = failure.userMessage()) }
                }
        }
    }

    fun markAllRead() {
        if (_uiState.value.isMarkingAll) return
        _uiState.update { it.copy(isMarkingAll = true) }
        viewModelScope.launch {
            runCatching { notificationRepository.markAllRead(profile.id) }
                .onFailure { failure ->
                    _uiState.update { it.copy(errorMessage = failure.userMessage()) }
                }
            _uiState.update { it.copy(isMarkingAll = false) }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    companion object {
        fun factory(
            profile: UserProfile,
            notificationRepository: NotificationRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { NotificationsViewModel(profile, notificationRepository) }
        }
    }
}

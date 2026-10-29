package com.munireport.app.resident.notifications

import com.munireport.app.domain.model.AppNotification

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList(),
    val errorMessage: String? = null,
    val isMarkingAll: Boolean = false,
)

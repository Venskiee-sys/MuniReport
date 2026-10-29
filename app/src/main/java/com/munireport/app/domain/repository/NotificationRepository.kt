package com.munireport.app.domain.repository

import com.munireport.app.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<AppNotification>>
    suspend fun markRead(userId: String, notificationId: String)
    suspend fun markAllRead(userId: String)
}

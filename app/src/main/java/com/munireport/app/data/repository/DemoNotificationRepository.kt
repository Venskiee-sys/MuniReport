package com.munireport.app.data.repository

import com.munireport.app.data.local.DemoMuniStore
import com.munireport.app.domain.model.AppNotification
import com.munireport.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DemoNotificationRepository(private val store: DemoMuniStore) : NotificationRepository {

    override fun observeNotifications(userId: String): Flow<List<AppNotification>> =
        store.notifications.map { all ->
            all[userId].orEmpty().sortedByDescending { it.createdAt }
        }

    override suspend fun markRead(userId: String, notificationId: String) {
        store.markNotificationRead(userId, notificationId)
    }

    override suspend fun markAllRead(userId: String) {
        store.markAllNotificationsRead(userId)
    }
}

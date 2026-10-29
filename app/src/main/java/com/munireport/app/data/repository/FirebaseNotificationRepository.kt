package com.munireport.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.munireport.app.data.mapper.toNotification
import com.munireport.app.data.remote.toAppException
import com.munireport.app.domain.model.AppNotification
import com.munireport.app.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseNotificationRepository(
    private val firestore: FirebaseFirestore,
) : NotificationRepository {

    private fun collection(userId: String) =
        firestore.collection("users").document(userId).collection("notifications")

    override fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val registration = collection(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error.toAppException())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toNotification() }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    override suspend fun markRead(userId: String, notificationId: String) {
        runCatching {
            collection(userId).document(notificationId)
                .update("isRead", true)
                .await()
        }.getOrElse { throw it.toAppException() }
    }

    override suspend fun markAllRead(userId: String) {
        runCatching {
            val unread = collection(userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            if (unread.isEmpty) return@runCatching
            val batch = firestore.batch()
            unread.documents.forEach { document ->
                batch.update(document.reference, "isRead", true)
            }
            batch.commit().await()
        }.getOrElse { throw it.toAppException() }
    }
}

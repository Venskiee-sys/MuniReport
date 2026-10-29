package com.munireport.app.domain.model

enum class NotificationType(val label: String) {
    STATUS_CHANGE("Status update"),
    COMMENT("New comment"),
    VERIFICATION("Verification"),
    SYSTEM("MuniReport"),
}

data class AppNotification(
    val id: String,
    val recipientId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val complaintId: String? = null,
    val createdAt: Long,
    val isRead: Boolean = false,
)

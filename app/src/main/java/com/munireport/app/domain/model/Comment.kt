package com.munireport.app.domain.model

data class Comment(
    val id: String,
    val complaintId: String,
    val authorId: String,
    val authorName: String,
    val authorRole: UserRole,
    val text: String,
    val createdAt: Long,
) {
    val isOfficial: Boolean
        get() = authorRole == UserRole.ADMIN

    val initials: String
        get() = authorName
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "R" }
}

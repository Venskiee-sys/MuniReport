package com.munireport.app.domain.model

enum class ComplaintStatus(val label: String) {
    PENDING("Pending"),
    ACKNOWLEDGED("Being Reviewed"),
    IN_PROGRESS("Being Reviewed"),
    RESOLVED("Resolved"),
    REJECTED("Closed"),
    ;

    /** Pending and being-reviewed reports are still open for follow-up. */
    val isOpen: Boolean
        get() = this == PENDING || this == ACKNOWLEDGED || this == IN_PROGRESS

    /** Both acknowledgment and field work read as "Being Reviewed" to residents. */
    val isBeingReviewed: Boolean
        get() = this == ACKNOWLEDGED || this == IN_PROGRESS
}

enum class ComplaintCategory(val label: String) {
    WASTE_MANAGEMENT("Garbage / Waste"),
    FLOODING("Flooding"),
    DRAINAGE("Drainage"),
    ROADS_AND_SIDEWALKS("Roads / Sidewalks"),
    STREETLIGHTS("Streetlights"),
    OTHER("Others"),
}

enum class ComplaintPriority(val label: String) {
    LOW("Low"),
    MEDIUM("Normal"),
    HIGH("High"),
    URGENT("Urgent"),
}

data class Complaint(
    val id: String,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val category: ComplaintCategory,
    val status: ComplaintStatus,
    val priority: ComplaintPriority,
    val authorId: String,
    val authorName: String,
    val authorPurok: String,
    val address: String,
    val landmark: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoUrls: List<String> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val followedByMe: Boolean = false,
    val followerCount: Int = 0,
    val commentCount: Int = 0,
) {
    val locationLabel: String
        get() = listOf(authorPurok, address, landmark)
            .filter { it.isNotBlank() }
            .joinToString(" · ")

    /** "MR-2026-0141" becomes "MR-0141" for compact in-app labels. */
    val shortTicketNumber: String
        get() {
            val parts = ticketNumber.split('-')
            return if (parts.size >= 3) "${parts.first()}-${parts.last()}" else ticketNumber
        }
}

package com.munireport.app.domain.model

data class ComplaintQuery(
    val search: String = "",
    val statuses: Set<ComplaintStatus> = emptySet(),
    val category: ComplaintCategory? = null,
    val mineOnly: Boolean = false,
    val followedOnly: Boolean = false,
    val residentId: String? = null,
) {
    fun apply(complaints: List<Complaint>): List<Complaint> {
        val needle = search.trim().lowercase()
        return complaints.filter { complaint ->
            val matchesSearch = needle.isEmpty() ||
                complaint.title.lowercase().contains(needle) ||
                complaint.ticketNumber.lowercase().contains(needle) ||
                complaint.address.lowercase().contains(needle)
            val matchesStatus = statuses.isEmpty() || complaint.status in statuses
            val matchesCategory = category == null || complaint.category == category
            val matchesResident = !mineOnly || complaint.authorId == residentId
            val matchesFollow = !followedOnly || complaint.followedByMe
            matchesSearch && matchesStatus && matchesCategory && matchesResident && matchesFollow
        }
    }
}

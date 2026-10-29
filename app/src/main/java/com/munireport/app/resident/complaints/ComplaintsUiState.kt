package com.munireport.app.resident.complaints

import com.munireport.app.domain.model.Complaint

enum class ResidentComplaintFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    REVIEWING("Being Reviewed"),
    RESOLVED("Resolved"),
    MINE("My Complaints"),
    FOLLOWING("Following"),
}

data class ComplaintsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val complaints: List<Complaint> = emptyList(),
    val filter: ResidentComplaintFilter = ResidentComplaintFilter.ALL,
    val query: String = "",
    val totalCount: Int = 0,
)

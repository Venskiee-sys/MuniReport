package com.munireport.app.admin.complaintmanagement

import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintCategory

enum class AdminComplaintStatusFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    REVIEWING("Being Reviewed"),
    RESOLVED("Resolved"),
}

enum class AdminComplaintSort(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    MOST_FOLLOWED("Most followed"),
}

data class ComplaintManagementUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val complaints: List<Complaint> = emptyList(),
    val query: String = "",
    val statusFilter: AdminComplaintStatusFilter = AdminComplaintStatusFilter.ALL,
    val category: ComplaintCategory? = null,
    val barangay: String? = null,
    val sort: AdminComplaintSort = AdminComplaintSort.NEWEST,
    val totalCount: Int = 0,
    val openCount: Int = 0,
    val pendingCount: Int = 0,
    val reviewingCount: Int = 0,
    val resolvedCount: Int = 0,
    val urgentCount: Int = 0,
    val barangays: List<String> = emptyList(),
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() ||
            statusFilter != AdminComplaintStatusFilter.ALL ||
            category != null ||
            barangay != null
}

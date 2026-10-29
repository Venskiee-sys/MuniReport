package com.munireport.app.admin.complaintmanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintCategory
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintQuery
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private data class ComplaintFilters(
    val query: String = "",
    val statusFilter: AdminComplaintStatusFilter = AdminComplaintStatusFilter.ALL,
    val category: ComplaintCategory? = null,
    val barangay: String? = null,
    val sort: AdminComplaintSort = AdminComplaintSort.NEWEST,
)

class ComplaintManagementViewModel(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
) : ViewModel() {

    private val filters = MutableStateFlow(ComplaintFilters())

    val uiState = combine(
        complaintRepository.observeComplaints(profile.id),
        filters,
    ) { all, current ->
        val statuses = when (current.statusFilter) {
            AdminComplaintStatusFilter.ALL -> emptySet()
            AdminComplaintStatusFilter.PENDING -> setOf(ComplaintStatus.PENDING)
            AdminComplaintStatusFilter.REVIEWING -> setOf(
                ComplaintStatus.ACKNOWLEDGED,
                ComplaintStatus.IN_PROGRESS,
            )
            AdminComplaintStatusFilter.RESOLVED -> setOf(ComplaintStatus.RESOLVED)
        }
        val query = ComplaintQuery(
            search = current.query,
            statuses = statuses,
            category = current.category,
        )
        val filtered = query.apply(all)
            .filter { current.barangay == null || it.authorPurok == current.barangay }
            .let { results ->
                when (current.sort) {
                    AdminComplaintSort.NEWEST -> results.sortedByDescending { it.createdAt }
                    AdminComplaintSort.OLDEST -> results.sortedBy { it.createdAt }
                    AdminComplaintSort.MOST_FOLLOWED -> results.sortedWith(
                        compareByDescending<Complaint> { it.followerCount }
                            .thenByDescending { it.createdAt },
                    )
                }
            }
        ComplaintManagementUiState(
            isLoading = false,
            complaints = filtered,
            query = current.query,
            statusFilter = current.statusFilter,
            category = current.category,
            barangay = current.barangay,
            sort = current.sort,
            totalCount = all.size,
            openCount = all.count { it.status.isOpen },
            pendingCount = all.count { it.status == ComplaintStatus.PENDING },
            reviewingCount = all.count { it.status.isBeingReviewed },
            resolvedCount = all.count { it.status == ComplaintStatus.RESOLVED },
            urgentCount = all.count { it.priority == ComplaintPriority.URGENT && it.status.isOpen },
            barangays = all.map { it.authorPurok }.filter { it.isNotBlank() }.distinct().sorted(),
        )
    }.catch { failure ->
        emit(
            ComplaintManagementUiState(
                isLoading = false,
                errorMessage = failure.userMessage(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ComplaintManagementUiState(),
    )

    fun onQueryChange(value: String) {
        filters.value = filters.value.copy(query = value)
    }

    fun onStatusFilterChange(value: AdminComplaintStatusFilter) {
        filters.value = filters.value.copy(statusFilter = value)
    }

    fun onCategoryChange(value: ComplaintCategory?) {
        filters.value = filters.value.copy(category = value)
    }

    fun onBarangayChange(value: String?) {
        filters.value = filters.value.copy(barangay = value)
    }

    fun onSortChange(value: AdminComplaintSort) {
        filters.value = filters.value.copy(sort = value)
    }

    fun clearFilters() {
        filters.value = filters.value.copy(
            query = "",
            statusFilter = AdminComplaintStatusFilter.ALL,
            category = null,
            barangay = null,
        )
    }

    companion object {
        fun factory(
            profile: UserProfile,
            complaintRepository: ComplaintRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ComplaintManagementViewModel(profile, complaintRepository) }
        }
    }
}

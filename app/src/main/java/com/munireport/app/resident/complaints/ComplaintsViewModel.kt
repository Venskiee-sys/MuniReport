package com.munireport.app.resident.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintQuery
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ComplaintsViewModel(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    initialFilter: ResidentComplaintFilter = ResidentComplaintFilter.ALL,
) : ViewModel() {

    private val filter = MutableStateFlow(initialFilter)
    private val query = MutableStateFlow("")

    val uiState = combine(
        complaintRepository.observeComplaints(profile.id),
        filter,
        query,
    ) { complaints, selectedFilter, search ->
        val statuses = when (selectedFilter) {
            ResidentComplaintFilter.PENDING -> setOf(ComplaintStatus.PENDING)
            ResidentComplaintFilter.REVIEWING -> setOf(
                ComplaintStatus.ACKNOWLEDGED,
                ComplaintStatus.IN_PROGRESS,
            )
            ResidentComplaintFilter.RESOLVED -> setOf(ComplaintStatus.RESOLVED)
            else -> emptySet()
        }
        val domainQuery = ComplaintQuery(
            search = search,
            statuses = statuses,
            mineOnly = selectedFilter == ResidentComplaintFilter.MINE,
            followedOnly = selectedFilter == ResidentComplaintFilter.FOLLOWING,
            residentId = profile.id,
        )
        ComplaintsUiState(
            isLoading = false,
            complaints = domainQuery.apply(complaints),
            filter = selectedFilter,
            query = search,
            totalCount = complaints.size,
        )
    }.catch { failure ->
        emit(
            ComplaintsUiState(
                isLoading = false,
                errorMessage = failure.userMessage(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ComplaintsUiState(filter = initialFilter),
    )

    fun onFilterChange(value: ResidentComplaintFilter) {
        filter.value = value
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    companion object {
        fun factory(
            profile: UserProfile,
            complaintRepository: ComplaintRepository,
            initialFilter: ResidentComplaintFilter = ResidentComplaintFilter.ALL,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ComplaintsViewModel(profile, complaintRepository, initialFilter) }
        }
    }
}

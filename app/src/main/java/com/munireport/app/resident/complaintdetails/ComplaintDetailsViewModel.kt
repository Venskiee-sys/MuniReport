package com.munireport.app.resident.complaintdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintUpdate
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class DetailsActionState(
    val isTogglingFollow: Boolean = false,
    val actionError: String? = null,
)

class ComplaintDetailsViewModel(
    private val complaintId: String,
    private val profile: UserProfile,
    private val complaintRepository: ComplaintRepository,
) : ViewModel() {

    private val actionState = MutableStateFlow(DetailsActionState())

    val uiState = combine(
        complaintRepository.observeComplaint(complaintId, profile.id),
        complaintRepository.observeUpdates(complaintId),
        actionState,
    ) { complaint, updates, action ->
        ComplaintDetailsUiState(
            isLoading = false,
            complaint = complaint,
            updates = updates,
            isTogglingFollow = action.isTogglingFollow,
            actionError = action.actionError,
        )
    }.catch { failure ->
        emit(
            ComplaintDetailsUiState(
                isLoading = false,
                errorMessage = failure.userMessage(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ComplaintDetailsUiState(),
    )

    fun toggleFollow() {
        val complaint = uiState.value.complaint ?: return
        if (actionState.value.isTogglingFollow) return
        viewModelScope.launch {
            actionState.update { it.copy(isTogglingFollow = true, actionError = null) }
            runCatching {
                complaintRepository.setFollowing(
                    complaintId = complaint.id,
                    userId = profile.id,
                    following = !complaint.followedByMe,
                )
            }.onFailure { failure ->
                actionState.update { it.copy(actionError = failure.userMessage()) }
            }
            actionState.update { it.copy(isTogglingFollow = false) }
        }
    }

    fun dismissActionError() {
        actionState.update { it.copy(actionError = null) }
    }

    companion object {
        fun factory(
            complaintId: String,
            profile: UserProfile,
            complaintRepository: ComplaintRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ComplaintDetailsViewModel(complaintId, profile, complaintRepository)
            }
        }
    }
}

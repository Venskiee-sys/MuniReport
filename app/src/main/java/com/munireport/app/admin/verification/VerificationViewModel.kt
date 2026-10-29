package com.munireport.app.admin.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.ResidencyVerification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.VerificationRepository
import com.munireport.app.domain.usecase.ReviewVerificationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerificationQueueViewModel(
    verificationRepository: VerificationRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(VerificationQueueFilter.PENDING)
    private val query = MutableStateFlow("")

    val uiState = combine(
        verificationRepository.observeQueue(),
        filter,
        query,
    ) { all, selectedFilter, search ->
        val needle = search.trim().lowercase()
        val filtered = all
            .filter { selectedFilter.status == null || it.status == selectedFilter.status }
            .filter { verification ->
                needle.isEmpty() ||
                    verification.applicantName.lowercase().contains(needle) ||
                    verification.applicantEmail.lowercase().contains(needle) ||
                    verification.address.lowercase().contains(needle) ||
                    verification.purok.lowercase().contains(needle) ||
                    verification.idNumber.lowercase().contains(needle)
            }
            .sortedByDescending { it.submittedAt }
        VerificationQueueUiState(
            isLoading = false,
            verifications = filtered,
            filter = selectedFilter,
            query = search,
        )
    }.catch { failure ->
        emit(
            VerificationQueueUiState(
                isLoading = false,
                errorMessage = failure.userMessage(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VerificationQueueUiState(),
    )

    fun onFilterChange(value: VerificationQueueFilter) {
        filter.value = value
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    companion object {
        fun factory(verificationRepository: VerificationRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { VerificationQueueViewModel(verificationRepository) }
            }
    }
}

private data class VerificationActionState(
    val isSubmitting: Boolean = false,
    val actionError: String? = null,
    val completed: Boolean = false,
)

class VerificationDetailsViewModel(
    private val verificationId: String,
    private val adminProfile: UserProfile,
    private val verificationRepository: VerificationRepository,
    private val reviewVerificationUseCase: ReviewVerificationUseCase,
) : ViewModel() {

    private val actionState = MutableStateFlow(VerificationActionState())

    val uiState = combine(
        verificationRepository.observeVerification(verificationId),
        actionState,
    ) { verification, action ->
        VerificationDetailsUiState(
            isLoading = false,
            verification = verification,
            isSubmitting = action.isSubmitting,
            actionError = action.actionError,
            completed = action.completed,
        )
    }.catch { failure ->
        emit(
            VerificationDetailsUiState(
                isLoading = false,
                errorMessage = failure.userMessage(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VerificationDetailsUiState(),
    )

    fun approve(note: String) {
        review(VerificationStatus.APPROVED, note)
    }

    fun reject(note: String) {
        review(VerificationStatus.REJECTED, note)
    }

    fun dismissActionError() {
        actionState.update { it.copy(actionError = null) }
    }

    private fun review(decision: VerificationStatus, note: String) {
        if (actionState.value.isSubmitting) return
        viewModelScope.launch {
            actionState.update { it.copy(isSubmitting = true, actionError = null) }
            reviewVerificationUseCase(
                verificationId = verificationId,
                decision = decision,
                note = note,
                reviewer = adminProfile,
            ).onSuccess {
                actionState.update { it.copy(isSubmitting = false, completed = true) }
            }.onFailure { failure ->
                actionState.update {
                    it.copy(isSubmitting = false, actionError = failure.userMessage())
                }
            }
        }
    }

    companion object {
        fun factory(
            verificationId: String,
            adminProfile: UserProfile,
            verificationRepository: VerificationRepository,
            reviewVerificationUseCase: ReviewVerificationUseCase,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                VerificationDetailsViewModel(
                    verificationId = verificationId,
                    adminProfile = adminProfile,
                    verificationRepository = verificationRepository,
                    reviewVerificationUseCase = reviewVerificationUseCase,
                )
            }
        }
    }
}

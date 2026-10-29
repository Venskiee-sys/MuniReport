package com.munireport.app.resident.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.AuthRepository
import com.munireport.app.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    initialProfile: UserProfile,
    authRepository: AuthRepository,
    complaintRepository: ComplaintRepository,
) : ViewModel() {

    val uiState = combine(
        authRepository.observeProfile(initialProfile.id),
        complaintRepository.observeComplaints(initialProfile.id),
    ) { liveProfile, complaints ->
        val profile = liveProfile ?: initialProfile
        ProfileUiState(
            isLoading = false,
            profile = profile,
            reportCount = complaints.count { it.authorId == profile.id },
            resolvedCount = complaints.count {
                it.authorId == profile.id && it.status == ComplaintStatus.RESOLVED
            },
            followingCount = complaints.count { it.followedByMe },
            commentCount = complaints
                .filter { it.authorId == profile.id }
                .sumOf { it.commentCount },
            followedComplaints = complaints
                .filter { it.followedByMe }
                .sortedByDescending { it.updatedAt },
        )
    }.catch { failure ->
        emit(
            ProfileUiState(
                isLoading = false,
                profile = initialProfile,
                errorMessage = failure.userMessage(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(profile = initialProfile),
    )

    companion object {
        fun factory(
            initialProfile: UserProfile,
            authRepository: AuthRepository,
            complaintRepository: ComplaintRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProfileViewModel(initialProfile, authRepository, complaintRepository) }
        }
    }
}

class EditProfileViewModel(
    private val profile: UserProfile,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditProfileUiState(
            fullName = profile.fullName,
            phone = profile.phone,
            address = profile.address,
            purok = profile.purok,
        ),
    )
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) = _uiState.update {
        it.copy(fullName = value, fullNameError = null, errorMessage = null)
    }

    fun onPhoneChange(value: String) = _uiState.update {
        it.copy(phone = value, phoneError = null, errorMessage = null)
    }

    fun onAddressChange(value: String) = _uiState.update {
        it.copy(address = value, addressError = null, errorMessage = null)
    }

    fun onPurokChange(value: String) = _uiState.update {
        it.copy(purok = value, purokError = null, errorMessage = null)
    }

    fun save() {
        val current = _uiState.value
        val nameError = com.munireport.app.core.validation.Validators.fullName(current.fullName)
        val phoneError = com.munireport.app.core.validation.Validators.phone(current.phone)
        val addressError = com.munireport.app.core.validation.Validators.address(current.address)
        val purokError = com.munireport.app.core.validation.Validators.purok(current.purok)
        if (listOf(nameError, phoneError, addressError, purokError).any { it != null }) {
            _uiState.update {
                it.copy(
                    fullNameError = nameError,
                    phoneError = phoneError,
                    addressError = addressError,
                    purokError = purokError,
                )
            }
            return
        }
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                authRepository.updateProfile(
                    userId = profile.id,
                    fullName = current.fullName,
                    phone = current.phone,
                    address = current.address,
                    purok = current.purok,
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = failure.userMessage())
                }
            }
        }
    }

    companion object {
        fun factory(
            profile: UserProfile,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { EditProfileViewModel(profile, authRepository) }
        }
    }
}

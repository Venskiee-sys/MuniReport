package com.munireport.app.admin.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.core.validation.Validators
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminAccountViewModel(
    private val profile: UserProfile,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AdminAccountUiState(
            fullName = profile.fullName,
            phone = profile.phone,
            address = profile.address,
            purok = profile.purok,
        ),
    )
    val uiState: StateFlow<AdminAccountUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) = _uiState.update {
        it.copy(fullName = value, fullNameError = null, saveError = null, saveMessage = null)
    }

    fun onPhoneChange(value: String) = _uiState.update {
        it.copy(phone = value, phoneError = null, saveError = null, saveMessage = null)
    }

    fun onAddressChange(value: String) = _uiState.update {
        it.copy(address = value, addressError = null, saveError = null, saveMessage = null)
    }

    fun onPurokChange(value: String) = _uiState.update {
        it.copy(purok = value, purokError = null, saveError = null, saveMessage = null)
    }

    fun save() {
        val current = _uiState.value
        val nameError = Validators.fullName(current.fullName)
        val phoneError = Validators.phone(current.phone)
        val addressError = Validators.address(current.address)
        val purokError = Validators.purok(current.purok)
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
        _uiState.update { it.copy(isSaving = true, saveError = null, saveMessage = null) }
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
                _uiState.update {
                    it.copy(isSaving = false, saveMessage = "Account details updated.")
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(isSaving = false, saveError = failure.userMessage())
                }
            }
        }
    }

    fun sendPasswordReset() {
        if (_uiState.value.isSendingReset) return
        _uiState.update {
            it.copy(isSendingReset = true, resetError = null, resetMessage = null)
        }
        viewModelScope.launch {
            runCatching { authRepository.sendPasswordReset(profile.email) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSendingReset = false,
                            resetMessage = "Password reset link sent to ${profile.email}.",
                        )
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(isSendingReset = false, resetError = failure.userMessage())
                    }
                }
        }
    }

    companion object {
        fun factory(
            profile: UserProfile,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { AdminAccountViewModel(profile, authRepository) }
        }
    }
}

package com.munireport.app.gettingin.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.core.validation.Validators
import com.munireport.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value, emailError = null, errorMessage = null, isSent = false)
        }
    }

    fun sendResetLink() {
        val current = _uiState.value
        val emailError = Validators.email(current.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { authRepository.sendPasswordReset(current.email) }
                .onSuccess {
                    _uiState.update {
                        it.copy(isSubmitting = false, isSent = true)
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = failure.userMessage())
                    }
                }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ForgotPasswordViewModel(authRepository) }
        }
    }
}

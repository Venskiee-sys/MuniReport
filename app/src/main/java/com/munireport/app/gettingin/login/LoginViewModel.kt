package com.munireport.app.gettingin.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.core.constants.AppConstants
import com.munireport.app.core.validation.Validators
import com.munireport.app.domain.usecase.SignInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val signInUseCase: SignInUseCase,
    private val allowDemoAdminPreview: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = null,
                errorMessage = null,
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                errorMessage = null,
            )
        }
    }

    /**
     * Development-only shortcut used by the framed login reference. The demo
     * repository still performs the sign-in, and the shortcut is inert whenever
     * Firebase (production auth) is configured.
     */
    fun previewAdminSignIn() {
        if (!allowDemoAdminPreview) return
        _uiState.update {
            it.copy(
                email = AppConstants.DEMO_ADMIN_EMAIL,
                password = AppConstants.DEMO_ADMIN_PASSWORD,
                emailError = null,
                passwordError = null,
                errorMessage = null,
            )
        }
        signIn()
    }

    fun signIn() {
        val current = _uiState.value
        if (current.isSubmitting) return
        val emailError = Validators.email(current.email)
        val passwordError = if (current.password.isBlank()) "Enter your password." else null
        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = signInUseCase(current.email, current.password)
            result.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = failure.userMessage(),
                    )
                }
            }
            result.onSuccess {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    companion object {
        fun factory(
            signInUseCase: SignInUseCase,
            allowDemoAdminPreview: Boolean,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(
                    signInUseCase = signInUseCase,
                    allowDemoAdminPreview = allowDemoAdminPreview,
                )
            }
        }
    }
}

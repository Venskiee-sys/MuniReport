package com.munireport.app.gettingin.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.core.validation.Validators
import com.munireport.app.gettingin.session.AuthSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val sessionManager: AuthSessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) = _uiState.update {
        it.copy(fullName = value, fullNameError = null, errorMessage = null)
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = null, errorMessage = null)
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

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null)
    }

    fun onAcceptTermsChange(value: Boolean) = _uiState.update {
        it.copy(acceptedTerms = value, termsError = null)
    }

    fun submit() {
        val current = _uiState.value
        val fullNameError = Validators.fullName(current.fullName)
        val emailError = Validators.email(current.email)
        val phoneError = Validators.phone(current.phone)
        val addressError = Validators.address(current.address)
        val purokError = Validators.purok(current.purok)
        val passwordError = Validators.password(current.password)
        val termsError = if (!current.acceptedTerms) {
            "Confirm that your details are accurate before continuing."
        } else {
            null
        }
        if (listOf(
                fullNameError,
                emailError,
                phoneError,
                addressError,
                purokError,
                passwordError,
                termsError,
            ).any { it != null }
        ) {
            _uiState.update {
                it.copy(
                    fullNameError = fullNameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    addressError = addressError,
                    purokError = purokError,
                    passwordError = passwordError,
                    termsError = termsError,
                )
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            sessionManager.signUp(
                fullName = current.fullName,
                email = current.email,
                phone = current.phone,
                address = current.address,
                purok = current.purok,
                password = current.password,
            ).onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = failure.userMessage(),
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    companion object {
        fun factory(sessionManager: AuthSessionManager): ViewModelProvider.Factory = viewModelFactory {
            initializer { SignUpViewModel(sessionManager) }
        }
    }
}

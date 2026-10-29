package com.munireport.app.gettingin.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.core.validation.Validators
import com.munireport.app.domain.model.IdType
import com.munireport.app.domain.model.ResidencyDraft
import com.munireport.app.domain.model.ResidencyVerification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.VerificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResidencyFormViewModel(
    private val profile: UserProfile,
    private val verificationRepository: VerificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ResidencyFormUiState(
            address = profile.address,
            purok = profile.purok,
        ),
    )
    val uiState: StateFlow<ResidencyFormUiState> = _uiState.asStateFlow()

    init {
        loadExisting()
    }

    fun loadExisting() {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            runCatching { verificationRepository.getLatestVerification(profile.id) }
                .onSuccess { verification ->
                    _uiState.update { state ->
                        if (verification == null) {
                            state.copy(isLoading = false)
                        } else {
                            state.copy(
                                isLoading = false,
                                existingStatus = verification.status,
                                existingNote = verification.reviewNote,
                                address = verification.address,
                                purok = verification.purok,
                                yearsOfResidency = verification.yearsOfResidency.toString(),
                                idType = verification.idType,
                                idNumber = verification.idNumber,
                                documentUri = verification.documentUrl,
                                documentName = verification.documentName,
                            )
                        }
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(isLoading = false, loadError = failure.userMessage())
                    }
                }
        }
    }

    fun onAddressChange(value: String) = _uiState.update {
        it.copy(address = value, addressError = null, submitError = null)
    }

    fun onPurokChange(value: String) = _uiState.update {
        it.copy(purok = value, purokError = null, submitError = null)
    }

    fun onYearsChange(value: String) = _uiState.update {
        it.copy(
            yearsOfResidency = value.filter { character -> character.isDigit() }.take(3),
            yearsError = null,
            submitError = null,
        )
    }

    fun onIdTypeChange(value: IdType) = _uiState.update { it.copy(idType = value) }

    fun onIdNumberChange(value: String) = _uiState.update {
        it.copy(idNumber = value, idNumberError = null, submitError = null)
    }

    fun onDocumentSelected(uri: String, name: String) = _uiState.update {
        it.copy(
            documentUri = uri,
            documentName = name,
            documentError = null,
            submitError = null,
        )
    }

    fun submit() {
        val current = _uiState.value
        val addressError = Validators.address(current.address)
        val purokError = Validators.purok(current.purok)
        val yearsError = Validators.yearsOfResidency(current.yearsOfResidency)
        val idNumberError = Validators.idNumber(current.idNumber)
        val documentError = if (current.documentUri.isNullOrBlank()) {
            "Attach a clear photo of your government ID."
        } else {
            null
        }
        if (listOf(addressError, purokError, yearsError, idNumberError, documentError).any { it != null }) {
            _uiState.update {
                it.copy(
                    addressError = addressError,
                    purokError = purokError,
                    yearsError = yearsError,
                    idNumberError = idNumberError,
                    documentError = documentError,
                )
            }
            return
        }
        val years = current.yearsOfResidency.toIntOrNull() ?: return
        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            runCatching {
                verificationRepository.submit(
                    ResidencyDraft(
                        address = current.address,
                        purok = current.purok,
                        yearsOfResidency = years,
                        idType = current.idType,
                        idNumber = current.idNumber,
                        documentUri = current.documentUri,
                        documentName = current.documentName,
                    ),
                    profile,
                )
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(isSubmitting = false, submitted = true)
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(isSubmitting = false, submitError = failure.userMessage())
                }
            }
        }
    }

    companion object {
        fun factory(
            profile: UserProfile,
            verificationRepository: VerificationRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ResidencyFormViewModel(profile, verificationRepository) }
        }
    }
}

class VerificationStatusViewModel(
    profile: UserProfile,
    verificationRepository: VerificationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VerificationStatusUiState())
    val uiState: StateFlow<VerificationStatusUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            verificationRepository.observeMyVerification(profile.id)
                .collect { verification ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            verification = verification,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(
            profile: UserProfile,
            verificationRepository: VerificationRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { VerificationStatusViewModel(profile, verificationRepository) }
        }
    }
}

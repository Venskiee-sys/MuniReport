package com.munireport.app.resident.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.core.utils.MuntinlupaGeo
import com.munireport.app.core.validation.Validators
import com.munireport.app.domain.model.ComplaintCategory
import com.munireport.app.domain.model.NewComplaint
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.usecase.CreateComplaintUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val profile: UserProfile,
    private val createComplaintUseCase: CreateComplaintUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReportUiState(
            address = profile.address.ifBlank {
                "Brgy. ${profile.purok.ifBlank { "Muntinlupa" }}, Muntinlupa City"
            },
        ),
    )
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) = _uiState.update {
        it.copy(title = value.take(90), titleError = null, submitError = null)
    }

    fun onDescriptionChange(value: String) = _uiState.update {
        it.copy(description = value.take(1200), descriptionError = null, submitError = null)
    }

    fun onCategoryChange(value: ComplaintCategory) = _uiState.update {
        it.copy(category = value, categoryError = null, submitError = null)
    }

    fun onAddressChange(value: String) = _uiState.update {
        it.copy(address = value, addressError = null, submitError = null)
    }

    fun onLandmarkChange(value: String) = _uiState.update { it.copy(landmark = value) }

    fun onPhotosAdded(uris: List<String>) = _uiState.update { state ->
        state.copy(photoUris = (state.photoUris + uris).distinct().take(1))
    }

    fun onPhotoRemoved(uri: String) = _uiState.update { state ->
        state.copy(photoUris = state.photoUris.filterNot { it == uri })
    }

    fun onCoordinates(latitude: Double, longitude: Double) = _uiState.update {
        if (MuntinlupaGeo.isInside(latitude, longitude)) {
            it.copy(
                latitude = latitude,
                longitude = longitude,
                locationError = null,
                submitError = null,
            )
        } else {
            it.copy(
                latitude = null,
                longitude = null,
                locationError = "That point is outside Muntinlupa City. Move the pin inside the city boundary.",
            )
        }
    }

    fun clearLocationError() = _uiState.update { it.copy(locationError = null) }

    fun clearCreatedComplaint() = _uiState.update { it.copy(createdComplaint = null) }

    fun submit() {
        val current = _uiState.value
        val titleError = Validators.complaintTitle(current.title)
        val descriptionError = Validators.complaintDescription(current.description)
        val categoryError = if (current.category == null) "Choose a category." else null
        val locationError = when {
            current.locationError != null -> current.locationError
            current.latitude != null && current.longitude != null &&
                !MuntinlupaGeo.isInside(current.latitude, current.longitude) ->
                "Move the pin inside Muntinlupa City before submitting."
            else -> null
        }
        if (listOf(titleError, descriptionError, categoryError, locationError).any { it != null }) {
            _uiState.update {
                it.copy(
                    titleError = titleError,
                    descriptionError = descriptionError,
                    categoryError = categoryError,
                    locationError = locationError,
                )
            }
            return
        }
        val category = current.category ?: return
        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            createComplaintUseCase(
                NewComplaint(
                    title = current.title,
                    description = current.description,
                    category = category,
                    address = current.address,
                    landmark = current.landmark,
                    latitude = current.latitude,
                    longitude = current.longitude,
                    photoUris = current.photoUris,
                ),
                profile,
            ).onSuccess { complaint ->
                _uiState.update {
                    it.copy(isSubmitting = false, createdComplaint = complaint)
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
            createComplaintUseCase: CreateComplaintUseCase,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ReportViewModel(profile, createComplaintUseCase) }
        }
    }
}

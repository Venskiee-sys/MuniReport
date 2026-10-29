package com.munireport.app.resident.profile

data class EditProfileUiState(
    val fullName: String,
    val phone: String,
    val address: String,
    val purok: String,
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val purokError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

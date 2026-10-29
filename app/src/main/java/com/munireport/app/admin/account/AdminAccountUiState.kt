package com.munireport.app.admin.account

data class AdminAccountUiState(
    val fullName: String,
    val phone: String,
    val address: String,
    val purok: String,
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val purokError: String? = null,
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
    val saveError: String? = null,
    val isSendingReset: Boolean = false,
    val resetMessage: String? = null,
    val resetError: String? = null,
)

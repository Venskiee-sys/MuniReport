package com.munireport.app.resident.report

import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintCategory

data class ReportUiState(
    val title: String = "",
    val description: String = "",
    val category: ComplaintCategory? = null,
    val address: String = "",
    val landmark: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoUris: List<String> = emptyList(),
    val titleError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val addressError: String? = null,
    val locationError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdComplaint: Complaint? = null,
)

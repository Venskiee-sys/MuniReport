package com.munireport.app.resident.complaintdetails

import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintUpdate

data class ComplaintDetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val complaint: Complaint? = null,
    val updates: List<ComplaintUpdate> = emptyList(),
    val isTogglingFollow: Boolean = false,
    val actionError: String? = null,
)

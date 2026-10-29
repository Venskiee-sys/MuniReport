package com.munireport.app.admin.checkcomplaint

import com.munireport.app.domain.model.Comment
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.ComplaintUpdate

data class AdminComplaintDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val complaint: Complaint? = null,
    val updates: List<ComplaintUpdate> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val selectedStatus: ComplaintStatus? = null,
    val selectedPriority: ComplaintPriority? = null,
    val note: String = "",
    val isSubmitting: Boolean = false,
    val actionError: String? = null,
    val actionSuccess: String? = null,
    val commentDraft: String = "",
    val isSendingComment: Boolean = false,
    val commentError: String? = null,
)

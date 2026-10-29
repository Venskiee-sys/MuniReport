package com.munireport.app.admin.checkcomplaint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.Comment
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.ComplaintUpdate
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckComplaintViewModel(
    private val complaintId: String,
    private val adminProfile: UserProfile,
    private val complaintRepository: ComplaintRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminComplaintDetailUiState())
    val uiState: StateFlow<AdminComplaintDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                complaintRepository.observeComplaint(complaintId, adminProfile.id),
                complaintRepository.observeUpdates(complaintId),
                complaintRepository.observeComments(complaintId),
            ) { complaint, updates, comments ->
                Triple(complaint, updates, comments)
            }.catch { failure ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = failure.userMessage())
                }
            }.collect { (complaint, updates, comments) ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        complaint = complaint,
                        updates = updates,
                        comments = comments,
                        selectedStatus = current.selectedStatus ?: complaint?.status,
                        selectedPriority = current.selectedPriority ?: complaint?.priority,
                    )
                }
            }
        }
    }

    fun onStatusChange(value: ComplaintStatus) {
        _uiState.update { it.copy(selectedStatus = value, actionError = null, actionSuccess = null) }
    }

    fun onPriorityChange(value: ComplaintPriority) {
        _uiState.update { it.copy(selectedPriority = value, actionError = null, actionSuccess = null) }
    }

    fun onNoteChange(value: String) {
        _uiState.update {
            it.copy(note = value.take(600), actionError = null, actionSuccess = null)
        }
    }

    fun postUpdate() {
        val state = _uiState.value
        val complaint = state.complaint ?: return
        if (state.isSubmitting) return
        if (state.note.isBlank()) {
            _uiState.update {
                it.copy(actionError = "Add a short note explaining the update for the resident.")
            }
            return
        }
        val status = state.selectedStatus ?: complaint.status
        val priority = state.selectedPriority ?: complaint.priority
        _uiState.update { it.copy(isSubmitting = true, actionError = null, actionSuccess = null) }
        val note = state.note.trim()
        viewModelScope.launch {
            runCatching {
                complaintRepository.updateStatus(
                    complaintId = complaint.id,
                    status = status,
                    priority = priority,
                    note = note,
                    author = adminProfile,
                )
                // The guide surfaces one admin note and says it is posted to the
                // thread, so mirror the public update into the comment thread.
                runCatching {
                    complaintRepository.addComment(
                        complaintId = complaint.id,
                        author = adminProfile,
                        text = note,
                    )
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        note = "",
                        selectedStatus = status,
                        selectedPriority = priority,
                        actionSuccess = "Official update posted. The resident has been notified.",
                    )
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(isSubmitting = false, actionError = failure.userMessage())
                }
            }
        }
    }

    fun onCommentDraftChange(value: String) {
        _uiState.update {
            it.copy(commentDraft = value.take(600), commentError = null)
        }
    }

    fun postOfficialComment() {
        val state = _uiState.value
        val complaint = state.complaint ?: return
        if (state.commentDraft.isBlank() || state.isSendingComment) return
        _uiState.update { it.copy(isSendingComment = true, commentError = null) }
        viewModelScope.launch {
            runCatching {
                complaintRepository.addComment(
                    complaintId = complaint.id,
                    author = adminProfile,
                    text = state.commentDraft,
                )
            }.onSuccess {
                _uiState.update { it.copy(isSendingComment = false, commentDraft = "") }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(isSendingComment = false, commentError = failure.userMessage())
                }
            }
        }
    }

    fun dismissActionMessage() {
        _uiState.update { it.copy(actionError = null, actionSuccess = null) }
    }

    companion object {
        fun factory(
            complaintId: String,
            adminProfile: UserProfile,
            complaintRepository: ComplaintRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CheckComplaintViewModel(complaintId, adminProfile, complaintRepository)
            }
        }
    }
}

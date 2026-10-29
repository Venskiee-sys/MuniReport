package com.munireport.app.resident.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.domain.model.Comment
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommentsViewModel(
    private val complaintId: String,
    private val profile: UserProfile,
    private val complaintRepository: ComplaintRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                complaintRepository.observeComments(complaintId).collect { comments ->
                    _uiState.update {
                        it.copy(comments = comments, isLoading = false)
                    }
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = failure.userMessage())
                }
            }
        }
    }

    fun onDraftChange(value: String) {
        _uiState.update { it.copy(draft = value.take(600), errorMessage = null) }
    }

    fun sendComment() {
        val current = _uiState.value
        if (current.draft.isBlank() || current.isSending) return
        _uiState.update { it.copy(isSending = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                complaintRepository.addComment(complaintId, profile, current.draft)
            }.onSuccess {
                _uiState.update { it.copy(draft = "", isSending = false) }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(isSending = false, errorMessage = failure.userMessage())
                }
            }
        }
    }

    companion object {
        fun factory(
            complaintId: String,
            profile: UserProfile,
            complaintRepository: ComplaintRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { CommentsViewModel(complaintId, profile, complaintRepository) }
        }
    }
}

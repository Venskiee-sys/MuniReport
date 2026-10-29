package com.munireport.app.resident.comments

import com.munireport.app.domain.model.Comment

data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val draft: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)

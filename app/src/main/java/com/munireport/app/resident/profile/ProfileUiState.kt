package com.munireport.app.resident.profile

import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val errorMessage: String? = null,
    val reportCount: Int = 0,
    val resolvedCount: Int = 0,
    val followingCount: Int = 0,
    val commentCount: Int = 0,
    val followedComplaints: List<Complaint> = emptyList(),
)

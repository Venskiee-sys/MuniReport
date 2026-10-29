package com.munireport.app.domain.repository

import com.munireport.app.domain.model.ResidencyDraft
import com.munireport.app.domain.model.ResidencyVerification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import kotlinx.coroutines.flow.Flow

interface VerificationRepository {
    fun observeMyVerification(userId: String): Flow<ResidencyVerification?>
    fun observeVerification(verificationId: String): Flow<ResidencyVerification?>
    fun observeQueue(): Flow<List<ResidencyVerification>>
    suspend fun getLatestVerification(userId: String): ResidencyVerification?
    suspend fun submit(draft: ResidencyDraft, profile: UserProfile): ResidencyVerification
    suspend fun review(
        verificationId: String,
        decision: VerificationStatus,
        note: String,
        reviewer: UserProfile,
    )
}

package com.munireport.app.domain.usecase

import com.munireport.app.core.common.AppException
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.VerificationRepository

class ReviewVerificationUseCase(private val verificationRepository: VerificationRepository) {
    suspend operator fun invoke(
        verificationId: String,
        decision: VerificationStatus,
        note: String,
        reviewer: UserProfile,
    ): Result<Unit> = runCatching {
        if (!reviewer.isAdmin) throw AppException.PermissionDenied()
        if (decision !in listOf(VerificationStatus.APPROVED, VerificationStatus.REJECTED)) {
            throw AppException.InvalidInput("Choose approve or reject.")
        }
        if (decision == VerificationStatus.REJECTED && note.isBlank()) {
            throw AppException.InvalidInput("Add a short reason so the resident can correct it.")
        }
        verificationRepository.review(verificationId, decision, note.trim(), reviewer)
    }
}

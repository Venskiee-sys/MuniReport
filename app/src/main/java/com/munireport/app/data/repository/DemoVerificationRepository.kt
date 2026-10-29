package com.munireport.app.data.repository

import com.munireport.app.core.common.AppException
import com.munireport.app.data.local.DemoMuniStore
import com.munireport.app.domain.model.NotificationType
import com.munireport.app.domain.model.ResidencyDraft
import com.munireport.app.domain.model.ResidencyVerification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.VerificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DemoVerificationRepository(private val store: DemoMuniStore) : VerificationRepository {

    override fun observeMyVerification(userId: String): Flow<ResidencyVerification?> =
        store.verifications.map { all -> all.firstOrNull { it.userId == userId } }

    override fun observeVerification(verificationId: String): Flow<ResidencyVerification?> =
        store.verifications.map { all -> all.firstOrNull { it.id == verificationId } }

    override fun observeQueue(): Flow<List<ResidencyVerification>> =
        store.verifications.map { all -> all.sortedByDescending { it.submittedAt } }

    override suspend fun getLatestVerification(userId: String): ResidencyVerification? =
        store.verifications.value.firstOrNull { it.userId == userId }

    override suspend fun submit(
        draft: ResidencyDraft,
        profile: UserProfile,
    ): ResidencyVerification {
        val existing = store.verifications.value.firstOrNull { it.userId == profile.id }
        if (existing?.status == VerificationStatus.APPROVED) {
            throw AppException.InvalidInput("Your residency is already verified.")
        }
        if (existing?.status == VerificationStatus.PENDING) {
            throw AppException.Conflict()
        }
        val now = System.currentTimeMillis()
        val verification = ResidencyVerification(
            id = profile.id,
            userId = profile.id,
            applicantName = profile.fullName,
            applicantEmail = profile.email,
            applicantPhone = profile.phone,
            address = draft.address.trim(),
            purok = draft.purok.trim(),
            yearsOfResidency = draft.yearsOfResidency,
            idType = draft.idType,
            idNumber = draft.idNumber.trim(),
            documentUrl = draft.documentUri,
            documentName = draft.documentName,
            status = VerificationStatus.PENDING,
            submittedAt = now,
        )
        store.upsertVerification(verification)
        store.notify(
            profile.id,
            store.notificationFor(
                recipientId = profile.id,
                title = "Verification submitted",
                message = "Your residency documents are now waiting for review.",
                type = NotificationType.VERIFICATION,
            ),
        )
        return verification
    }

    override suspend fun review(
        verificationId: String,
        decision: VerificationStatus,
        note: String,
        reviewer: UserProfile,
    ) {
        if (!reviewer.isAdmin) throw AppException.PermissionDenied()
        if (decision !in listOf(VerificationStatus.APPROVED, VerificationStatus.REJECTED)) {
            throw AppException.InvalidInput("Choose approve or reject.")
        }
        val current = store.verifications.value.firstOrNull { it.id == verificationId }
            ?: throw AppException.NotFound()
        val now = System.currentTimeMillis()
        val updated = current.copy(
            status = decision,
            reviewedAt = now,
            reviewerId = reviewer.id,
            reviewerName = reviewer.fullName,
            reviewNote = note.trim().ifBlank { null },
        )
        store.upsertVerification(updated)
        val title = if (decision == VerificationStatus.APPROVED) {
            "Your residency is verified"
        } else {
            "Verification needs attention"
        }
        val message = if (decision == VerificationStatus.APPROVED) {
            "You can now file reports for Barangay San Isidro."
        } else {
            note.trim().ifBlank { "Open MuniReport to review the note and submit again." }
        }
        store.notify(
            current.userId,
            store.notificationFor(
                recipientId = current.userId,
                title = title,
                message = message,
                type = NotificationType.VERIFICATION,
            ),
        )
    }
}

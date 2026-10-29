package com.munireport.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.munireport.app.core.common.AppException
import com.munireport.app.data.mapper.toFirestoreMap
import com.munireport.app.data.mapper.toVerification
import com.munireport.app.data.remote.FirebaseStorageDataSource
import com.munireport.app.data.remote.toAppException
import com.munireport.app.domain.model.ResidencyDraft
import com.munireport.app.domain.model.ResidencyVerification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.VerificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseVerificationRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorageDataSource,
) : VerificationRepository {

    private val verifications = firestore.collection("verifications")

    override fun observeMyVerification(userId: String): Flow<ResidencyVerification?> =
        observeVerification(userId)

    override fun observeVerification(verificationId: String): Flow<ResidencyVerification?> =
        callbackFlow {
            val registration = verifications.document(verificationId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error.toAppException())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toVerification())
                }
            awaitClose { registration.remove() }
        }

    override fun observeQueue(): Flow<List<ResidencyVerification>> = callbackFlow {
        val registration = verifications
            .orderBy("submittedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error.toAppException())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toVerification() }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getLatestVerification(userId: String): ResidencyVerification? =
        runCatching {
            verifications.document(userId).get().await().toVerification()
        }.getOrElse { throw it.toAppException() }

    override suspend fun submit(
        draft: ResidencyDraft,
        profile: UserProfile,
    ): ResidencyVerification {
        return runCatching {
            if (profile.isVerified) throw AppException.InvalidInput("Your residency is already verified.")
            val current = verifications.document(profile.id).get().await().toVerification()
            if (current?.status == VerificationStatus.PENDING) throw AppException.Conflict()
            val now = System.currentTimeMillis()
            val uploadedUrl = when {
                draft.documentUri.isNullOrBlank() -> current?.documentUrl
                draft.documentUri.startsWith("http") -> draft.documentUri
                else -> storage.uploadVerificationDocument(profile.id, draft.documentUri)
            }
            if (uploadedUrl.isNullOrBlank()) {
                throw AppException.InvalidInput("Attach a photo of your government ID.")
            }
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
                documentUrl = uploadedUrl,
                documentName = draft.documentName ?: current?.documentName,
                status = VerificationStatus.PENDING,
                submittedAt = now,
            )
            val batch = firestore.batch()
            batch.set(verifications.document(profile.id), verification.toFirestoreMap())
            batch.update(
                firestore.collection("users").document(profile.id),
                mapOf(
                    "verificationStatus" to VerificationStatus.PENDING.name,
                    "updatedAt" to Timestamp(Date(now)),
                ),
            )
            batch.commit().await()
            verification
        }.getOrElse { throw it.toAppException() }
    }

    override suspend fun review(
        verificationId: String,
        decision: VerificationStatus,
        note: String,
        reviewer: UserProfile,
    ) {
        runCatching {
            if (!reviewer.isAdmin) throw AppException.PermissionDenied()
            if (decision !in listOf(VerificationStatus.APPROVED, VerificationStatus.REJECTED)) {
                throw AppException.InvalidInput("Choose approve or reject.")
            }
            val current = verifications.document(verificationId).get().await().toVerification()
                ?: throw AppException.NotFound()
            val now = System.currentTimeMillis()
            val updateFields = mutableMapOf<String, Any>(
                "status" to decision.name,
                "reviewedAt" to Timestamp(Date(now)),
                "reviewerId" to reviewer.id,
                "reviewerName" to reviewer.fullName,
            )
            if (note.isNotBlank()) updateFields["reviewNote"] = note.trim()
            val batch = firestore.batch()
            batch.update(verifications.document(verificationId), updateFields)
            batch.update(
                firestore.collection("users").document(current.userId),
                mapOf(
                    "verificationStatus" to decision.name,
                    "updatedAt" to Timestamp(Date(now)),
                ),
            )
            batch.commit().await()
        }.getOrElse { throw it.toAppException() }
    }
}

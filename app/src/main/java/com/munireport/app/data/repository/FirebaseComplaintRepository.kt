package com.munireport.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.munireport.app.core.common.AppException
import com.munireport.app.data.mapper.toComment
import com.munireport.app.data.mapper.toComplaint
import com.munireport.app.data.mapper.toComplaintUpdate
import com.munireport.app.data.mapper.toFirestoreMap
import com.munireport.app.data.remote.FirebaseStorageDataSource
import com.munireport.app.data.remote.followDocumentId
import com.munireport.app.data.remote.toAppException
import com.munireport.app.domain.model.Comment
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.ComplaintUpdate
import com.munireport.app.domain.model.NewComplaint
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirebaseComplaintRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorageDataSource,
) : ComplaintRepository {

    private val complaints = firestore.collection("complaints")

    override fun observeComplaints(viewerId: String): Flow<List<Complaint>> {
        val complaintFlow = callbackFlow {
            val registration = complaints
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(200)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error.toAppException())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.toComplaint() }.orEmpty())
                }
            awaitClose { registration.remove() }
        }
        val followFlow = callbackFlow {
            val registration = firestore.collection("follows")
                .whereEqualTo("userId", viewerId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error.toAppException())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.getString("complaintId") }?.toSet().orEmpty())
                }
            awaitClose { registration.remove() }
        }
        return combine(complaintFlow, followFlow) { list, followed ->
            list.map { it.copy(followedByMe = followed.contains(it.id)) }
                .sortedByDescending { it.createdAt }
        }
    }

    override fun observeComplaint(complaintId: String, viewerId: String): Flow<Complaint?> {
        val complaintFlow = callbackFlow {
            val registration = complaints.document(complaintId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error.toAppException())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toComplaint())
                }
            awaitClose { registration.remove() }
        }
        val followFlow = callbackFlow {
            val registration = firestore.collection("follows")
                .document(followDocumentId(viewerId, complaintId))
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error.toAppException())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.exists() == true)
                }
            awaitClose { registration.remove() }
        }
        return combine(complaintFlow, followFlow) { complaint, followed ->
            complaint?.copy(followedByMe = followed)
        }
    }

    override fun observeUpdates(complaintId: String): Flow<List<ComplaintUpdate>> = callbackFlow {
        val registration = complaints.document(complaintId).collection("updates")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error.toAppException())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toComplaintUpdate() }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    override fun observeComments(complaintId: String): Flow<List<Comment>> = callbackFlow {
        val registration = complaints.document(complaintId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error.toAppException())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toComment() }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createComplaint(draft: NewComplaint, author: UserProfile): Complaint {
        return runCatching {
            if (!author.isVerified) throw AppException.PermissionDenied()
            val now = System.currentTimeMillis()
            val reference = complaints.document()
            val photoUrls = storage.uploadComplaintPhotos(author.id, draft.photoUris)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(now))
            val suffix = (1000 + ((reference.id.hashCode() and 0x7fffffff) % 9000))
            val complaint = Complaint(
                id = reference.id,
                ticketNumber = "MR-$year-$suffix",
                title = draft.title.trim(),
                description = draft.description.trim(),
                category = draft.category,
                status = ComplaintStatus.PENDING,
                priority = ComplaintPriority.MEDIUM,
                authorId = author.id,
                authorName = author.fullName,
                authorPurok = author.purok,
                address = draft.address.trim(),
                landmark = draft.landmark.trim(),
                latitude = draft.latitude,
                longitude = draft.longitude,
                photoUrls = photoUrls,
                createdAt = now,
                updatedAt = now,
            )
            val update = ComplaintUpdate(
                id = "initial",
                complaintId = complaint.id,
                status = ComplaintStatus.PENDING,
                note = "Report received and added to the barangay queue.",
                authorId = "system",
                authorName = "MuniReport",
                isOfficial = false,
                createdAt = now,
            )
            val batch = firestore.batch()
            batch.set(reference, complaint.toFirestoreMap())
            batch.set(
                reference.collection("updates").document("initial"),
                update.toFirestoreMap(),
            )
            batch.commit().await()
            complaint
        }.getOrElse { throw it.toAppException() }
    }

    override suspend fun setFollowing(complaintId: String, userId: String, following: Boolean) {
        runCatching {
            val reference = firestore.collection("follows")
                .document(followDocumentId(userId, complaintId))
            if (following) {
                reference.set(
                    mapOf(
                        "userId" to userId,
                        "complaintId" to complaintId,
                        "createdAt" to Timestamp(Date(System.currentTimeMillis())),
                    ),
                ).await()
            } else {
                reference.delete().await()
            }
        }.getOrElse { throw it.toAppException() }
    }

    override suspend fun addComment(complaintId: String, author: UserProfile, text: String) {
        runCatching {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) throw AppException.InvalidInput("Write a comment first.")
            val reference = complaints.document(complaintId).collection("comments").document()
            val comment = Comment(
                id = reference.id,
                complaintId = complaintId,
                authorId = author.id,
                authorName = author.fullName,
                authorRole = author.role,
                text = trimmed,
                createdAt = System.currentTimeMillis(),
            )
            reference.set(comment.toFirestoreMap()).await()
        }.getOrElse { throw it.toAppException() }
    }

    override suspend fun updateStatus(
        complaintId: String,
        status: ComplaintStatus,
        priority: ComplaintPriority,
        note: String,
        author: UserProfile,
    ) {
        runCatching {
            if (!author.isAdmin) throw AppException.PermissionDenied()
            val now = System.currentTimeMillis()
            val reference = complaints.document(complaintId)
            val update = ComplaintUpdate(
                id = UUID.randomUUID().toString(),
                complaintId = complaintId,
                status = status,
                note = note.trim().ifBlank { "Status updated by the barangay office." },
                authorId = author.id,
                authorName = author.fullName,
                isOfficial = true,
                createdAt = now,
            )
            val batch = firestore.batch()
            batch.update(
                reference,
                mapOf(
                    "status" to status.name,
                    "priority" to priority.name,
                    "updatedAt" to Timestamp(Date(now)),
                ),
            )
            batch.set(reference.collection("updates").document(), update.toFirestoreMap())
            batch.commit().await()
        }.getOrElse { throw it.toAppException() }
    }
}

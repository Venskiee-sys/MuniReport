package com.munireport.app.data.repository

import com.munireport.app.core.common.AppException
import com.munireport.app.data.local.DemoMuniStore
import com.munireport.app.domain.model.Comment
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.ComplaintUpdate
import com.munireport.app.domain.model.NewComplaint
import com.munireport.app.domain.model.NotificationType
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DemoComplaintRepository(private val store: DemoMuniStore) : ComplaintRepository {

    private fun decorate(
        complaint: Complaint,
        viewerId: String,
        follows: Set<String>,
        comments: Map<String, List<Comment>>,
    ): Complaint {
        val followCount = follows.count { it.endsWith("|${complaint.id}") }
        val commentCount = comments[complaint.id].orEmpty().size
        return complaint.copy(
            followedByMe = follows.contains("$viewerId|${complaint.id}"),
            followerCount = complaint.followerCount + followCount,
            commentCount = complaint.commentCount + commentCount,
        )
    }

    override fun observeComplaints(viewerId: String): Flow<List<Complaint>> =
        combine(store.complaints, store.follows, store.comments) { complaints, follows, comments ->
            complaints
                .map { complaint -> decorate(complaint, viewerId, follows, comments) }
                .sortedByDescending { it.createdAt }
        }

    override fun observeComplaint(complaintId: String, viewerId: String): Flow<Complaint?> =
        combine(store.complaints, store.follows, store.comments) { complaints, follows, comments ->
            complaints.firstOrNull { it.id == complaintId }?.let {
                decorate(it, viewerId, follows, comments)
            }
        }

    override fun observeUpdates(complaintId: String): Flow<List<ComplaintUpdate>> =
        store.updates.map { current ->
            current[complaintId].orEmpty().sortedBy { it.createdAt }
        }

    override fun observeComments(complaintId: String): Flow<List<Comment>> =
        store.comments.map { current ->
            current[complaintId].orEmpty().sortedBy { it.createdAt }
        }

    override suspend fun createComplaint(
        draft: NewComplaint,
        author: UserProfile,
    ): Complaint {
        if (!author.isVerified) throw AppException.PermissionDenied()
        val now = System.currentTimeMillis()
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(now))
        val ticket = "MR-$year-${(now % 10000).toString().padStart(4, '0')}"
        val complaint = Complaint(
            id = store.nextComplaintId(),
            ticketNumber = ticket,
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
            photoUrls = draft.photoUris,
            createdAt = now,
            updatedAt = now,
        )
        store.addComplaint(complaint)
        store.addUpdate(
            ComplaintUpdate(
                id = "upd-${UUID.randomUUID()}",
                complaintId = complaint.id,
                status = ComplaintStatus.PENDING,
                note = "Report received and added to the barangay queue.",
                authorId = "system",
                authorName = "MuniReport",
                isOfficial = false,
                createdAt = now,
            ),
        )
        store.notify(
            author.id,
            store.notificationFor(
                recipientId = author.id,
                title = "Report received",
                message = "$ticket is now in the barangay queue.",
                type = NotificationType.STATUS_CHANGE,
                complaintId = complaint.id,
            ),
        )
        return complaint
    }

    override suspend fun setFollowing(complaintId: String, userId: String, following: Boolean) {
        store.setFollowing(userId, complaintId, following)
    }

    override suspend fun addComment(complaintId: String, author: UserProfile, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) throw AppException.InvalidInput("Write a comment first.")
        val now = System.currentTimeMillis()
        val comment = Comment(
            id = "cmt-${UUID.randomUUID()}",
            complaintId = complaintId,
            authorId = author.id,
            authorName = author.fullName,
            authorRole = author.role,
            text = trimmed,
            createdAt = now,
        )
        store.addComment(comment)
        val complaint = store.complaints.value.firstOrNull { it.id == complaintId }
        if (complaint != null && complaint.authorId != author.id) {
            store.notify(
                complaint.authorId,
                store.notificationFor(
                    recipientId = complaint.authorId,
                    title = "New comment on ${complaint.ticketNumber}",
                    message = "${author.fullName}: ${trimmed.take(90)}",
                    type = NotificationType.COMMENT,
                    complaintId = complaintId,
                ),
            )
        }
    }

    override suspend fun updateStatus(
        complaintId: String,
        status: ComplaintStatus,
        priority: ComplaintPriority,
        note: String,
        author: UserProfile,
    ) {
        if (!author.isAdmin) throw AppException.PermissionDenied()
        val complaint = store.complaints.value.firstOrNull { it.id == complaintId }
            ?: throw AppException.NotFound()
        val now = System.currentTimeMillis()
        store.updateComplaint(
            complaint.copy(
                status = status,
                priority = priority,
                updatedAt = now,
            ),
        )
        store.addUpdate(
            ComplaintUpdate(
                id = "upd-${UUID.randomUUID()}",
                complaintId = complaintId,
                status = status,
                note = note.trim().ifBlank { "Status updated by the barangay office." },
                authorId = author.id,
                authorName = author.fullName,
                isOfficial = true,
                createdAt = now,
            ),
        )
        if (complaint.authorId != author.id) {
            store.notify(
                complaint.authorId,
                store.notificationFor(
                    recipientId = complaint.authorId,
                    title = "${complaint.ticketNumber} is now ${status.label.lowercase()}",
                    message = note.trim().ifBlank { "The barangay office updated your report." },
                    type = NotificationType.STATUS_CHANGE,
                    complaintId = complaintId,
                ),
            )
        }
    }
}

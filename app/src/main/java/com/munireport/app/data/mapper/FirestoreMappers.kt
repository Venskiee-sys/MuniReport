package com.munireport.app.data.mapper

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.munireport.app.domain.model.AppNotification
import com.munireport.app.domain.model.Comment
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintCategory
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.ComplaintUpdate
import com.munireport.app.domain.model.IdType
import com.munireport.app.domain.model.NotificationType
import com.munireport.app.domain.model.ResidencyVerification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.UserRole
import com.munireport.app.domain.model.VerificationStatus
import java.util.Date

private fun DocumentSnapshot.millis(field: String): Long =
    getTimestamp(field)?.toDate()?.time ?: 0L

private fun DocumentSnapshot.nullableMillis(field: String): Long? =
    getTimestamp(field)?.toDate()?.time

private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
    name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default

fun UserProfile.toFirestoreMap(): Map<String, Any> = mapOf(
    "fullName" to fullName,
    "email" to email,
    "phone" to phone,
    "address" to address,
    "purok" to purok,
    "role" to role.name,
    "verificationStatus" to verificationStatus.name,
    "createdAt" to Timestamp(Date(createdAt)),
    "updatedAt" to Timestamp(Date(updatedAt)),
)

fun DocumentSnapshot.toUserProfile(): UserProfile? {
    if (!exists()) return null
    return UserProfile(
        id = id,
        fullName = getString("fullName").orEmpty().ifBlank { getString("email").orEmpty() },
        email = getString("email").orEmpty(),
        phone = getString("phone").orEmpty(),
        address = getString("address").orEmpty(),
        purok = getString("purok").orEmpty(),
        role = enumOrDefault(getString("role"), UserRole.RESIDENT),
        verificationStatus = enumOrDefault(getString("verificationStatus"), VerificationStatus.NOT_SUBMITTED),
        createdAt = millis("createdAt"),
        updatedAt = millis("updatedAt"),
    )
}

fun Complaint.toFirestoreMap(): Map<String, Any> {
    val data = mutableMapOf<String, Any>(
        "ticketNumber" to ticketNumber,
        "title" to title,
        "description" to description,
        "category" to category.name,
        "status" to status.name,
        "priority" to priority.name,
        "authorId" to authorId,
        "authorName" to authorName,
        "authorPurok" to authorPurok,
        "address" to address,
        "landmark" to landmark,
        "photoUrls" to photoUrls,
        "createdAt" to Timestamp(Date(createdAt)),
        "updatedAt" to Timestamp(Date(updatedAt)),
    )
    if (latitude != null) data["latitude"] = latitude
    if (longitude != null) data["longitude"] = longitude
    return data
}

fun DocumentSnapshot.toComplaint(): Complaint? {
    if (!exists()) return null
    return Complaint(
        id = id,
        ticketNumber = getString("ticketNumber").orEmpty(),
        title = getString("title").orEmpty(),
        description = getString("description").orEmpty(),
        category = enumOrDefault(getString("category"), ComplaintCategory.OTHER),
        status = enumOrDefault(getString("status"), ComplaintStatus.PENDING),
        priority = enumOrDefault(getString("priority"), ComplaintPriority.MEDIUM),
        authorId = getString("authorId").orEmpty(),
        authorName = getString("authorName").orEmpty(),
        authorPurok = getString("authorPurok").orEmpty(),
        address = getString("address").orEmpty(),
        landmark = getString("landmark").orEmpty(),
        latitude = getDouble("latitude"),
        longitude = getDouble("longitude"),
        photoUrls = (get("photoUrls") as? List<*>)?.filterIsInstance<String>().orEmpty(),
        createdAt = millis("createdAt"),
        updatedAt = millis("updatedAt"),
        followerCount = (getLong("followerCount") ?: 0L).toInt(),
        commentCount = (getLong("commentCount") ?: 0L).toInt(),
    )
}

fun ComplaintUpdate.toFirestoreMap(): Map<String, Any> = mapOf(
    "complaintId" to complaintId,
    "status" to status.name,
    "note" to note,
    "authorId" to authorId,
    "authorName" to authorName,
    "isOfficial" to isOfficial,
    "createdAt" to Timestamp(Date(createdAt)),
)

fun DocumentSnapshot.toComplaintUpdate(): ComplaintUpdate? {
    if (!exists()) return null
    return ComplaintUpdate(
        id = id,
        complaintId = getString("complaintId").orEmpty(),
        status = enumOrDefault(getString("status"), ComplaintStatus.PENDING),
        note = getString("note").orEmpty(),
        authorId = getString("authorId").orEmpty(),
        authorName = getString("authorName").orEmpty(),
        isOfficial = getBoolean("isOfficial") ?: false,
        createdAt = millis("createdAt"),
    )
}

fun Comment.toFirestoreMap(): Map<String, Any> = mapOf(
    "complaintId" to complaintId,
    "authorId" to authorId,
    "authorName" to authorName,
    "authorRole" to authorRole.name,
    "text" to text,
    "createdAt" to Timestamp(Date(createdAt)),
)

fun DocumentSnapshot.toComment(): Comment? {
    if (!exists()) return null
    return Comment(
        id = id,
        complaintId = getString("complaintId").orEmpty(),
        authorId = getString("authorId").orEmpty(),
        authorName = getString("authorName").orEmpty(),
        authorRole = enumOrDefault(getString("authorRole"), UserRole.RESIDENT),
        text = getString("text").orEmpty(),
        createdAt = millis("createdAt"),
    )
}

fun ResidencyVerification.toFirestoreMap(): Map<String, Any> {
    val data = mutableMapOf<String, Any>(
        "userId" to userId,
        "applicantName" to applicantName,
        "applicantEmail" to applicantEmail,
        "applicantPhone" to applicantPhone,
        "address" to address,
        "purok" to purok,
        "yearsOfResidency" to yearsOfResidency,
        "idType" to idType.name,
        "idNumber" to idNumber,
        "status" to status.name,
        "submittedAt" to Timestamp(Date(submittedAt)),
    )
    if (documentUrl != null) data["documentUrl"] = documentUrl
    if (documentName != null) data["documentName"] = documentName
    if (reviewedAt != null) data["reviewedAt"] = Timestamp(Date(reviewedAt))
    if (reviewerId != null) data["reviewerId"] = reviewerId
    if (reviewerName != null) data["reviewerName"] = reviewerName
    if (reviewNote != null) data["reviewNote"] = reviewNote
    return data
}

fun DocumentSnapshot.toVerification(): ResidencyVerification? {
    if (!exists()) return null
    return ResidencyVerification(
        id = id,
        userId = getString("userId").orEmpty(),
        applicantName = getString("applicantName").orEmpty(),
        applicantEmail = getString("applicantEmail").orEmpty(),
        applicantPhone = getString("applicantPhone").orEmpty(),
        address = getString("address").orEmpty(),
        purok = getString("purok").orEmpty(),
        yearsOfResidency = getLong("yearsOfResidency")?.toInt() ?: 0,
        idType = enumOrDefault(getString("idType"), IdType.PHILIPPINE_ID),
        idNumber = getString("idNumber").orEmpty(),
        documentUrl = getString("documentUrl"),
        documentName = getString("documentName"),
        status = enumOrDefault(getString("status"), VerificationStatus.PENDING),
        submittedAt = millis("submittedAt"),
        reviewedAt = nullableMillis("reviewedAt"),
        reviewerId = getString("reviewerId"),
        reviewerName = getString("reviewerName"),
        reviewNote = getString("reviewNote"),
    )
}

fun AppNotification.toFirestoreMap(): Map<String, Any> {
    val data = mutableMapOf<String, Any>(
        "recipientId" to recipientId,
        "title" to title,
        "message" to message,
        "type" to type.name,
        "isRead" to isRead,
        "createdAt" to Timestamp(Date(createdAt)),
    )
    if (complaintId != null) data["complaintId"] = complaintId
    return data
}

fun DocumentSnapshot.toNotification(): AppNotification? {
    if (!exists()) return null
    return AppNotification(
        id = id,
        recipientId = getString("recipientId").orEmpty(),
        title = getString("title").orEmpty(),
        message = getString("message").orEmpty(),
        type = enumOrDefault(getString("type"), NotificationType.SYSTEM),
        complaintId = getString("complaintId"),
        createdAt = millis("createdAt"),
        isRead = getBoolean("isRead") ?: false,
    )
}

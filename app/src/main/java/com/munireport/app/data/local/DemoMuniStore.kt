package com.munireport.app.data.local

import com.munireport.app.R
import com.munireport.app.core.constants.AppConstants
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import java.util.concurrent.TimeUnit

data class DemoUserAccount(
    val profile: UserProfile,
    val password: String,
)

/**
 * In-memory data used only when Firebase is not configured.
 *
 * It deliberately mirrors the production repository contract so screens and
 * ViewModels do not need a second code path. Data resets when the process ends.
 */
class DemoMuniStore(
    private val now: Long = System.currentTimeMillis(),
) {
    private fun hoursAgo(hours: Long): Long = now - TimeUnit.HOURS.toMillis(hours)
    private fun daysAgo(days: Long): Long = now - TimeUnit.DAYS.toMillis(days)

    private val resident = UserProfile(
        id = "res-001",
        fullName = "Maria Santos",
        email = AppConstants.DEMO_RESIDENT_EMAIL,
        phone = "0917 555 0142",
        address = "24 Mabini Street",
        purok = "Poblacion",
        role = UserRole.RESIDENT,
        verificationStatus = VerificationStatus.APPROVED,
        createdAt = daysAgo(210),
        updatedAt = daysAgo(140),
    )

    private val admin = UserProfile(
        id = "adm-001",
        fullName = "Elena Reyes",
        email = AppConstants.DEMO_ADMIN_EMAIL,
        phone = "0917 555 0100",
        address = "Barangay Hall, Rizal Avenue",
        purok = "Poblacion",
        role = UserRole.ADMIN,
        verificationStatus = VerificationStatus.APPROVED,
        createdAt = daysAgo(400),
        updatedAt = daysAgo(30),
    )

    private val juan = UserProfile(
        id = "res-002",
        fullName = "Juan Dela Cruz",
        email = "juan@munireport.app",
        phone = "0918 555 0198",
        address = "18 Sampaguita Lane",
        purok = "Putatan",
        role = UserRole.RESIDENT,
        verificationStatus = VerificationStatus.PENDING,
        createdAt = daysAgo(60),
        updatedAt = hoursAgo(5),
    )

    private val ana = UserProfile(
        id = "res-003",
        fullName = "Ana Villanueva",
        email = "ana@munireport.app",
        phone = "0919 555 0171",
        address = "7 Camia Street",
        purok = "Tunasan",
        role = UserRole.RESIDENT,
        verificationStatus = VerificationStatus.REJECTED,
        createdAt = daysAgo(45),
        updatedAt = daysAgo(2),
    )

    private val mark = UserProfile(
        id = "res-004",
        fullName = "Mark Rivera",
        email = "mark@munireport.app",
        phone = "0917 555 0126",
        address = "31 Del Pilar Street",
        purok = "Alabang",
        role = UserRole.RESIDENT,
        verificationStatus = VerificationStatus.PENDING,
        createdAt = daysAgo(12),
        updatedAt = daysAgo(1),
    )

    val users = MutableStateFlow(
        listOf(resident, admin, juan, ana, mark).associateBy { it.id },
    )

    val accounts = MutableStateFlow(
        mapOf(
            resident.email to DemoUserAccount(resident, AppConstants.DEMO_RESIDENT_PASSWORD),
            admin.email to DemoUserAccount(admin, AppConstants.DEMO_ADMIN_PASSWORD),
            juan.email to DemoUserAccount(juan, "Resident123!"),
            ana.email to DemoUserAccount(ana, "Resident123!"),
            mark.email to DemoUserAccount(mark, "Resident123!"),
        ),
    )

    val currentUserId = MutableStateFlow<String?>(null)

    val complaints = MutableStateFlow(
        listOf(
            Complaint(
                id = "cmp-1001",
                ticketNumber = "MR-2026-0141",
                title = "Overflowing garbage near the covered court",
                description = "The pile has grown since Monday and now blocks half of the sidewalk. Residents have started placing bags beside the court because the bins are full.",
                category = ComplaintCategory.WASTE_MANAGEMENT,
                status = ComplaintStatus.IN_PROGRESS,
                priority = ComplaintPriority.HIGH,
                authorId = resident.id,
                authorName = resident.fullName,
                authorPurok = resident.purok,
                address = "Mabini Street, near the covered court",
                landmark = "Beside the barangay covered court",
                latitude = 14.4062,
                longitude = 121.0451,
                photoUrls = listOf("android.resource://com.munireport.app/${R.drawable.sample_report_waste}"),
                followerCount = 24,
                commentCount = 0,
                createdAt = hoursAgo(6),
                updatedAt = hoursAgo(2),
            ),
            Complaint(
                id = "cmp-1002",
                ticketNumber = "MR-2026-0142",
                title = "Streetlight out at Sampaguita corner",
                description = "The lamp post beside the sari-sari store has been dark for four nights. It is difficult to see pedestrians crossing after 7 PM.",
                category = ComplaintCategory.STREETLIGHTS,
                status = ComplaintStatus.PENDING,
                priority = ComplaintPriority.MEDIUM,
                authorId = juan.id,
                authorName = juan.fullName,
                authorPurok = juan.purok,
                address = "Mabini Street corner Sampaguita Lane",
                landmark = "Beside Aling Nena's store",
                followerCount = 8,
                commentCount = 0,
                createdAt = hoursAgo(27),
                updatedAt = hoursAgo(27),
            ),
            Complaint(
                id = "cmp-1003",
                ticketNumber = "MR-2026-0138",
                title = "Deep pothole in front of the elementary school",
                description = "A pothole has widened after the rain. Two motorcycles have already tipped over while avoiding it during dismissal time.",
                category = ComplaintCategory.ROADS_AND_SIDEWALKS,
                status = ComplaintStatus.ACKNOWLEDGED,
                priority = ComplaintPriority.HIGH,
                authorId = resident.id,
                authorName = resident.fullName,
                authorPurok = resident.purok,
                address = "Rizal Avenue, in front of San Isidro Elementary School",
                landmark = "Along the school drop-off lane",
                followerCount = 14,
                commentCount = 0,
                createdAt = daysAgo(3),
                updatedAt = daysAgo(2),
            ),
            Complaint(
                id = "cmp-1004",
                ticketNumber = "MR-2026-0129",
                title = "Uncollected garden waste on Camia Street",
                description = "Branches and cut grass have been sitting on the curb for over a week. The pile is beginning to smell and attracts mosquitoes.",
                category = ComplaintCategory.WASTE_MANAGEMENT,
                status = ComplaintStatus.RESOLVED,
                priority = ComplaintPriority.MEDIUM,
                authorId = ana.id,
                authorName = ana.fullName,
                authorPurok = ana.purok,
                address = "Camia Street",
                landmark = "Across from the barangay health station",
                followerCount = 11,
                commentCount = 0,
                createdAt = daysAgo(8),
                updatedAt = daysAgo(4),
            ),
            Complaint(
                id = "cmp-1005",
                ticketNumber = "MR-2026-0140",
                title = "Stray dogs near the playground",
                description = "Three dogs have been staying under the playground slide. Children are afraid to use the equipment, especially in the late afternoon.",
                category = ComplaintCategory.OTHER,
                status = ComplaintStatus.IN_PROGRESS,
                priority = ComplaintPriority.URGENT,
                authorId = juan.id,
                authorName = juan.fullName,
                authorPurok = juan.purok,
                address = "Sampaguita Lane playground",
                landmark = "Beside the basketball half-court",
                followerCount = 19,
                commentCount = 0,
                createdAt = daysAgo(2),
                updatedAt = hoursAgo(10),
            ),
            Complaint(
                id = "cmp-1006",
                ticketNumber = "MR-2026-0112",
                title = "Clogged drainage before the rainy season",
                description = "Leaves and plastic have blocked the storm drain at the corner. Water no longer drains and reaches the sidewalk during heavy rain.",
                category = ComplaintCategory.DRAINAGE,
                status = ComplaintStatus.RESOLVED,
                priority = ComplaintPriority.HIGH,
                authorId = resident.id,
                authorName = resident.fullName,
                authorPurok = resident.purok,
                address = "Del Pilar Street, after the bridge",
                landmark = "Near the old waiting shed",
                followerCount = 17,
                commentCount = 0,
                createdAt = daysAgo(14),
                updatedAt = daysAgo(7),
            ),
            Complaint(
                id = "cmp-1007",
                ticketNumber = "MR-2026-0136",
                title = " Videoke until past midnight",
                description = "A neighbor on Mabini Street continues videoke sessions past 12 AM on weekdays. The sound carries into nearby homes and wakes children.",
                category = ComplaintCategory.OTHER,
                status = ComplaintStatus.ACKNOWLEDGED,
                priority = ComplaintPriority.LOW,
                authorId = ana.id,
                authorName = ana.fullName,
                authorPurok = ana.purok,
                address = "Mabini Street, near the basketball court",
                landmark = "Two houses from the corner store",
                followerCount = 6,
                commentCount = 0,
                createdAt = daysAgo(4),
                updatedAt = daysAgo(3),
            ),
            Complaint(
                id = "cmp-1008",
                ticketNumber = "MR-2026-0143",
                title = "Water leak wasting supply on Gumamela Street",
                description = "Water has been running from the pipe beside the meter for two days. The road edge is now muddy and slippery.",
                category = ComplaintCategory.DRAINAGE,
                status = ComplaintStatus.PENDING,
                priority = ComplaintPriority.MEDIUM,
                authorId = resident.id,
                authorName = resident.fullName,
                authorPurok = resident.purok,
                address = "Gumamela Street",
                landmark = "In front of house number 12",
                followerCount = 5,
                commentCount = 0,
                createdAt = hoursAgo(20),
                updatedAt = hoursAgo(20),
            ),
            Complaint(
                id = "cmp-1009",
                ticketNumber = "MR-2026-0131",
                title = "Request for another community trash bin",
                description = "Residents at the end of Ilang-Ilang Street have no nearby bin. We would like to ask if another collection point can be added.",
                category = ComplaintCategory.OTHER,
                status = ComplaintStatus.RESOLVED,
                priority = ComplaintPriority.LOW,
                authorId = juan.id,
                authorName = juan.fullName,
                authorPurok = juan.purok,
                address = "Ilang-Ilang Street",
                landmark = "End of the street",
                followerCount = 3,
                commentCount = 0,
                createdAt = daysAgo(9),
                updatedAt = daysAgo(8),
            ),
            Complaint(
                id = "cmp-1010",
                ticketNumber = "MR-2026-0144",
                title = "Knee-deep flood along National Road after heavy rain",
                description = "Water rises quickly near the pedestrian lane whenever it rains hard. Students and workers have to wade through the flood because the sidewalk is submerged.",
                category = ComplaintCategory.FLOODING,
                status = ComplaintStatus.IN_PROGRESS,
                priority = ComplaintPriority.URGENT,
                authorId = ana.id,
                authorName = ana.fullName,
                authorPurok = ana.purok,
                address = "National Road, near the pedestrian lane",
                landmark = "Across from the barangay health center",
                latitude = 14.3876,
                longitude = 121.0508,
                photoUrls = listOf("android.resource://com.munireport.app/${R.drawable.sample_report_drainage}"),
                followerCount = 31,
                commentCount = 0,
                createdAt = hoursAgo(15),
                updatedAt = hoursAgo(4),
            ),
        ),
    )

    val follows = MutableStateFlow(
        setOf(
            followKey(resident.id, "cmp-1001"),
            followKey(resident.id, "cmp-1003"),
            followKey(resident.id, "cmp-1006"),
            followKey(juan.id, "cmp-1005"),
        ),
    )

    val updates = MutableStateFlow(
        mapOf(
            "cmp-1001" to listOf(
                update("cmp-1001", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", hoursAgo(6), false),
                update("cmp-1001", ComplaintStatus.ACKNOWLEDGED, "The sanitation team inspected the area this morning.", "Barangay Office", hoursAgo(4), true),
                update("cmp-1001", ComplaintStatus.IN_PROGRESS, "A collection truck has been scheduled. Please keep the sidewalk clear until pickup.", "Barangay Office", hoursAgo(2), true),
            ),
            "cmp-1002" to listOf(
                update("cmp-1002", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", hoursAgo(27), false),
            ),
            "cmp-1003" to listOf(
                update("cmp-1003", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", daysAgo(3), false),
                update("cmp-1003", ComplaintStatus.ACKNOWLEDGED, "Forwarded to the engineering liaison for a site assessment.", "Barangay Office", daysAgo(2), true),
            ),
            "cmp-1004" to listOf(
                update("cmp-1004", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", daysAgo(8), false),
                update("cmp-1004", ComplaintStatus.ACKNOWLEDGED, "Collection request endorsed to the waste management team.", "Barangay Office", daysAgo(7), true),
                update("cmp-1004", ComplaintStatus.IN_PROGRESS, "The crew will include Camia Street in the Thursday route.", "Barangay Office", daysAgo(6), true),
                update("cmp-1004", ComplaintStatus.RESOLVED, "Garden waste was collected at 9:15 AM. The curb has been cleared.", "Barangay Office", daysAgo(4), true),
            ),
            "cmp-1005" to listOf(
                update("cmp-1005", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", daysAgo(2), false),
                update("cmp-1005", ComplaintStatus.ACKNOWLEDGED, "Coordinating with the city veterinary office.", "Barangay Office", daysAgo(1), true),
                update("cmp-1005", ComplaintStatus.IN_PROGRESS, "A capture schedule is being arranged with the barangay animal control team.", "Barangay Office", hoursAgo(10), true),
            ),
            "cmp-1006" to listOf(
                update("cmp-1006", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", daysAgo(14), false),
                update("cmp-1006", ComplaintStatus.ACKNOWLEDGED, "Maintenance team will inspect the drain line.", "Barangay Office", daysAgo(12), true),
                update("cmp-1006", ComplaintStatus.IN_PROGRESS, "Desilting work started at the Del Pilar corner.", "Barangay Office", daysAgo(9), true),
                update("cmp-1006", ComplaintStatus.RESOLVED, "The line was cleared and tested with a water hose. Flow is normal again.", "Barangay Office", daysAgo(7), true),
            ),
            "cmp-1007" to listOf(
                update("cmp-1007", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", daysAgo(4), false),
                update("cmp-1007", ComplaintStatus.ACKNOWLEDGED, "A barangay tanod will speak with the household about the ordinance.", "Barangay Office", daysAgo(3), true),
            ),
            "cmp-1008" to listOf(
                update("cmp-1008", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", hoursAgo(20), false),
            ),
            "cmp-1009" to listOf(
                update("cmp-1009", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", daysAgo(9), false),
                update("cmp-1009", ComplaintStatus.ACKNOWLEDGED, "The request was endorsed to the city waste management office.", "Barangay Office", daysAgo(8), true),
                update("cmp-1009", ComplaintStatus.RESOLVED, "An additional collection point was approved for Ilang-Ilang Street.", "Barangay Office", daysAgo(8), true),
            ),
            "cmp-1010" to listOf(
                update("cmp-1010", ComplaintStatus.PENDING, "Report received and added to the barangay queue.", "MuniReport", hoursAgo(15), false),
                update("cmp-1010", ComplaintStatus.ACKNOWLEDGED, "The barangay engineering team inspected the drainage line.", "Barangay Office", hoursAgo(8), true),
                update("cmp-1010", ComplaintStatus.IN_PROGRESS, "A declogging crew is scheduled today. Please avoid the flooded lane.", "Barangay Office", hoursAgo(4), true),
            ),
        ),
    )

    val comments = MutableStateFlow(
        mapOf(
            "cmp-1001" to listOf(
                comment("cmp-1001", resident.id, resident.fullName, UserRole.RESIDENT, "The pile has grown again since yesterday. It is now blocking the crossing for students.", hoursAgo(3)),
                comment("cmp-1001", admin.id, "Barangay Office", UserRole.ADMIN, "Thank you for the update. The truck is scheduled before noon tomorrow, and we will check the corner again afterward.", hoursAgo(2)),
            ),
            "cmp-1003" to listOf(
                comment("cmp-1003", juan.id, juan.fullName, UserRole.RESIDENT, "The pothole is especially dangerous when it rains because the water hides the depth.", daysAgo(2)),
            ),
            "cmp-1005" to listOf(
                comment("cmp-1005", ana.id, ana.fullName, UserRole.RESIDENT, "The dogs are still there this afternoon. They stay near the slide, not the court.", hoursAgo(12)),
                comment("cmp-1005", admin.id, "Barangay Office", UserRole.ADMIN, "Noted. The animal control team will check the playground side first.", hoursAgo(9)),
            ),
        ),
    )

    val verifications = MutableStateFlow(
        listOf(
            ResidencyVerification(
                id = juan.id,
                userId = juan.id,
                applicantName = juan.fullName,
                applicantEmail = juan.email,
                applicantPhone = juan.phone,
                address = juan.address,
                purok = juan.purok,
                yearsOfResidency = 6,
                idType = IdType.PHILIPPINE_ID,
                idNumber = "1234-5678-9012",
                documentName = "philsys-card.jpg",
                status = VerificationStatus.PENDING,
                submittedAt = hoursAgo(5),
            ),
            ResidencyVerification(
                id = mark.id,
                userId = mark.id,
                applicantName = mark.fullName,
                applicantEmail = mark.email,
                applicantPhone = mark.phone,
                address = mark.address,
                purok = mark.purok,
                yearsOfResidency = 3,
                idType = IdType.BARANGAY_ID,
                idNumber = "BRGY-2026-1042",
                documentName = "barangay-id.jpg",
                status = VerificationStatus.PENDING,
                submittedAt = daysAgo(1),
            ),
            ResidencyVerification(
                id = ana.id,
                userId = ana.id,
                applicantName = ana.fullName,
                applicantEmail = ana.email,
                applicantPhone = ana.phone,
                address = ana.address,
                purok = ana.purok,
                yearsOfResidency = 8,
                idType = IdType.VOTERS_ID,
                idNumber = "VTR-4401-8821",
                documentName = "voters-id.jpg",
                status = VerificationStatus.REJECTED,
                submittedAt = daysAgo(3),
                reviewedAt = daysAgo(2),
                reviewerId = admin.id,
                reviewerName = admin.fullName,
                reviewNote = "The uploaded photo was too blurred to read the ID number. Please submit a clear photo of the front side.",
            ),
            ResidencyVerification(
                id = resident.id,
                userId = resident.id,
                applicantName = resident.fullName,
                applicantEmail = resident.email,
                applicantPhone = resident.phone,
                address = resident.address,
                purok = resident.purok,
                yearsOfResidency = 11,
                idType = IdType.PHILIPPINE_ID,
                idNumber = "1234-4456-8890",
                documentName = "philsys-card.jpg",
                status = VerificationStatus.APPROVED,
                submittedAt = daysAgo(20),
                reviewedAt = daysAgo(19),
                reviewerId = admin.id,
                reviewerName = admin.fullName,
                reviewNote = "Details matched the barangay residency record.",
            ),
        ),
    )

    val notifications = MutableStateFlow(
        mapOf(
            resident.id to listOf(
                notification("Your report is now in progress", "Waste collection for MR-2026-0141 has been scheduled.", NotificationType.STATUS_CHANGE, "cmp-1001", hoursAgo(2), false),
                notification("The barangay replied to your report", "There is a new official comment on MR-2026-0141.", NotificationType.COMMENT, "cmp-1001", hoursAgo(2), false),
                notification("Your residency is verified", "You can now file reports for Barangay Poblacion.", NotificationType.VERIFICATION, null, daysAgo(19), true),
                notification("MR-2026-0129 was resolved", "The garden waste on Camia Street has been collected.", NotificationType.STATUS_CHANGE, "cmp-1004", daysAgo(4), true),
            ),
            admin.id to listOf(
                notification("3 reports need a first response", "Urgent and aging reports are waiting in the priority queue.", NotificationType.STATUS_CHANGE, "cmp-1010", hoursAgo(1), false, admin.id),
                notification("New residency submission", "Ana Reyes from Tunasan submitted proof of residency for review.", NotificationType.VERIFICATION, null, hoursAgo(4), false, admin.id),
                notification("MR-2026-0129 was resolved", "The garden waste report on Camia Street is now closed.", NotificationType.STATUS_CHANGE, "cmp-1004", daysAgo(4), true, admin.id),
            ),
            juan.id to listOf(
                notification("Your report is in progress", "Animal control is arranging a capture schedule for the playground.", NotificationType.STATUS_CHANGE, "cmp-1005", hoursAgo(10), false),
                notification("Report received", "MR-2026-0142 is now in the barangay queue.", NotificationType.STATUS_CHANGE, "cmp-1002", hoursAgo(27), true),
                notification("Verification submitted", "Your residency documents are waiting for review.", NotificationType.VERIFICATION, null, hoursAgo(5), true),
            ),
            ana.id to listOf(
                notification("Verification needs attention", "Please submit a clearer photo of your voter's ID.", NotificationType.VERIFICATION, null, daysAgo(2), false),
            ),
        ),
    )

    fun profile(userId: String): UserProfile? = users.value[userId]

    fun setCurrentUser(userId: String?) {
        currentUserId.value = userId
    }

    fun createAccount(profile: UserProfile, password: String) {
        users.update { it + (profile.id to profile) }
        accounts.update { it + (profile.email to DemoUserAccount(profile, password)) }
        currentUserId.value = profile.id
    }

    fun updateProfile(profile: UserProfile) {
        users.update { it + (profile.id to profile) }
        accounts.update { accounts ->
            val email = profile.email
            val account = accounts[email] ?: return@update accounts
            accounts + (email to account.copy(profile = profile))
        }
    }

    fun upsertVerification(verification: ResidencyVerification) {
        verifications.update { current ->
            listOf(verification) + current.filterNot { it.id == verification.id }
        }
        profile(verification.userId)?.let { updateProfile(it.copy(verificationStatus = verification.status, updatedAt = now)) }
    }

    fun setFollowing(userId: String, complaintId: String, following: Boolean) {
        follows.update { current ->
            val key = followKey(userId, complaintId)
            if (following) current + key else current - key
        }
    }

    fun addComplaint(complaint: Complaint) {
        complaints.update { listOf(complaint) + it }
    }

    fun updateComplaint(complaint: Complaint) {
        complaints.update { current -> current.map { if (it.id == complaint.id) complaint else it } }
    }

    fun addUpdate(update: ComplaintUpdate) {
        updates.update { current ->
            current + (update.complaintId to (current[update.complaintId].orEmpty() + update))
        }
    }

    fun addComment(comment: Comment) {
        comments.update { current ->
            current + (comment.complaintId to (current[comment.complaintId].orEmpty() + comment))
        }
    }

    fun notify(userId: String, notification: AppNotification) {
        notifications.update { current ->
            current + (userId to listOf(notification) + current[userId].orEmpty())
        }
    }

    fun notificationFor(
        recipientId: String,
        title: String,
        message: String,
        type: NotificationType,
        complaintId: String? = null,
    ): AppNotification = notification(title, message, type, complaintId, now, false, recipientId)

    fun markNotificationRead(userId: String, notificationId: String) {
        notifications.update { current ->
            current + (userId to current[userId].orEmpty().map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            })
        }
    }

    fun markAllNotificationsRead(userId: String) {
        notifications.update { current ->
            current + (userId to current[userId].orEmpty().map { it.copy(isRead = true) })
        }
    }

    fun nextComplaintId(): String = "cmp-${UUID.randomUUID().toString().take(8)}"

    fun nowMillis(): Long = now

    private fun followKey(userId: String, complaintId: String): String = "$userId|$complaintId"

    private fun update(
        complaintId: String,
        status: ComplaintStatus,
        note: String,
        author: String,
        at: Long,
        official: Boolean,
    ): ComplaintUpdate = ComplaintUpdate(
        id = "upd-${UUID.randomUUID()}",
        complaintId = complaintId,
        status = status,
        note = note,
        authorId = if (author == "MuniReport") "system" else admin.id,
        authorName = author,
        isOfficial = official,
        createdAt = at,
    )

    private fun comment(
        complaintId: String,
        authorId: String,
        authorName: String,
        role: UserRole,
        text: String,
        at: Long,
    ): Comment = Comment(
        id = "cmt-${UUID.randomUUID()}",
        complaintId = complaintId,
        authorId = authorId,
        authorName = authorName,
        authorRole = role,
        text = text,
        createdAt = at,
    )

    private fun notification(
        title: String,
        message: String,
        type: NotificationType,
        complaintId: String?,
        at: Long,
        read: Boolean,
        recipientId: String = resident.id,
    ): AppNotification = AppNotification(
        id = "ntf-${UUID.randomUUID()}",
        recipientId = recipientId,
        title = title,
        message = message,
        type = type,
        complaintId = complaintId,
        createdAt = at,
        isRead = read,
    )
}

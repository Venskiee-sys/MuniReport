package com.munireport.app.domain.model

enum class VerificationStatus(val label: String) {
    NOT_SUBMITTED("Not submitted"),
    PENDING("Under review"),
    APPROVED("Verified resident"),
    REJECTED("Needs attention"),
}

enum class IdType(val label: String) {
    PHILIPPINE_ID("Philippine ID / PhilSys"),
    DRIVERS_LICENSE("Driver's license"),
    PASSPORT("Passport"),
    VOTERS_ID("Voter's ID"),
    BARANGAY_ID("Barangay ID"),
    OTHER("Other government ID"),
}

data class ResidencyVerification(
    val id: String,
    val userId: String,
    val applicantName: String,
    val applicantEmail: String,
    val applicantPhone: String,
    val address: String,
    val purok: String,
    val yearsOfResidency: Int,
    val idType: IdType,
    val idNumber: String,
    val documentUrl: String? = null,
    val documentName: String? = null,
    val status: VerificationStatus = VerificationStatus.PENDING,
    val submittedAt: Long = 0L,
    val reviewedAt: Long? = null,
    val reviewerId: String? = null,
    val reviewerName: String? = null,
    val reviewNote: String? = null,
)

data class ResidencyDraft(
    val address: String,
    val purok: String,
    val yearsOfResidency: Int,
    val idType: IdType,
    val idNumber: String,
    val documentUri: String?,
    val documentName: String?,
)

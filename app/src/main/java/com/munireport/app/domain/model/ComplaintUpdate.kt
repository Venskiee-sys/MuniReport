package com.munireport.app.domain.model

data class NewComplaint(
    val title: String,
    val description: String,
    val category: ComplaintCategory,
    val address: String,
    val landmark: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoUris: List<String> = emptyList(),
)

data class ComplaintUpdate(
    val id: String,
    val complaintId: String,
    val status: ComplaintStatus,
    val note: String,
    val authorId: String,
    val authorName: String,
    val isOfficial: Boolean,
    val createdAt: Long,
)
